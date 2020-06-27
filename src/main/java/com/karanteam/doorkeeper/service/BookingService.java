package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.entity.Booking;
import com.karanteam.doorkeeper.entity.OfficePosition;
import com.karanteam.doorkeeper.exception.EntryForbiddenException;
import com.karanteam.doorkeeper.exception.EntryNotFoundException;
import com.karanteam.doorkeeper.repository.BookingRepository;
import java.net.URI;
import java.util.Optional;
import org.openapitools.model.RegisterResponse;
import org.openapitools.model.StatusResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Service for office booking functions.
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingCacheService bookingCacheService;
    private final VipService vipService;
    private final OfficePositionService officePositionService;

    public BookingService(BookingRepository bookingRepository,
        BookingCacheService bookingCacheService, VipService vipService,
        OfficePositionService officePositionService) {
        this.bookingRepository = bookingRepository;
        this.bookingCacheService = bookingCacheService;
        this.vipService = vipService;
        this.officePositionService = officePositionService;
    }

    public void exit(String userId) {
        if (!vipService.isVip(userId)) {
            bookingCacheService.exit(userId);
        }
    }

    public void entry(String userId) {
        if (!vipService.isVip(userId)) {
            Booking waitingBooking = getWaitingBooking(userId);
            if (!isEntryPermitted(waitingBooking)) {
                throw new EntryForbiddenException();
            }
            waitingBooking.setEntered(true);
            bookingRepository.save(waitingBooking);
            officePositionService.enter(waitingBooking.getOfficePosition().getId());
        }
    }

    private boolean isEntryPermitted(Booking user) {
        return bookingCacheService.calculatePositionFromOrdinal(user.getOrdinal()) <= 0;
    }

    public RegisterResponse register(String userId) {
        if (vipService.isVip(userId)) {
            return createRegisterResponse(0, null);
        }
        Optional<Booking> alreadyWaitingUser = findWaitingBooking(userId);
        if (alreadyWaitingUser.isPresent()) {
            String uri = getURIForPosition(alreadyWaitingUser.get().getOfficePosition());
            return createRegisterResponse(calculatePosition(alreadyWaitingUser.get()), uri);
        }

        OfficePosition officePosition = officePositionService.getNextFreePosition();
        Booking booking = bookingRepository.save(Booking.builder().userId(userId).officePosition(officePosition).build());
        String uri = getURIForPosition(officePosition);
        return createRegisterResponse(calculatePosition(booking), uri);
    }

    public void cleanupBookings() {
        bookingRepository.findByEntered(false).forEach(booking -> {
                booking.setEntered(true);
                booking.setExited(true);
                bookingRepository.save(booking);
            }
        );
    }

    private RegisterResponse createRegisterResponse(int position, String uri) {
        return new RegisterResponse().canEnter(position <= 0).position(position).positionPicture(uri);
    }

    /**
     * Creates response for status request. The position is 0 if the user can enter the office.
     *
     * @param userId Unique identifier of the user.
     * @return @StatusResponse
     */
    public StatusResponse status(String userId) {
        Booking booking = getWaitingBooking(userId);
        String uri = getURIForPosition(booking.getOfficePosition());
        return new StatusResponse().positionPicture(uri).position(
            vipService.isVip(userId) ? 0 : calculatePosition(getWaitingBooking(userId))
        );
    }

    private int calculatePosition(Booking user) { // TODO how do we handle when there is no free space by the office map? :/
        return bookingCacheService.calculatePositionFromOrdinal(user.getOrdinal());
    }

    private Booking getWaitingBooking(String userId) {
        return findWaitingBooking(userId).orElseThrow(EntryNotFoundException::new);
    }

    private Optional<Booking> findWaitingBooking(String userId) {
        return bookingRepository.findByEnteredAndUserId(false, userId);
    }

    private String getURIForPosition(final OfficePosition position) {
        if(position != null) {
            String path =  "/positions/" + position.getId();
            URI uri = ServletUriComponentsBuilder.fromCurrentContextPath().path(path).buildAndExpand().toUri();
            return uri.toString();
        }
        return null;
    }
}

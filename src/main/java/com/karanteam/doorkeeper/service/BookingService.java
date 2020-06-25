package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.entity.Booking;
import com.karanteam.doorkeeper.exception.EntryForbiddenException;
import com.karanteam.doorkeeper.exception.EntryNotFoundException;
import com.karanteam.doorkeeper.repository.BookingRepository;
import java.util.Optional;
import org.openapitools.model.RegisterResponse;
import org.openapitools.model.StatusResponse;
import org.springframework.stereotype.Service;

/**
 * Service for office booking functions.
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingCacheService bookingCacheService;
    private final VipService vipService;

    public BookingService(
        BookingRepository bookingRepository,
        BookingCacheService bookingCacheService,
        VipService vipService) {
        this.bookingRepository = bookingRepository;
        this.bookingCacheService = bookingCacheService;
        this.vipService = vipService;
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
        }
    }

    private boolean isEntryPermitted(Booking user) {
        return bookingCacheService.calculatePositionFromOrdinal(user.getOrdinal()) <= 0;
    }

    public RegisterResponse register(String userId) {
        if (vipService.isVip(userId)) {
            return createRegisterResponse(0);
        }
        Optional<Booking> alreadyWaitingUser = findWaitingBooking(userId);
        if (alreadyWaitingUser.isPresent()) {
            return createRegisterResponse(calculatePosition(alreadyWaitingUser.get()));
        }

        Booking booking = bookingRepository.save(Booking.builder().userId(userId).build());
        return createRegisterResponse(calculatePosition(booking));
    }

    public void cleanupBookings() {
        bookingRepository.findByEntered(false).forEach(booking -> {
                booking.setEntered(true);
                booking.setExited(true);
                bookingRepository.save(booking);
            }
        );
    }

    private RegisterResponse createRegisterResponse(int position) {
        return new RegisterResponse().canEnter(position <= 0).position(position);
    }

    /**
     * Creates response for status request. The position is 0 if the user can enter the office.
     *
     * @param userId Unique identifier of the user.
     * @return @StatusResponse
     */
    public StatusResponse status(String userId) {
        return new StatusResponse().position(
            vipService.isVip(userId) ? 0 : calculatePosition(getWaitingBooking(userId))
        );
    }

    private int calculatePosition(Booking user) {
        return bookingCacheService.calculatePositionFromOrdinal(user.getOrdinal());
    }

    private Booking getWaitingBooking(String userId) {
        return findWaitingBooking(userId).orElseThrow(EntryNotFoundException::new);
    }

    private Optional<Booking> findWaitingBooking(String userId) {
        return bookingRepository.findByEnteredAndUserId(false, userId);
    }
}

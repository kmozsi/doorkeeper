package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.entity.Booking;
import com.karanteam.doorkeeper.exception.EntryForbiddenException;
import com.karanteam.doorkeeper.exception.EntryNotFoundException;
import com.karanteam.doorkeeper.repository.BookingRepository;
import org.openapitools.model.RegisterResponse;
import org.openapitools.model.StatusResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.karanteam.doorkeeper.config.CachingConfig.POSITION_CACHE;

/**
 * Service for office booking functions.
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingCacheService bookingCacheService;

    public BookingService(
        BookingRepository bookingRepository,
        BookingCacheService bookingCacheService) {
        this.bookingRepository = bookingRepository;
        this.bookingCacheService = bookingCacheService;
    }

    @CacheEvict(value = POSITION_CACHE, allEntries = true)
    public void exit(String userId) {
        Booking userInBuilding = getActiveBooking(userId);
        userInBuilding.setExited(true);
        bookingRepository.save(userInBuilding);
    }

    public void entry(String userId) {
        Booking waitingBooking = getWaitingBooking(userId);
        if (!isEntryPermitted(waitingBooking)) {
            throw new EntryForbiddenException();
        }
        waitingBooking.setEntered(true);
        bookingRepository.save(waitingBooking);
    }

    private boolean isEntryPermitted(Booking user) {
        return bookingCacheService.calculatePositionFromOrdinal(user.getOrdinal()) <= 0;
    }

    public RegisterResponse register(String userId) {
        Optional<Booking> alreadyWaitingUser = findWaitingBooking(userId);
        if (alreadyWaitingUser.isPresent()) {
            return createRegisterResponse(calculatePosition(alreadyWaitingUser.get()));
        }

        Booking booking = bookingRepository.save(Booking.builder().userId(userId).build());
        return createRegisterResponse(calculatePosition(booking));
    }

    private RegisterResponse createRegisterResponse(int position) {
        return new RegisterResponse().canEnter(position <= 0).position(position);
    }

    /**
     * Creates response for status request. The position is 0 if the user can enter the office.
     * @param userId Unique identifier of the user.
     * @return @StatusResponse
     */
    public StatusResponse status(String userId) {
        return new StatusResponse().position(calculatePosition(getWaitingBooking(userId)));
    }

    private int calculatePosition(Booking user) {
        return bookingCacheService.calculatePositionFromOrdinal(user.getOrdinal());
    }

    private Booking getActiveBooking(String userId) {
        return bookingRepository.findByExitedAndEnteredAndUserId(false, true, userId)
            .orElseThrow(EntryNotFoundException::new);
    }

    private Booking getWaitingBooking(String userId) {
        return findWaitingBooking(userId).orElseThrow(EntryNotFoundException::new);
    }

    private Optional<Booking> findWaitingBooking(String userId) {
        return bookingRepository.findByEnteredAndUserId(false, userId);
    }
}

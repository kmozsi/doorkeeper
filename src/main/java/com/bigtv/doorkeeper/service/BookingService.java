package com.bigtv.doorkeeper.service;

import com.bigtv.doorkeeper.entity.Booking;
import com.bigtv.doorkeeper.exception.EntryNotFoundException;
import com.bigtv.doorkeeper.repository.BookingRepository;
import org.openapitools.model.EntryResponse;
import org.openapitools.model.RegisterResponse;
import org.openapitools.model.StatusResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import static com.bigtv.doorkeeper.config.CachingConfig.POSITION_CACHE;

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

    public EntryResponse entry(String userId) {
        Booking waitingBooking = getWaitingBooking(userId);
        if (isEntryPermitted(waitingBooking)) {
            return new EntryResponse().permitted(false);
        }
        waitingBooking.setEntered(true);
        bookingRepository.save(waitingBooking);
        return new EntryResponse().permitted(true);
    }

    private boolean isEntryPermitted(Booking user) {
        return bookingCacheService.calculatePositionFromOrdinal(user.getOrdinal()) > 0;
    }

    public RegisterResponse register(String userId) {
        Booking booking = bookingRepository.save(Booking.builder().userId(userId).build());
        return createRegisterResponse(calculatePosition(booking));
    }

    private RegisterResponse createRegisterResponse(int position) {
        return new RegisterResponse().accepted(position < 0).position(Math.max(position, 0));
    }

    public StatusResponse status(String userId) {
        return new StatusResponse().position(calculatePosition(getWaitingBooking(userId)));
    }

    private int calculatePosition(Booking user) {
        return bookingCacheService.calculatePositionFromOrdinal(user.getOrdinal());
    }

    private Booking getActiveBooking(String userId) {
        return bookingRepository.findByExitedAndUserId(false, userId)
            .orElseThrow(EntryNotFoundException::new);
    }

    private Booking getWaitingBooking(String userId) {
        return bookingRepository.findByEnteredAndUserId(false, userId)
            .orElseThrow(EntryNotFoundException::new);
    }
}
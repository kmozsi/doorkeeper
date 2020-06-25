package com.karanteam.doorkeeper.service;

import static com.karanteam.doorkeeper.config.CachingConfig.POSITION_CACHE;

import com.karanteam.doorkeeper.config.CachingConfig;
import com.karanteam.doorkeeper.entity.Booking;
import com.karanteam.doorkeeper.exception.EntryNotFoundException;
import com.karanteam.doorkeeper.repository.BookingRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service to cache the users current position.
 */
@Service
public class BookingCacheService {

    private final OfficeCapacityService officeCapacityService;
    private final BookingRepository bookingRepository;

    public BookingCacheService(
        OfficeCapacityService officeCapacityService,
        BookingRepository bookingRepository
    ) {
        this.officeCapacityService = officeCapacityService;
        this.bookingRepository = bookingRepository;
    }

    @Cacheable(value = CachingConfig.POSITION_CACHE)
    public int calculatePositionFromOrdinal(int ordinal) {
        return Math.max(ordinal - officeCapacityService.getActualDailyCapacity() - countExitedBefore(ordinal) - getFirstOrdinal() + 1, 0);
    }

    private int getFirstOrdinal() {
        return bookingRepository.findTopByOrderByOrdinalAsc().getOrdinal();
    }

    private int countExitedBefore(int ordinal) {
        return bookingRepository.countAllByExitedAndOrdinalLessThan(true, ordinal);
    }

    @CacheEvict(value = POSITION_CACHE, allEntries = true)
    public void exit(String userId) {
        Booking userInBuilding = getActiveBooking(userId);
        userInBuilding.setExited(true);
        bookingRepository.save(userInBuilding);
    }

    private Booking getActiveBooking(String userId) {
        return bookingRepository.findByExitedAndEnteredAndUserId(false, true, userId)
            .orElseThrow(EntryNotFoundException::new);
    }

}

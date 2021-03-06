package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.config.CachingConfig;
import com.karanteam.doorkeeper.entity.Booking;
import com.karanteam.doorkeeper.exception.EntryNotFoundException;
import com.karanteam.doorkeeper.repository.BookingRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import static com.karanteam.doorkeeper.config.CachingConfig.POSITION_CACHE;

/**
 * Service to cache the users current position.
 */
@Service
public class BookingCacheService {

    private final AdminService adminService;
    private final BookingRepository bookingRepository;
    private final OfficePositionService officePositionService;

    public BookingCacheService(AdminService adminService, BookingRepository bookingRepository, OfficePositionService officePositionService) {
        this.adminService = adminService;
        this.bookingRepository = bookingRepository;
        this.officePositionService = officePositionService;
    }

    @Cacheable(value = CachingConfig.POSITION_CACHE)
    public int calculatePositionFromOrdinal(int ordinal) {
        return Math.max(ordinal - adminService.getActualDailyCapacity() - countExitedBefore(ordinal) - getFirstOrdinal() + 1, 0);
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
        officePositionService.exit(userInBuilding.getOfficePosition());
        userInBuilding.setExited(true);
        userInBuilding.setOfficePosition(null);
        bookingRepository.save(userInBuilding);
    }

    private Booking getActiveBooking(String userId) {
        return bookingRepository.findByExitedAndEnteredAndUserId(false, true, userId)
            .orElseThrow(EntryNotFoundException::new);
    }
}

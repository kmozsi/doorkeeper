package com.bigtv.doorkeeper.service;

import com.bigtv.doorkeeper.repository.BookingRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static com.bigtv.doorkeeper.config.CachingConfig.POSITION_CACHE;

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

    @Cacheable(value = POSITION_CACHE)
    public int calculatePositionFromOrdinal(int ordinal) {
        return Math.max(ordinal - officeCapacityService.getActualDailyCapacity() - countExitedBefore(ordinal) - getFirstOrdinal() + 1, 0);
    }

    private int getFirstOrdinal() {
        return bookingRepository.findTopByOrderByOrdinalAsc().getOrdinal();
    }

    private int countExitedBefore(int ordinal) {
        return bookingRepository.countAllByExitedAndOrdinalLessThan(true, ordinal);
    }

}

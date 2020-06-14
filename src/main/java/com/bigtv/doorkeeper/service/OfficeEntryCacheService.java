package com.bigtv.doorkeeper.service;

import com.bigtv.doorkeeper.repository.OfficeEntryRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class OfficeEntryCacheService {

    private final OfficeCapacityService officeCapacityService;
    private final OfficeEntryRepository officeEntryRepository;

    public OfficeEntryCacheService(
        OfficeCapacityService officeCapacityService,
        OfficeEntryRepository officeEntryRepository
    ) {
        this.officeCapacityService = officeCapacityService;
        this.officeEntryRepository = officeEntryRepository;
    }

    @Cacheable(value = "positions")
    public int calculatePositionFromOrdinal(int ordinal) {
        return ordinal - officeCapacityService.getActualDailyCapacity() - countExitedBefore(ordinal) - getFirstOrdinal() + 1;
    }

    private int getFirstOrdinal() {
        return officeEntryRepository.findTopByOrderByOrdinalAsc().getOrdinal();
    }

    private int countExitedBefore(int ordinal) {
        return officeEntryRepository.countAllByExitedAndOrdinalLessThan(true, ordinal);
    }

}

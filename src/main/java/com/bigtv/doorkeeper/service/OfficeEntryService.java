package com.bigtv.doorkeeper.service;

import com.bigtv.doorkeeper.entity.OfficeEntry;
import com.bigtv.doorkeeper.exception.EntryNotFoundException;
import com.bigtv.doorkeeper.repository.OfficeEntryRepository;
import org.openapitools.model.EntryResponse;
import org.openapitools.model.RegisterResponse;
import org.openapitools.model.StatusResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OfficeEntryService {

    private final OfficeEntryRepository officeEntryRepository;
    private final OfficeCapacityService officeCapacityService;

    public OfficeEntryService(
        OfficeEntryRepository officeEntryRepository,
        OfficeCapacityService officeCapacityService
    ) {
        this.officeEntryRepository = officeEntryRepository;
        this.officeCapacityService = officeCapacityService;
    }

    public void exit(String userId) {
        OfficeEntry userInBuilding = findNotExitedUser(userId).orElseThrow(EntryNotFoundException::new);
        userInBuilding.setExited(true);
        officeEntryRepository.save(userInBuilding);
    }

    public EntryResponse entry(String userId) {
        OfficeEntry waitingUser = findWaitingUser(userId).orElseThrow(EntryNotFoundException::new);
        if (calculatePositionFromOrdinal(waitingUser.getOrdinal()) > 0) {
            return new EntryResponse().permitted(false);
        }
        waitingUser.setEntered(true);
        officeEntryRepository.save(waitingUser);
        return new EntryResponse().permitted(true);
    }

    public RegisterResponse register(String userId) {
        OfficeEntry newEntry = officeEntryRepository.save(OfficeEntry.builder().userId(userId).build());
        int position = calculatePositionFromOrdinal(newEntry.getOrdinal());
        return new RegisterResponse().accepted(position < 0).position(Math.max(position, 0));
    }

    public StatusResponse status(String userId) {
        return new StatusResponse().position(calculatePositionFromOrdinal(findWaitingUser(userId).orElseThrow(EntryNotFoundException::new).getOrdinal()));
    }

    // TODO should be cached, and cache invalidate should be triggered by exiting a user
    private int calculatePositionFromOrdinal(int ordinal) {
        return ordinal - officeCapacityService.getActualDailyCapacity() - countExitedBefore(ordinal) - getFirstOrdinal() + 1;
    }

    private Optional<OfficeEntry> findNotExitedUser(String userId) {
        return officeEntryRepository.findByExitedAndUserId(false, userId);
    }

    private Optional<OfficeEntry> findWaitingUser(String userId) {
        return officeEntryRepository.findByEnteredAndUserId(false, userId);
    }

    private int getFirstOrdinal() {
        return officeEntryRepository.findTopByOrderByOrdinalAsc().getOrdinal();
    }

    private int countExitedBefore(int ordinal) {
        return officeEntryRepository.countAllByExitedAndOrdinalLessThan(true, ordinal);
    }
}

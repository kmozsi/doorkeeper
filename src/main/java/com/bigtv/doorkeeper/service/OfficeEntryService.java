package com.bigtv.doorkeeper.service;

import com.bigtv.doorkeeper.entity.OfficeEntry;
import com.bigtv.doorkeeper.exception.EntryNotFoundException;
import com.bigtv.doorkeeper.repository.OfficeEntryRepository;
import org.openapitools.model.EntryResponse;
import org.openapitools.model.RegisterResponse;
import org.openapitools.model.StatusResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OfficeEntryService {

    private final OfficeEntryRepository officeEntryRepository;
    private final OfficeEntryCacheService officeEntryCacheService;

    public OfficeEntryService(
        OfficeEntryRepository officeEntryRepository,
        OfficeEntryCacheService officeEntryCacheService) {
        this.officeEntryRepository = officeEntryRepository;
        this.officeEntryCacheService = officeEntryCacheService;
    }

    @CacheEvict(value = "positions", allEntries = true)
    public void exit(String userId) {
        OfficeEntry userInBuilding = findNotExitedUser(userId).orElseThrow(EntryNotFoundException::new);
        userInBuilding.setExited(true);
        officeEntryRepository.save(userInBuilding);
    }

    public EntryResponse entry(String userId) {
        OfficeEntry waitingUser = findWaitingUser(userId).orElseThrow(EntryNotFoundException::new);
        if (officeEntryCacheService.calculatePositionFromOrdinal(waitingUser.getOrdinal()) > 0) {
            return new EntryResponse().permitted(false);
        }
        waitingUser.setEntered(true);
        officeEntryRepository.save(waitingUser);
        return new EntryResponse().permitted(true);
    }

    public RegisterResponse register(String userId) {
        OfficeEntry newEntry = officeEntryRepository.save(OfficeEntry.builder().userId(userId).build());
        int position = officeEntryCacheService.calculatePositionFromOrdinal(newEntry.getOrdinal());
        return new RegisterResponse().accepted(position < 0).position(Math.max(position, 0));
    }

    public StatusResponse status(String userId) {
        return new StatusResponse().position(officeEntryCacheService.calculatePositionFromOrdinal(findWaitingUser(userId).orElseThrow(EntryNotFoundException::new).getOrdinal()));
    }

    private Optional<OfficeEntry> findNotExitedUser(String userId) {
        return officeEntryRepository.findByExitedAndUserId(false, userId);
    }

    private Optional<OfficeEntry> findWaitingUser(String userId) {
        return officeEntryRepository.findByEnteredAndUserId(false, userId);
    }
}

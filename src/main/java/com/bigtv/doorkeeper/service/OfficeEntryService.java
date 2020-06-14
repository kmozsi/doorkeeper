package com.bigtv.doorkeeper.service;

import com.bigtv.doorkeeper.entity.OfficeEntry;
import com.bigtv.doorkeeper.exception.EntryNotFoundException;
import com.bigtv.doorkeeper.repository.OfficeEntryRepository;
import org.openapitools.model.EntryResponse;
import org.openapitools.model.RegisterResponse;
import org.openapitools.model.StatusResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import static com.bigtv.doorkeeper.config.CachingConfig.POSITION_CACHE;

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

    @CacheEvict(value = POSITION_CACHE, allEntries = true)
    public void exit(String userId) {
        OfficeEntry userInBuilding = getNotExitedUser(userId);
        userInBuilding.setExited(true);
        officeEntryRepository.save(userInBuilding);
    }

    public EntryResponse entry(String userId) {
        OfficeEntry waitingUser = getWaitingUser(userId);
        if (isEntryPermittedForUser(waitingUser)) {
            return new EntryResponse().permitted(false);
        }
        waitingUser.setEntered(true);
        officeEntryRepository.save(waitingUser);
        return new EntryResponse().permitted(true);
    }

    private boolean isEntryPermittedForUser(OfficeEntry user) {
        return officeEntryCacheService.calculatePositionFromOrdinal(user.getOrdinal()) > 0;
    }

    public RegisterResponse register(String userId) {
        OfficeEntry newEntry = officeEntryRepository.save(OfficeEntry.builder().userId(userId).build());
        return createRegisterResponse(calculatePositionForUser(newEntry));
    }

    private RegisterResponse createRegisterResponse(int position) {
        return new RegisterResponse().accepted(position < 0).position(Math.max(position, 0));
    }

    public StatusResponse status(String userId) {
        return new StatusResponse().position(calculatePositionForUser(getWaitingUser(userId)));
    }

    private int calculatePositionForUser(OfficeEntry user) {
        return officeEntryCacheService.calculatePositionFromOrdinal(user.getOrdinal());
    }

    private OfficeEntry getNotExitedUser(String userId) {
        return officeEntryRepository.findByExitedAndUserId(false, userId)
            .orElseThrow(EntryNotFoundException::new);
    }

    private OfficeEntry getWaitingUser(String userId) {
        return officeEntryRepository.findByEnteredAndUserId(false, userId)
            .orElseThrow(EntryNotFoundException::new);
    }
}

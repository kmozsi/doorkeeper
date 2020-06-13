package com.bigtv.doorkeeper.service;

import com.bigtv.doorkeeper.entity.OfficeEntry;
import com.bigtv.doorkeeper.exception.EntryNotFoundException;
import com.bigtv.doorkeeper.repository.OfficeEntryRepository;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;

@Service
public class OfficeEntryService {

    private final OfficeEntryRepository officeEntryRepository;

    public OfficeEntryService(OfficeEntryRepository officeEntryRepository) {
        this.officeEntryRepository = officeEntryRepository;
    }

    @Transactional
    public void exit(String userId) {
        officeEntryRepository
            .findById(userId)
            .orElseThrow(EntryNotFoundException::new)
            .setEntered(false);
    }

    @Transactional
    public void entry(String userId) {
        //TODO it is still just dummy
        officeEntryRepository.save(OfficeEntry.of(userId, 1, true));
    }
}

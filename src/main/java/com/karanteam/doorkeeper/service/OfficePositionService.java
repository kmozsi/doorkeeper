package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.entity.OfficePosition;
import com.karanteam.doorkeeper.repository.OfficePositionsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfficePositionService {

    private final OfficePositionsRepository officePositionsRepository;

    public OfficePositionService(OfficePositionsRepository officePositionsRepository) {
        this.officePositionsRepository = officePositionsRepository;
    }

    public void setPositions(List<OfficePosition> officePositions) {
        officePositionsRepository.saveAll(officePositions);
    }
}

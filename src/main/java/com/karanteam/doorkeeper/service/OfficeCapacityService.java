package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.config.CapacityConfig;
import com.karanteam.doorkeeper.entity.OfficeCapacity;
import com.karanteam.doorkeeper.repository.OfficeCapacityRepository;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.CapacityBody;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class OfficeCapacityService {

    private final OfficeCapacityRepository capacityRepository;
    private final CapacityConfig capacityConfig;

    public OfficeCapacityService(OfficeCapacityRepository capacityRepository, CapacityConfig capacityConfig) {
        this.capacityRepository = capacityRepository;
        this.capacityConfig = capacityConfig;
    }

    public int getActualDailyCapacity() {
        Optional<OfficeCapacity> optionalCapacity = capacityRepository.findTopByOrderByIdAsc();
        OfficeCapacity capacity;
        if (optionalCapacity.isPresent()) {
            capacity = optionalCapacity.get();
        } else {
            capacity = getInitialOfficeCapacity();
            capacityRepository.save(capacity);
        }
        return capacity.getDailyCapacity();
    }

    public void setCapacity(CapacityBody capacityBody) {
        Optional<OfficeCapacity> optionalCapacity = capacityRepository.findTopByOrderByIdAsc();
        OfficeCapacity capacity;
        if (optionalCapacity.isPresent()) {
            capacity = optionalCapacity.get();
        } else {
            capacity = new OfficeCapacity();
            capacity.setCapacity(capacityConfig.getInitialCapacity());
        }
        capacity.setCapacity(capacityBody.getCapacity());
        capacity.setAllowedPercentage(capacityBody.getPercentage());
        log.info("New daily capacity set: " + capacity.getDailyCapacity());
        capacityRepository.save(capacity);
    }

    private OfficeCapacity getInitialOfficeCapacity() {
        OfficeCapacity capacity = new OfficeCapacity();
        capacity.setCapacity(capacityConfig.getInitialCapacity());
        capacity.setAllowedPercentage(capacityConfig.getInitialPercentage());
        return capacity;
    }
}

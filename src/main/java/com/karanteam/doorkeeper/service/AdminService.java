package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.config.CapacityConfig;
import com.karanteam.doorkeeper.entity.OfficeCapacity;
import com.karanteam.doorkeeper.repository.OfficeCapacityRepository;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.CapacityBody;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service to handle the daily capacity of the office.
 */
@Service
@Slf4j
public class AdminService {

    private final OfficeCapacityRepository capacityRepository;
    private final CapacityConfig capacityConfig;

    public AdminService(OfficeCapacityRepository capacityRepository, CapacityConfig capacityConfig) {
        this.capacityRepository = capacityRepository;
        this.capacityConfig = capacityConfig;
    }

    public int getActualDailyCapacity() {
        return getActualOfficeCapacity().getDailyCapacity();
    }

    public int getActualMinimalDistance() {
        return getActualOfficeCapacity().getMinimalDistance();
    }

    public void setCapacity(CapacityBody capacityBody) {
        OfficeCapacity capacity = capacityRepository.findTopByOrderByIdAsc().orElseGet(OfficeCapacity::new);
        capacity.setCapacity(capacityBody.getCapacity());
        capacity.setAllowedPercentage(capacityBody.getPercentage());
        capacity.setMinimalDistance(capacityBody.getMinimalDistance());
        log.info("New daily capacity set: " + capacity.getDailyCapacity());
        capacityRepository.save(capacity);
    }

    private OfficeCapacity getActualOfficeCapacity() {
        Optional<OfficeCapacity> optionalCapacity = capacityRepository.findTopByOrderByIdAsc();
        OfficeCapacity capacity;
        if (optionalCapacity.isPresent()) {
            capacity = optionalCapacity.get();
        } else {
            capacity = getInitialOfficeCapacity();
            capacityRepository.save(capacity);
        }
        return capacity;
    }

    private OfficeCapacity getInitialOfficeCapacity() {
        OfficeCapacity capacity = new OfficeCapacity();
        capacity.setCapacity(capacityConfig.getInitialCapacity());
        capacity.setAllowedPercentage(capacityConfig.getInitialPercentage());
        capacity.setMinimalDistance(capacityConfig.getInitialMinDistance());
        return capacity;
    }
}

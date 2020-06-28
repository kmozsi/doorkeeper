package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.config.ApplicationConfig;
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
    private final ApplicationConfig applicationConfig;

    public AdminService(OfficeCapacityRepository capacityRepository, ApplicationConfig applicationConfig) {
        this.capacityRepository = capacityRepository;
        this.applicationConfig = applicationConfig;
    }

    public int getActualDailyCapacity() {
        OfficeCapacity capacity = getActualOfficeCapacity();
        int dailyCapacity = capacity.getDailyCapacity();
        int maxCapacityOnMap = capacity.getMaxMapCapacity();
        return Math.min(dailyCapacity, maxCapacityOnMap);
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

    public void setMapCapacity(int mapCapacity) {
        OfficeCapacity capacity = getActualOfficeCapacity();
        capacity.setMaxMapCapacity(mapCapacity);
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
        capacity.setCapacity(applicationConfig.getInitialCapacity());
        capacity.setAllowedPercentage(applicationConfig.getInitialPercentage());
        capacity.setMinimalDistance(applicationConfig.getInitialMinDistance());
        capacity.setMaxMapCapacity(capacity.getDailyCapacity());
        return capacity;
    }
}

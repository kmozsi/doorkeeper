package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.config.ApplicationConfig;
import com.karanteam.doorkeeper.entity.OfficeCapacity;
import com.karanteam.doorkeeper.repository.OfficeCapacityRepository;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.CapacityBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AdminService.class)
public class AdminServiceTest {

    private static final int CAPACITY = 200;
    private static final int PERCENTAGE = 10;
    private static final int MIN_DISTANCE = 5;
    private static final int MAX_MAP_CAPACITY = 20;

    @Autowired
    private AdminService adminService;

    @MockBean
    private OfficeCapacityRepository capacityRepository;

    @SpyBean
    private ApplicationConfig applicationConfig;

    @BeforeEach
    private void init() {
        when(applicationConfig.getInitialCapacity()).thenReturn(200);
        when(applicationConfig.getInitialPercentage()).thenReturn(10);
        when(applicationConfig.getInitialMinDistance()).thenReturn(5);
    }

    @Test
    public void getActualDailyCapacityWhenAlreadyExist() {
        OfficeCapacity capacity = OfficeCapacity.of(1, CAPACITY, PERCENTAGE, MIN_DISTANCE, MAX_MAP_CAPACITY);
        when(capacityRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(capacity));
        assertEquals(20, adminService.getActualDailyCapacity());
    }

    @Test
    public void getActualDailyCapacityWhenMapCapacityIsLess() {
        OfficeCapacity capacity = OfficeCapacity.of(1, CAPACITY, PERCENTAGE, MIN_DISTANCE, 2);
        when(capacityRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(capacity));
        assertEquals(2, adminService.getActualDailyCapacity());
    }

    @Test
    public void getInitialDailyCapacity() {
        when(capacityRepository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());
        when(capacityRepository.save(any())).thenReturn(OfficeCapacity.of(1, CAPACITY, PERCENTAGE, MIN_DISTANCE, MAX_MAP_CAPACITY));
        assertEquals(20, adminService.getActualDailyCapacity());
    }

    @Test
    public void getInitialDailyCapacityWhenMapCapacityNotSet() {
        when(capacityRepository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());
        when(capacityRepository.save(any())).thenReturn(OfficeCapacity.of(1, CAPACITY, PERCENTAGE, MIN_DISTANCE, 0));
        assertEquals(20, adminService.getActualDailyCapacity());
    }

    @Test
    public void setCapacityWhenAlreadyExist() throws IOException {
        int newCapacity = 500;
        int newPercentage = 50;
        int newDistance = 1;

        OfficeCapacity capacity = OfficeCapacity.of(1, CAPACITY, PERCENTAGE, MIN_DISTANCE, MAX_MAP_CAPACITY);
        CapacityBody capacityBody = new CapacityBody();
        capacityBody.setCapacity(newCapacity);
        capacityBody.setPercentage(newPercentage);
        capacityBody.setMinimalDistance(newDistance);

        when(capacityRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(capacity));
        adminService.setCapacity(capacityBody);

        verify(capacityRepository, times(1)).save(argThat(c ->
            c.getId() == 1 && c.getAllowedPercentage() == newPercentage && c.getCapacity() == newCapacity && c.getMinimalDistance() == newDistance
        ));
        verify(capacityRepository, times(1)).findTopByOrderByIdAsc();
    }

    @Test
    public void setCapacityWhenEntryDoesNotExist() throws IOException {
        int newCapacity = 500;
        int newPercentage = 50;
        int newDistance = 1;

        CapacityBody capacityBody = new CapacityBody();
        capacityBody.setCapacity(newCapacity);
        capacityBody.setPercentage(newPercentage);
        capacityBody.setMinimalDistance(newDistance);

        when(capacityRepository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());
        adminService.setCapacity(capacityBody);

        verify(capacityRepository, times(1)).save(argThat(c ->
            c.getAllowedPercentage() == newPercentage && c.getCapacity() == newCapacity && c.getMinimalDistance() == newDistance
        ));
        verify(capacityRepository, times(1)).findTopByOrderByIdAsc();
    }
}

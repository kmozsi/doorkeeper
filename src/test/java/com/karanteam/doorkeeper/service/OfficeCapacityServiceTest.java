package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.config.CapacityConfig;
import com.karanteam.doorkeeper.entity.Booking;
import com.karanteam.doorkeeper.entity.OfficeCapacity;
import com.karanteam.doorkeeper.exception.EntryForbiddenException;
import com.karanteam.doorkeeper.exception.EntryNotFoundException;
import com.karanteam.doorkeeper.repository.OfficeCapacityRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.model.CapacityBody;
import org.openapitools.model.RegisterResponse;
import org.openapitools.model.StatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OfficeCapacityServiceTest {

    private static final int CAPACITY = 200;
    private static final int PERCENTAGE = 10;

    @Autowired
    private OfficeCapacityService capacityService;

    @MockBean
    private OfficeCapacityRepository capacityRepository;

    @MockBean
    private CapacityConfig capacityConfig;

    @Test
    public void getActualDailyCapacityWhenAlreadyExist() {
        OfficeCapacity capacity = OfficeCapacity.of(1, CAPACITY, PERCENTAGE);
        when(capacityRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(capacity));
        assertEquals(20, capacityService.getActualDailyCapacity());
    }

    @Test
    public void getInitialDailyCapacity() {
        when(capacityRepository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());
        when(capacityConfig.getInitialCapacity()).thenReturn(CAPACITY);
        when(capacityConfig.getInitialPercentage()).thenReturn(PERCENTAGE);
        when(capacityRepository.save(any())).thenReturn(OfficeCapacity.of(1, CAPACITY, PERCENTAGE));

        assertEquals(20, capacityService.getActualDailyCapacity());
    }

    @Test
    public void setCapacityWhenAlreadyExist() {
        int newCapacity = 500;
        int newPercentage = 50;

        OfficeCapacity capacity = OfficeCapacity.of(1, CAPACITY, PERCENTAGE);
        CapacityBody capacityBody = new CapacityBody();
        capacityBody.setCapacity(newCapacity);
        capacityBody.setPercentage(newPercentage);

        when(capacityRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(capacity));
        capacityService.setCapacity(capacityBody);

        verify(capacityRepository, times(1)).save(argThat(c ->
            c.getId() == 1 && c.getAllowedPercentage() == newPercentage && c.getCapacity() == newCapacity
        ));
        verify(capacityRepository, times(1)).findTopByOrderByIdAsc();
    }

    @Test
    public void setCapacityWhenEntryDoesNotExist() {
        int newCapacity = 500;
        int newPercentage = 50;

        CapacityBody capacityBody = new CapacityBody();
        capacityBody.setCapacity(newCapacity);
        capacityBody.setPercentage(newPercentage);

        when(capacityRepository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());
        capacityService.setCapacity(capacityBody);

        verify(capacityRepository, times(1)).save(argThat(c ->
            c.getAllowedPercentage() == newPercentage && c.getCapacity() == newCapacity
        ));
        verify(capacityRepository, times(1)).findTopByOrderByIdAsc();
    }
}

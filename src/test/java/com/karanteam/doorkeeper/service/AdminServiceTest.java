package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.entity.OfficeCapacity;
import com.karanteam.doorkeeper.repository.OfficeCapacityRepository;
import org.junit.jupiter.api.Test;
import org.openapitools.model.CapacityBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = { "application.initialCapacity=200" })
public class AdminServiceTest {

    private static final int CAPACITY = 200;
    private static final int PERCENTAGE = 10;
    private static final int MIN_DISTANCE = 5;

    @Autowired
    private AdminService capacityService;

    @MockBean
    private OfficeCapacityRepository capacityRepository;

    @Test
    public void getActualDailyCapacityWhenAlreadyExist() {
        OfficeCapacity capacity = OfficeCapacity.of(1, CAPACITY, PERCENTAGE, MIN_DISTANCE);
        when(capacityRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(capacity));
        assertEquals(20, capacityService.getActualDailyCapacity());
    }

    @Test
    public void getInitialDailyCapacity() {
        when(capacityRepository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());
        when(capacityRepository.save(any())).thenReturn(OfficeCapacity.of(1, CAPACITY, PERCENTAGE, MIN_DISTANCE));
        assertEquals(20, capacityService.getActualDailyCapacity());
    }

    @Test
    public void setCapacityWhenAlreadyExist() {
        int newCapacity = 500;
        int newPercentage = 50;
        int newDistance = 1;

        OfficeCapacity capacity = OfficeCapacity.of(1, CAPACITY, PERCENTAGE, MIN_DISTANCE);
        CapacityBody capacityBody = new CapacityBody();
        capacityBody.setCapacity(newCapacity);
        capacityBody.setPercentage(newPercentage);
        capacityBody.setMinimalDistance(newDistance);

        when(capacityRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(capacity));
        capacityService.setCapacity(capacityBody);

        verify(capacityRepository, times(1)).save(argThat(c ->
            c.getId() == 1 && c.getAllowedPercentage() == newPercentage && c.getCapacity() == newCapacity && c.getMinimalDistance() == newDistance
        ));
        verify(capacityRepository, times(1)).findTopByOrderByIdAsc();
    }

    @Test
    public void setCapacityWhenEntryDoesNotExist() {
        int newCapacity = 500;
        int newPercentage = 50;
        int newDistance = 1;

        CapacityBody capacityBody = new CapacityBody();
        capacityBody.setCapacity(newCapacity);
        capacityBody.setPercentage(newPercentage);
        capacityBody.setMinimalDistance(newDistance);

        when(capacityRepository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());
        capacityService.setCapacity(capacityBody);

        verify(capacityRepository, times(1)).save(argThat(c ->
            c.getAllowedPercentage() == newPercentage && c.getCapacity() == newCapacity && c.getMinimalDistance() == newDistance
        ));
        verify(capacityRepository, times(1)).findTopByOrderByIdAsc();
    }
}

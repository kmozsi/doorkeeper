package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.config.ApplicationConfig;
import com.karanteam.doorkeeper.entity.OfficePosition;
import com.karanteam.doorkeeper.enumeration.PositionStatus;
import com.karanteam.doorkeeper.repository.OfficePositionsRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import static com.karanteam.doorkeeper.data.OfficePositionOrientation.EAST;
import static com.karanteam.doorkeeper.data.OfficePositionOrientation.NORTH;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = OfficePositionService.class)
public class OfficePositionServiceTest {

    private static final OfficePosition TAKEN = OfficePosition.builder()
        .id(1)
        .status(PositionStatus.TAKEN)
        .x(10).y(10)
        .orientation(NORTH)
        .build();

    private static final OfficePosition FREE_NEXT_TO_TAKEN = OfficePosition.builder()
        .id(2)
        .status(PositionStatus.FREE)
        .x(11).y(11)
        .orientation(NORTH)
        .build();

    private static final OfficePosition FREE = OfficePosition.builder()
        .id(3)
        .status(PositionStatus.FREE)
        .x(100).y(100)
        .orientation(NORTH)
        .build();

    @Autowired
    private OfficePositionService officePositionService;

    @MockBean
    private OfficePositionsRepository officePositionsRepository;

    @MockBean
    private AdminService adminService;

    @SpyBean
    private ApplicationConfig applicationConfig;

    @BeforeEach
    private void init() {
        when(applicationConfig.getPixelSizeInCm()).thenReturn(10);
    }

    @Test
    public void imageProcessingFoundAllPositionAndStoreIt() {
        List<OfficePosition> officePositions = Arrays.asList(
            OfficePosition.builder().x(10).y(20).orientation(NORTH).build(),
            OfficePosition.builder().x(20).y(30).orientation(EAST).build()
        );

        officePositionService.setPositions(officePositions);
        verify(officePositionsRepository, times(1)).saveAll(officePositions);
    }

    @Test
    public void testEnterTheOffice() {
        OfficePosition position = OfficePosition.builder()
            .x(10).y(20).orientation(NORTH).status(PositionStatus.BOOKED).build();
        officePositionService.enter(position);
        verify(officePositionsRepository).save(argThat(pos -> pos.getStatus() == PositionStatus.TAKEN));
    }

    @Test
    public void testExitTheOffice() {
        OfficePosition position = OfficePosition.builder()
            .x(10).y(20).orientation(NORTH).status(PositionStatus.TAKEN).build();
        officePositionService.exit(position);
        verify(officePositionsRepository).save(argThat(pos -> pos.getStatus() == PositionStatus.FREE));
    }

    @Test
    public void thereIsNoPosition() {
        when(officePositionsRepository.findAll()).thenReturn(Collections.singletonList(TAKEN));
        OfficePosition freePosition = officePositionService.getNextFreePosition();
        Assertions.assertNull(freePosition);
    }

    @Test
    public void thereIsNoFreePositionAllowed() {
        when(adminService.getActualMinimalDistance()).thenReturn(5);
        when(officePositionsRepository.findAll()).thenReturn(Arrays.asList(TAKEN, FREE_NEXT_TO_TAKEN));
        OfficePosition freePosition = officePositionService.getNextFreePosition();
        Assertions.assertNull(freePosition);
    }

    @Test
    public void thereIsOneFreePositionAllowed() {
        when(adminService.getActualMinimalDistance()).thenReturn(5);
        when(officePositionsRepository.findAll()).thenReturn(Arrays.asList(TAKEN, FREE_NEXT_TO_TAKEN, FREE));
        OfficePosition freePosition = officePositionService.getNextFreePosition();
        Assertions.assertEquals(FREE.getId(), freePosition.getId());
    }
}

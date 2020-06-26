package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.entity.OfficePosition;
import com.karanteam.doorkeeper.repository.OfficePositionsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;

import static com.karanteam.doorkeeper.data.OfficePositionOrientation.EAST;
import static com.karanteam.doorkeeper.data.OfficePositionOrientation.NORTH;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OfficePositionServiceTest {

    @Autowired
    private OfficePositionService officePositionService;

    @MockBean
    private OfficePositionsRepository officePositionsRepository;

    @Test
    public void imageProcessingFoundAllPositionAndStoreIt() {
        List<OfficePosition> officePositions = Arrays.asList(
            OfficePosition.builder().x(10).y(20).orientation(NORTH).build(),
            OfficePosition.builder().x(20).y(30).orientation(EAST).build()
        );

        officePositionService.setPositions(officePositions);
        verify(officePositionsRepository, times(1)).saveAll(officePositions);
    }
}

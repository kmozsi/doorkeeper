package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.entity.OfficePosition;
import com.karanteam.doorkeeper.entity.Vip;
import com.karanteam.doorkeeper.repository.VipRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PositionOptimalizationServiceTest {

    @Autowired
    private PositionOptimalizationService positionOptimalizationService;

    @Autowired
    private OfficeMapService officeMapService;

    @Autowired
    private OfficePositionService officePositionService;



    @Test
    public void optimalDistributionShouldFound() {
        List<OfficePosition> allPositions = officePositionService.getAllPositions();
        Assertions.assertEquals(280, allPositions.size());

        List<OfficePosition> optimalPositionDistribution = positionOptimalizationService
            .getOptimalPositionDistribution(
                50,
                allPositions.subList(0,20)
            );

        Assertions.assertEquals(42, optimalPositionDistribution.size());
    }



}

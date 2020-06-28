package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.entity.OfficePosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OfficeMapServiceTest {

    @Autowired
    private OfficeMapService officeMapService;

    @MockBean
    private OfficePositionService officePositionService;

    @Captor
    private ArgumentCaptor<List<OfficePosition>> captor;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void imageProcessingFoundAllPositionAndStoreIt() throws IOException {
        when(officePositionService.setPositions(any())).thenReturn(10);

        File file = ResourceUtils.getFile("classpath:image/original.jpg");
        byte[] bytes = Files.readAllBytes(file.toPath());
        int count = officeMapService.storePositions(bytes);
        assertEquals(10, count);

        verify(officePositionService, times(2)).setPositions(captor.capture()); // TODO init miatt
        assertTrue(captor.getValue().size() > 100);
    }
}

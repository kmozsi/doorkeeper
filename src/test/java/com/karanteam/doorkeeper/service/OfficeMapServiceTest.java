package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.entity.OfficePosition;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.ResourceUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class OfficeMapServiceTest {

    @Autowired
    private OfficeMapService officeMapService;

    @MockBean
    private OfficePositionService officePositionService;

    @Captor
    private ArgumentCaptor<List<OfficePosition>> captor;

    @Test
    public void imageProcessingFoundAllPositionAndStoreIt() throws IOException {
        clearInvocations(officePositionService);
        when(officePositionService.setPositions(any())).thenReturn(10);

        File file = ResourceUtils.getFile("classpath:image/original.jpg");
        byte[] bytes = Files.readAllBytes(file.toPath());
        int count = officeMapService.storeOfficeAndPositions(bytes);
        assertEquals(10, count);

        verify(officePositionService, times(1)).setPositions(captor.capture());
        assertTrue(captor.getValue().size() > 100);
    }
}

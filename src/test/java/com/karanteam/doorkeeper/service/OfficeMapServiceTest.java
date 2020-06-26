package com.karanteam.doorkeeper.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.ResourceUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OfficeMapServiceTest {

    @Autowired
    private OfficeMapService officeMapService;

    @MockBean
    private OfficePositionService officePositionService;

    @Test
    public void imageProcessingFoundAllPositionAndStoreIt() throws IOException {

        File file = ResourceUtils.getFile("classpath:image/original.jpg");
        byte[] bytes = Files.readAllBytes(file.toPath());
        int count = officeMapService.storePositions(bytes);
        assertEquals(108, count);

        verify(officePositionService, times(1)).setPositions(anyList());
    }
}

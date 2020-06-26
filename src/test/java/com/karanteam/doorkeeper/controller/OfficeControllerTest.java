package com.karanteam.doorkeeper.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.karanteam.doorkeeper.service.JwtService;
import com.karanteam.doorkeeper.service.OfficeMapService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OfficeControllerTest {

    private static final String HEADER_TOKEN_NAME = "X-Token";
    private static final String TOKEN = "TEST";
    private static final String USER_ID = "USER_ID";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OfficeMapService officeMapService;

    @MockBean
    private JwtService jwtService;

    @Test
    public void setPositionsFailsBecauseTokenIsInvalid() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenThrow(new JWTVerificationException(""));

        mockMvc.perform(multipart("/positions")
            .file(new MockMultipartFile("officeMap", "filename.txt", "text/plain", "some xml".getBytes()))
            .contentType(MULTIPART_FORM_DATA)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isForbidden());

        verify(officeMapService, never()).storePositions(any());
    }

    @Test
    public void setPositionsSuccessfullyProcessed() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);

        mockMvc.perform(multipart("/positions")
            .file(new MockMultipartFile("officeMap", "filename.txt", "text/plain", "some xml".getBytes()))
            .contentType(MULTIPART_FORM_DATA)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isOk());

        verify(officeMapService, times(1)).storePositions(any());
        verifyNoMoreInteractions(officeMapService);
    }
}

package com.karanteam.doorkeeper.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.karanteam.doorkeeper.exception.EntryForbiddenException;
import com.karanteam.doorkeeper.exception.EntryNotFoundException;
import com.karanteam.doorkeeper.service.JwtService;
import com.karanteam.doorkeeper.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerTest {

    private static final String HEADER_TOKEN_NAME = "X-Token";
    private static final String TOKEN = "TEST";
    private static final String USER_ID = "USER_ID";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtService jwtService;

    @Test
    public void setCapacityFailsBecauseTokenIsInvalid() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenThrow(new JWTVerificationException(""));

        mockMvc.perform(patch("/capacity")
            .contentType(APPLICATION_JSON)
            .content("{\"capacity\": 1, \"percentage\": 1}")
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isForbidden());

        verify(adminService, never()).setCapacity(any());
    }

    @Test
    public void setCapacitySuccessfullyProcessed() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);

        mockMvc.perform(patch("/capacity")
            .contentType(APPLICATION_JSON)
            .content("{\"capacity\": 1, \"percentage\": 1}")
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isOk());

        verify(adminService, times(1)).setCapacity(any());
        verifyNoMoreInteractions(adminService);
    }
}

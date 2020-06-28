package com.karanteam.doorkeeper.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.karanteam.doorkeeper.exception.EntryForbiddenException;
import com.karanteam.doorkeeper.exception.EntryNotFoundException;
import com.karanteam.doorkeeper.service.BookingService;
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
public class BookingControllerTest {

    private static final String HEADER_TOKEN_NAME = "X-Token";
    private static final String TOKEN = "TEST";
    private static final String USER_ID = "USER_ID";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtService jwtService;

    @Test
    public void registerFailsBecauseTokenIsInvalid() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenThrow(new JWTVerificationException(""));

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isForbidden());

        verify(bookingService, never()).register(anyString());
    }

    @Test
    public void registerSuccessfullyProcessed() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isOk());

        verify(bookingService, times(1)).register(USER_ID);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    public void entryFailsBecauseTokenIsInvalid() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenThrow(new JWTVerificationException(""));

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isForbidden());

        verify(bookingService, never()).entry(anyString());
    }

    @Test
    public void entryFailsBecauseCapacityIsReached() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);
        doThrow(new EntryForbiddenException()).when(bookingService).entry(anyString());

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isBadRequest());

        verify(bookingService, times(1)).entry(anyString());
    }

    @Test
    public void entryFailsBecauseEntryNotFound() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);
        doThrow(new EntryNotFoundException()).when(bookingService).entry(anyString());

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isNotFound());

        verify(bookingService, times(1)).entry(anyString());
    }

    @Test
    public void entrySuccessfullyProcessed() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isOk());

        verify(bookingService, times(1)).entry(USER_ID);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    public void getStatusFailsBecauseTokenIsInvalid() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenThrow(new JWTVerificationException(""));

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isForbidden());

        verify(bookingService, never()).status(anyString());
    }

    @Test
    public void getStatusFailsBecauseEntryNotFound() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);
        doThrow(new EntryNotFoundException()).when(bookingService).status(anyString());

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isNotFound());

        verify(bookingService, times(1)).status(anyString());
    }

    @Test
    public void getStatusSuccessfullyProcessed() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isOk());

        verify(bookingService, times(1)).status(USER_ID);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    public void exitFailsBecauseTokenIsInvalid() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenThrow(new JWTVerificationException(""));

        mockMvc.perform(post("/exit")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isForbidden());

        verify(bookingService, never()).exit(anyString());
    }

    @Test
    public void exitFailsBecauseEntryNotFound() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);
        doThrow(new EntryNotFoundException()).when(bookingService).exit(anyString());

        mockMvc.perform(post("/exit")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isNotFound());

        verify(bookingService, times(1)).exit(anyString());
    }

    @Test
    public void exitSuccessfullyProcessed() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);

        mockMvc.perform(post("/exit")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isOk());

        verify(bookingService, times(1)).exit(USER_ID);
        verifyNoMoreInteractions(bookingService);
    }
}

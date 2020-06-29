package com.karanteam.doorkeeper.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.karanteam.doorkeeper.exception.GlobalExceptionHandler;
import com.karanteam.doorkeeper.service.AdminService;
import com.karanteam.doorkeeper.service.BookingService;
import com.karanteam.doorkeeper.service.JwtService;
import com.karanteam.doorkeeper.service.OfficeMapService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {GlobalExceptionHandler.class, AdminController.class})
@AutoConfigureMockMvc
@EnableWebMvc
public class AdminControllerTest {

    private static final String HEADER_TOKEN_NAME = "X-Token";
    private static final String TOKEN = "TEST";
    private static final String USER_ID = "USER_ID";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private OfficeMapService officeMapService;

    @MockBean
    private BookingService bookingService;

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
        when(bookingService.isThereActiveBooking()).thenReturn(false);

        mockMvc.perform(patch("/capacity")
            .contentType(APPLICATION_JSON)
            .content("{\"capacity\": 1, \"percentage\": 1}")
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isOk());

        verify(adminService, times(1)).setCapacity(any());
        verify(officeMapService, times(1)).recalculatePositions();
    }

    @Test
    public void setCapacityFailsBecauseThereAreActiveBookings() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);
        when(bookingService.isThereActiveBooking()).thenReturn(true);

        mockMvc.perform(patch("/capacity")
            .contentType(APPLICATION_JSON)
            .content("{\"capacity\": 1, \"percentage\": 1}")
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(adminService, officeMapService);
    }
}

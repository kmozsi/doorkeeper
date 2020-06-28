package com.karanteam.doorkeeper.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.karanteam.doorkeeper.data.OfficePositionOrientation;
import com.karanteam.doorkeeper.entity.OfficePosition;
import com.karanteam.doorkeeper.enumeration.PositionStatus;
import com.karanteam.doorkeeper.service.BookingService;
import com.karanteam.doorkeeper.service.JwtService;
import com.karanteam.doorkeeper.service.OfficeMapService;
import com.karanteam.doorkeeper.service.OfficePositionService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class OfficeControllerTest {

    private static final String HEADER_TOKEN_NAME = "X-Token";
    private static final String TOKEN = "TEST";
    private static final String USER_ID = "USER_ID";
    private static final OfficePosition OFFICE_POSITION = OfficePosition.builder().x(100).y(100)
        .orientation(OfficePositionOrientation.NORTH).id(1).status(PositionStatus.BOOKED).build();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OfficeMapService officeMapService;

    @MockBean
    private OfficePositionService officePositionService;

    @MockBean
    private BookingService bookingService;

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

        verify(officeMapService, never()).storeOfficeAndPositions(any());
    }

    @Test
    public void setPositionsSuccessfullyProcessed() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);
        when(bookingService.isThereActiveBooking()).thenReturn(false);

        mockMvc.perform(multipart("/positions")
            .file(new MockMultipartFile("officeMap", "filename.txt", "text/plain", "some xml".getBytes()))
            .contentType(MULTIPART_FORM_DATA)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isOk());

        verify(officeMapService, times(1)).storeOfficeAndPositions(any());
        verifyNoMoreInteractions(officeMapService);
    }

    @Test
    public void setPositionsFailsBecauseThereAreActiveBookings() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);
        when(bookingService.isThereActiveBooking()).thenReturn(true);

        mockMvc.perform(multipart("/positions")
            .file(new MockMultipartFile("officeMap", "filename.txt", "text/plain", "some xml".getBytes()))
            .contentType(MULTIPART_FORM_DATA)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(officeMapService);
    }

    @Test
    public void getPositionFailsBecauseTokenIsInvalid() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenThrow(new JWTVerificationException(""));

        mockMvc.perform(get("/positions/{id}", 1)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isForbidden());

        verify(officePositionService, never()).findById(any());
        verify(officeMapService, never()).markPosition(any());
    }

    @Test
    public void getPositionReturnsPicture() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);
        when(officePositionService.findById(any())).thenReturn(Optional.of(OFFICE_POSITION));

        mockMvc.perform(get("/positions/{id}", 1)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isOk());

        verify(officePositionService, times(1)).findById(1);
        verify(officeMapService, times(1)).markPosition(
            argThat(pos -> pos.getId() == 1 && pos.getStatus() == PositionStatus.BOOKED)
        );
    }
}

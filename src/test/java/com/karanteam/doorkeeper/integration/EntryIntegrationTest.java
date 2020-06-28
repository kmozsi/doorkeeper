package com.karanteam.doorkeeper.integration;

import com.karanteam.doorkeeper.entity.OfficeCapacity;
import com.karanteam.doorkeeper.repository.BookingRepository;
import com.karanteam.doorkeeper.repository.OfficeCapacityRepository;
import com.karanteam.doorkeeper.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EntryIntegrationTest {

    private static final String HEADER_TOKEN_NAME = "X-Token";

    private static final String USER_1_X_TOKEN = "TEST";
    private static final String USER_2_X_TOKEN = "TEST2";
    private static final String USER_3_X_TOKEN = "TEST3";
    private static final String USER_4_X_TOKEN = "TEST4";
    private static final String USER_1_ID = "uid";
    private static final String USER_2_ID = "uid2";
    private static final String USER_3_ID = "uid3";
    private static final String USER_4_ID = "uid4";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private OfficeCapacityRepository officeCapacityRepository;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    public void deleteTables() {
        bookingRepository.deleteAll();
        officeCapacityRepository.deleteAll();
    }

    @Test
    public void usersCanRegisterThenEntryIntoAnEmptyHouse() throws Exception {
        when(jwtService.parseToken(matches(USER_1_X_TOKEN), any())).thenReturn(USER_1_ID);
        when(jwtService.parseToken(matches(USER_2_X_TOKEN), any())).thenReturn(USER_2_ID);

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'canEnter' :true,'position':0}"));

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'position':0}"));

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_2_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'canEnter':true,'position':0}"));

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_2_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'position':0}"));

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk());

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_2_X_TOKEN))
            .andExpect(status().isOk());

        mockMvc.perform(post("/exit")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk());
    }

    @Test
    public void entryShouldRefusedAfterRegisteringToAFullHouse() throws Exception {
        thereIsTwoPlaceForToday();
        thereAreTwoEmployeeInTheBuilding();

        when(jwtService.parseToken(matches(USER_1_X_TOKEN), any())).thenReturn(USER_1_ID);

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'canEnter':false,'position':1}"));

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'position':1}"));

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void exitingShouldCreatePlacesForWaitingUsers() throws Exception {
        thereIsTwoPlaceForToday();
        thereAreTwoEmployeeInTheBuilding();

        when(jwtService.parseToken(matches(USER_1_X_TOKEN), any())).thenReturn(USER_1_ID);
        when(jwtService.parseToken(matches(USER_2_X_TOKEN), any())).thenReturn(USER_2_ID);
        when(jwtService.parseToken(matches(USER_4_X_TOKEN), any())).thenReturn(USER_4_ID);

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'canEnter':false,'position':1}"));

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_4_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'canEnter':false,'position':2}"));

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'position':1}"));

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_4_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'position':2}"));

        mockMvc.perform(post("/exit")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_2_X_TOKEN))
            .andExpect(status().isOk());

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'position':0}"));

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_4_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'position':1}"));
    }

    @Test
    public void userShouldRegisterAgainAfterExiting() throws Exception {

        when(jwtService.parseToken(matches(USER_1_X_TOKEN), any())).thenReturn(USER_1_ID);

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'canEnter':true,'position':0}"));

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'position':0}"));

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk());

        mockMvc.perform(post("/exit")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk());

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'canEnter':true,'position':0}"));

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'position':0}"));

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk());
    }

    @Test
    public void userGetPositionWithNewRegistrationAfterExit() throws Exception {
        thereIsTwoPlaceForToday();
        thereAreTwoEmployeeInTheBuilding();

        when(jwtService.parseToken(matches(USER_1_X_TOKEN), any())).thenReturn(USER_1_ID);
        when(jwtService.parseToken(matches(USER_2_X_TOKEN), any())).thenReturn(USER_2_ID);

        mockMvc.perform(post("/exit")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_2_X_TOKEN))
            .andExpect(status().isOk());

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'canEnter':true,'position':0}"));

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk());

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_2_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'canEnter':false,'position':1}"));

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_2_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json("{'position':1}"));
    }

    @Test
    public void userCantExitWithoutEntry() throws Exception {
        when(jwtService.parseToken(matches(USER_1_X_TOKEN), any())).thenReturn(USER_1_ID);

        mockMvc.perform(post("/exit")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isNotFound());
    }

    private void thereIsTwoPlaceForToday() {
        officeCapacityRepository.save(OfficeCapacity.of(1, 10, 20, 5, 12));
    }

    private void thereAreTwoEmployeeInTheBuilding() throws Exception {
        when(jwtService.parseToken(matches(USER_3_X_TOKEN), any())).thenReturn(USER_3_ID);
        when(jwtService.parseToken(matches(USER_2_X_TOKEN), any())).thenReturn(USER_2_ID);

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_3_X_TOKEN))
            .andExpect(status().isOk());

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_2_X_TOKEN))
            .andExpect(status().isOk());

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_3_X_TOKEN))
            .andExpect(status().isOk());

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_2_X_TOKEN))
            .andExpect(status().isOk());
    }
}

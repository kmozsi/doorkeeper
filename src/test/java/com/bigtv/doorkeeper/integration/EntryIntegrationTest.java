package com.bigtv.doorkeeper.integration;

import com.bigtv.doorkeeper.entity.OfficeCapacity;
import com.bigtv.doorkeeper.entity.Booking;
import com.bigtv.doorkeeper.repository.OfficeCapacityRepository;
import com.bigtv.doorkeeper.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EntryIntegrationTest {

    private static final String HEADER_TOKEN_NAME = "X-Token";

    // TODO mock jwtService
    private static final String USER_1_X_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZXhwIjoxNjE2NTM5MTIyLCJpYXQiOjE1MTYyMzkwMjIsInJvbGVzIjpbIkVNUExPWUVFIiwiSFIiXSwidXNlcklkIjoidWlkIn0.cGsC9yA77vTcSK7He0D3Vt0OBSWQvQS33AO387cdA1Q";
    private static final String USER_2_X_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlRpYmkiLCJleHAiOjE2MTY1MzkxMjIsImlhdCI6MTUxNjIzOTAyMiwicm9sZXMiOlsiRU1QTE9ZRUUiLCJIUiJdLCJ1c2VySWQiOiJ1aWQyIn0.F7yAcpaMnXmC0drkOq363TAh7a5hfm5hfQpTY6vtJKA";
    private static final String USER_4_X_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZXhwIjoxNjE2NTM5MTIyLCJpYXQiOjE1MTYyMzkwMjIsInJvbGVzIjpbIkVNUExPWUVFIiwiSFIiXSwidXNlcklkIjoidWlkNCJ9.bgIbdawgynq8zVykiTpTVjn6RuQgYlXiy7wJUjAO8N8";
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

    @BeforeEach
    public void deleteTables() {
        bookingRepository.deleteAll();
        officeCapacityRepository.deleteAll();
    }

    @Test
    public void usersCanRegisterThenEntryIntoAnEmptyHouse() throws Exception {
        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"accepted\":true,\"position\":0}"));

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"position\":0}"));

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_2_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"accepted\":true,\"position\":0}"));

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_2_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"position\":0}"));

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(content().string("{\"permitted\":true}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_2_X_TOKEN))
            .andExpect(content().string("{\"permitted\":true}"))
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

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"accepted\":false,\"position\":1}"));

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"position\":1}"));

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"permitted\":false}"));
    }

    @Test
    public void exitingShouldCreatePlacesForWaitingUsers() throws Exception {
        thereIsTwoPlaceForToday();
        thereAreTwoEmployeeInTheBuilding();

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"accepted\":false,\"position\":1}"));

        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_4_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"accepted\":false,\"position\":2}"));

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"position\":1}"));

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_4_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"position\":2}"));

        mockMvc.perform(post("/exit")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_2_X_TOKEN))
            .andExpect(status().isOk());

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"position\":0}"));

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_4_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"position\":1}"));
    }

    @Test
    public void userCantExitWithoutEntry() throws Exception {
        mockMvc.perform(post("/exit")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isConflict());
    }

    private void thereIsTwoPlaceForToday() {
        officeCapacityRepository.save(OfficeCapacity.of(1, 10, 20));
    }

    private void thereAreTwoEmployeeInTheBuilding() {
        bookingRepository.save(Booking.builder().userId(USER_2_ID).entered(true).build());
        bookingRepository.save(Booking.builder().userId(USER_3_ID).entered(true).build());
    }
}

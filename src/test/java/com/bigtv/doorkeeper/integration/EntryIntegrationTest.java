package com.bigtv.doorkeeper.integration;

import com.bigtv.doorkeeper.entity.OfficeCapacity;
import com.bigtv.doorkeeper.entity.OfficeEntry;
import com.bigtv.doorkeeper.repository.OfficeCapacityRepository;
import com.bigtv.doorkeeper.repository.OfficeEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class EntryIntegrationTest {

    private static final String HEADER_TOKEN_NAME = "X-Token";

    // TODO mock jwtService
    private static final String USER_1_X_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZXhwIjoxNjE2NTM5MTIyLCJpYXQiOjE1MTYyMzkwMjIsInJvbGVzIjpbIkVNUExPWUVFIiwiSFIiXSwidXNlcklkIjoidWlkIn0.cGsC9yA77vTcSK7He0D3Vt0OBSWQvQS33AO387cdA1Q";
    private static final String USER_2_X_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlRpYmkiLCJleHAiOjE2MTY1MzkxMjIsImlhdCI6MTUxNjIzOTAyMiwicm9sZXMiOlsiRU1QTE9ZRUUiLCJIUiJdLCJ1c2VySWQiOiJ1aWQyIn0.F7yAcpaMnXmC0drkOq363TAh7a5hfm5hfQpTY6vtJKA";
    private static final String USER_1_ID = "uid";
    private static final String USER_2_ID = "uid2";
    private static final String USER_3_ID = "uid3";


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OfficeEntryRepository officeEntryRepository;

    @Autowired
    private OfficeCapacityRepository officeCapacityRepository;

    @Test
    public void userCanRegisterThenEntryThenExitIntoAnEmptyHouse() throws Exception {
        mockMvc.perform(post("/register")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"accepted\":true,\"position\":0}"));

        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
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
    public void exitingShouldCreatePlaceForWaitingUser() throws Exception {
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

        mockMvc.perform(post("/exit")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_2_X_TOKEN))
            .andExpect(status().isOk());

        mockMvc.perform(get("/status")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, USER_1_X_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"position\":0}"));

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
        officeEntryRepository.save(OfficeEntry.builder().userId(USER_2_ID).entered(true).build());
        officeEntryRepository.save(OfficeEntry.builder().userId(USER_3_ID).entered(true).build());
    }
}

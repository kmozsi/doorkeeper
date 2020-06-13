package com.bigtv.doorkeeper.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EntryIntegrationTest {

    private static final String X_TOKEN = "1000";
    private static final String HEADER_TOKEN_NAME = "X-Token";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void userCanEntryAndExit() throws Exception {
        mockMvc.perform(post("/entry")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, X_TOKEN))
            .andExpect(status().isOk());

        mockMvc.perform(post("/exit")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, X_TOKEN))
            .andExpect(status().isOk());
    }

    @Test
    public void userCantExitWithoutEntry() throws Exception {
        mockMvc.perform(post("/exit")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, X_TOKEN))
            .andExpect(status().isConflict());
    }
}

package com.karanteam.doorkeeper.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.karanteam.doorkeeper.exception.GlobalExceptionHandler;
import com.karanteam.doorkeeper.service.JwtService;
import com.karanteam.doorkeeper.service.VipService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootTest(classes = {GlobalExceptionHandler.class, VipController.class})
@AutoConfigureMockMvc
@EnableWebMvc
public class VipControllerTest {

    private static final String HEADER_TOKEN_NAME = "X-Token";
    private static final String TOKEN = "TEST";
    private static final String USER_ID = "USER_ID";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VipService vipService;

    @MockBean
    private JwtService jwtService;

    @Test
    public void addVipFailsBecauseTokenIsInvalid() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any()))
            .thenThrow(new JWTVerificationException(""));

        mockMvc.perform(post("/vip")
            .contentType(APPLICATION_JSON)
            .content("{\"userId\": \"" + USER_ID + "\"}")
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isForbidden());

        verify(vipService, never()).addVipUser(anyString());
    }

    @Test
    public void vipUserSuccessfullyAdded() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);

        mockMvc.perform(post("/vip")
            .contentType(APPLICATION_JSON)
            .content("{\"userId\": \"" + USER_ID + "\"}")
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isCreated());

        verify(vipService, times(1)).addVipUser(USER_ID);
        verifyNoMoreInteractions(vipService);
    }

    @Test
    public void deleteVipFailsBecauseTokenIsInvalid() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any()))
            .thenThrow(new JWTVerificationException(""));

        mockMvc.perform(delete("/vip/" + USER_ID)
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isForbidden());

        verify(vipService, never()).deleteVip(anyString());
    }

    @Test
    public void vipUserSuccessfullyDeleted() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);
        doReturn(true).when(vipService).deleteVip(anyString());

        mockMvc.perform(delete("/vip/" + USER_ID)
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isNoContent());

        verify(vipService, times(1)).deleteVip(USER_ID);
        verifyNoMoreInteractions(vipService);
    }

    @Test
    public void vipUserNotFound() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);
        doReturn(false).when(vipService).deleteVip(anyString());

        mockMvc.perform(delete("/vip/" + USER_ID)
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isNotFound());

        verify(vipService, times(1)).deleteVip(USER_ID);
        verifyNoMoreInteractions(vipService);
    }

    @Test
    public void getVipsFailsBecauseTokenIsInvalid() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any()))
            .thenThrow(new JWTVerificationException(""));

        mockMvc.perform(get("/vip")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isForbidden());

        verify(vipService, never()).getVipUserIds();
    }

    @Test
    public void vipUserSuccessfullyListed() throws Exception {
        when(jwtService.parseToken(matches(TOKEN), any())).thenReturn(USER_ID);

        mockMvc.perform(get("/vip")
            .contentType(APPLICATION_JSON)
            .header(HEADER_TOKEN_NAME, TOKEN))
            .andExpect(status().isOk());

        verify(vipService, times(1)).getVipUserIds();
        verifyNoMoreInteractions(vipService);
    }
}

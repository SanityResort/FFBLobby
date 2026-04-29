package com.ffblobby.controller;

import com.ffblobby.dto.JnlpResponse;
import com.ffblobby.service.JnlpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JnlpService jnlpService;

    @Test
    void getJnlp_noAuth_redirects() throws Exception {
        when(jnlpService.resolveJnlp("TEST", null, null))
                .thenReturn(new JnlpResponse("https://jnlp.example.com", "primary", "TEST"));

        mockMvc.perform(get("/api/public/jnlp?environment=TEST"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://jnlp.example.com"));
    }

    @Test
    void getJnlp_withGameName_redirects() throws Exception {
        when(jnlpService.resolveJnlp("TEST", "myGame", null))
                .thenReturn(new JnlpResponse("https://jnlp.example.com", "primary", "TEST"));

        mockMvc.perform(get("/api/public/jnlp?environment=TEST&gameName=myGame"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://jnlp.example.com"));
    }

    @Test
    void getJnlp_withGameId_redirects() throws Exception {
        when(jnlpService.resolveJnlp("TEST", null, 42L))
                .thenReturn(new JnlpResponse("https://jnlp.backend2.com", "backend2", "TEST"));

        mockMvc.perform(get("/api/public/jnlp?environment=TEST&gameId=42"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://jnlp.backend2.com"));
    }

    @Test
    void getJnlp_noPrimaryBackend_returns404() throws Exception {
        when(jnlpService.resolveJnlp("LIVE", null, null))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "No primary backend"));

        mockMvc.perform(get("/api/public/jnlp?environment=LIVE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getJnlp_missingEnvironment_returns400() throws Exception {
        mockMvc.perform(get("/api/public/jnlp"))
                .andExpect(status().isBadRequest());
    }
}

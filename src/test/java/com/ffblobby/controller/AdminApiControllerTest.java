package com.ffblobby.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffblobby.dto.BackendServerRequest;
import com.ffblobby.dto.BackendServerResponse;
import com.ffblobby.service.BackendServerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BackendServerService backendServerService;

    private BackendServerResponse sampleResponse() {
        BackendServerResponse resp = new BackendServerResponse();
        resp.setId(1L);
        resp.setName("test-backend");
        resp.setEnvironment("TEST");
        resp.setVersion("1.0.0");
        resp.setJnlpUrl("https://jnlp.example.com");
        resp.setCheckUrl("https://check.example.com");
        resp.setPrimary(true);
        return resp;
    }

    @Test
    void listBackends_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/admin/backends"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listBackends_returnsAll() throws Exception {
        when(backendServerService.listAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/admin/backends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("test-backend"))
                .andExpect(jsonPath("$[0].environment").value("TEST"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerBackend_validRequest_returns201() throws Exception {
        BackendServerRequest request = new BackendServerRequest(
                "new-backend", "TEST", "1.0.0",
                "https://jnlp.example.com", "https://check.example.com");

        when(backendServerService.register(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/admin/backends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test-backend"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerBackend_missingName_returns400() throws Exception {
        BackendServerRequest request = new BackendServerRequest(
                "", "TEST", "1.0.0", null, null);

        mockMvc.perform(post("/api/admin/backends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deregisterBackend_primary_returns409() throws Exception {
        doThrow(new ResponseStatusException(CONFLICT, "Cannot deregister primary"))
                .when(backendServerService).deregister("test-backend");

        mockMvc.perform(delete("/api/admin/backends/test-backend"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deregisterBackend_nonPrimary_returns204() throws Exception {
        doNothing().when(backendServerService).deregister("test-backend");

        mockMvc.perform(delete("/api/admin/backends/test-backend"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void setPrimary_returns200() throws Exception {
        when(backendServerService.setPrimary("test-backend")).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/admin/backends/test-backend/primary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primary").value(true));
    }
}

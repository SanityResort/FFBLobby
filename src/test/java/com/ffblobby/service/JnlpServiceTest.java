package com.ffblobby.service;

import com.ffblobby.dto.JnlpResponse;
import com.ffblobby.entity.BackendServer;
import com.ffblobby.entity.Environment;
import com.ffblobby.entity.GameAssociation;
import com.ffblobby.repository.BackendServerRepository;
import com.ffblobby.repository.GameAssociationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JnlpServiceTest {

    @Mock
    private BackendServerRepository backendServerRepository;

    @Mock
    private GameAssociationRepository gameAssociationRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private JnlpService jnlpService;

    private BackendServer primaryBackend;

    @BeforeEach
    void setUp() {
        primaryBackend = new BackendServer("primary", Environment.TEST, "1.0.0",
                "https://jnlp.primary.com", "https://check.primary.com", true);
        primaryBackend.setId(1L);
    }

    @Test
    void resolveJnlp_neitherGameNameNorGameId_returnsPrimary() {
        when(backendServerRepository.findByEnvironmentAndPrimaryTrue(Environment.TEST))
                .thenReturn(Optional.of(primaryBackend));

        JnlpResponse response = jnlpService.resolveJnlp("TEST", null, null);

        assertThat(response.getBackendName()).isEqualTo("primary");
        assertThat(response.getJnlpUrl()).isEqualTo("https://jnlp.primary.com");
        assertThat(response.getEnvironment()).isEqualTo("TEST");
    }

    @Test
    void resolveJnlp_gameNameUnknown_createAssociationAndReturnPrimary() {
        when(backendServerRepository.findByEnvironmentAndPrimaryTrue(Environment.TEST))
                .thenReturn(Optional.of(primaryBackend));
        when(gameAssociationRepository.findByGameNameAndEnvironment("game1", Environment.TEST))
                .thenReturn(Optional.empty());
        when(gameAssociationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JnlpResponse response = jnlpService.resolveJnlp("TEST", "game1", null);

        assertThat(response.getBackendName()).isEqualTo("primary");
        verify(gameAssociationRepository).save(any(GameAssociation.class));
    }

    @Test
    void resolveJnlp_gameNameKnown_deleteAssociationAndReturnAssociatedBackend() {
        BackendServer assocBackend = new BackendServer("assoc-backend", Environment.TEST,
                "1.0.0", "https://jnlp.assoc.com", null, false);
        assocBackend.setId(2L);
        GameAssociation assoc = new GameAssociation("game1", assocBackend, Environment.TEST);

        when(gameAssociationRepository.findByGameNameAndEnvironment("game1", Environment.TEST))
                .thenReturn(Optional.of(assoc));

        JnlpResponse response = jnlpService.resolveJnlp("TEST", "game1", null);

        assertThat(response.getBackendName()).isEqualTo("assoc-backend");
        assertThat(response.getJnlpUrl()).isEqualTo("https://jnlp.assoc.com");
        verify(gameAssociationRepository).delete(assoc);
    }

    @Test
    void resolveJnlp_gameIdFound_returnsMatchingBackend() {
        BackendServer backend2 = new BackendServer("backend2", Environment.TEST, "1.0.0",
                "https://jnlp.backend2.com", "https://check.backend2.com", false);
        backend2.setId(2L);

        when(backendServerRepository.findByEnvironment(Environment.TEST))
                .thenReturn(List.of(primaryBackend, backend2));
        when(restTemplate.getForEntity(eq("https://check.primary.com?gameId=42"), eq(String.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        when(restTemplate.getForEntity(eq("https://check.backend2.com?gameId=42"), eq(String.class)))
                .thenReturn(ResponseEntity.ok("found"));

        JnlpResponse response = jnlpService.resolveJnlp("TEST", null, 42L);

        assertThat(response.getBackendName()).isEqualTo("backend2");
    }

    @Test
    void resolveJnlp_gameIdNotFound_returnsPrimary() {
        when(backendServerRepository.findByEnvironment(Environment.TEST))
                .thenReturn(List.of(primaryBackend));
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        when(backendServerRepository.findByEnvironmentAndPrimaryTrue(Environment.TEST))
                .thenReturn(Optional.of(primaryBackend));

        JnlpResponse response = jnlpService.resolveJnlp("TEST", null, 99L);

        assertThat(response.getBackendName()).isEqualTo("primary");
    }

    @Test
    void resolveJnlp_invalidEnvironment_throwsBadRequest() {
        assertThatThrownBy(() -> jnlpService.resolveJnlp("INVALID", null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }
}

package com.ffblobby.service;

import com.ffblobby.dto.BackendServerRequest;
import com.ffblobby.dto.BackendServerResponse;
import com.ffblobby.entity.BackendServer;
import com.ffblobby.entity.Environment;
import com.ffblobby.repository.BackendServerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackendServerServiceTest {

    @Mock
    private BackendServerRepository backendServerRepository;

    @InjectMocks
    private BackendServerService backendServerService;

    private BackendServer server;

    @BeforeEach
    void setUp() {
        server = new BackendServer("test-backend", Environment.TEST, "1.0.0",
                "https://jnlp.example.com", "https://check.example.com", false);
        server.setId(1L);
    }

    @Test
    void register_firstForEnvironment_setsPrimary() {
        when(backendServerRepository.existsByEnvironment(Environment.TEST)).thenReturn(false);
        when(backendServerRepository.save(any())).thenAnswer(inv -> {
            BackendServer s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        BackendServerRequest request = new BackendServerRequest(
                "test-backend", "TEST", "1.0.0",
                "https://jnlp.example.com", "https://check.example.com");

        BackendServerResponse response = backendServerService.register(request);

        assertThat(response.isPrimary()).isTrue();
        assertThat(response.getName()).isEqualTo("test-backend");
    }

    @Test
    void register_notFirstForEnvironment_notPrimary() {
        when(backendServerRepository.existsByEnvironment(Environment.TEST)).thenReturn(true);
        when(backendServerRepository.save(any())).thenAnswer(inv -> {
            BackendServer s = inv.getArgument(0);
            s.setId(2L);
            return s;
        });

        BackendServerRequest request = new BackendServerRequest(
                "second-backend", "TEST", "1.0.0", null, null);

        BackendServerResponse response = backendServerService.register(request);

        assertThat(response.isPrimary()).isFalse();
    }

    @Test
    void deregister_primaryBackend_throwsConflict() {
        server.setPrimary(true);
        when(backendServerRepository.findByName("test-backend")).thenReturn(Optional.of(server));

        assertThatThrownBy(() -> backendServerService.deregister("test-backend"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }

    @Test
    void deregister_nonPrimaryBackend_succeeds() {
        server.setPrimary(false);
        when(backendServerRepository.findByName("test-backend")).thenReturn(Optional.of(server));

        assertThatNoException().isThrownBy(() -> backendServerService.deregister("test-backend"));
        verify(backendServerRepository).delete(server);
    }

    @Test
    void deregister_notFound_throwsNotFound() {
        when(backendServerRepository.findByName("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> backendServerService.deregister("unknown"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void listAll_returnsAll() {
        when(backendServerRepository.findAll()).thenReturn(List.of(server));

        List<BackendServerResponse> result = backendServerService.listAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("test-backend");
    }

    @Test
    void setPrimary_clearsPreviousPrimaryAndSetsNew() {
        BackendServer oldPrimary = new BackendServer("old-primary", Environment.TEST,
                "0.9.0", null, null, true);
        oldPrimary.setId(2L);

        when(backendServerRepository.findByName("test-backend")).thenReturn(Optional.of(server));
        when(backendServerRepository.findByEnvironmentAndPrimaryTrue(Environment.TEST))
                .thenReturn(Optional.of(oldPrimary));
        when(backendServerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BackendServerResponse result = backendServerService.setPrimary("test-backend");

        assertThat(result.isPrimary()).isTrue();
        assertThat(oldPrimary.isPrimary()).isFalse();
    }
}

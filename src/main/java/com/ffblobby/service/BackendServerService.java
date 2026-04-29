package com.ffblobby.service;

import com.ffblobby.dto.BackendServerRequest;
import com.ffblobby.dto.BackendServerResponse;
import com.ffblobby.entity.BackendServer;
import com.ffblobby.entity.Environment;
import com.ffblobby.repository.BackendServerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BackendServerService {

    private final BackendServerRepository backendServerRepository;

    public BackendServerService(BackendServerRepository backendServerRepository) {
        this.backendServerRepository = backendServerRepository;
    }

    @Transactional
    public BackendServerResponse register(BackendServerRequest request) {
        Environment env = parseEnvironment(request.getEnvironment());
        boolean isFirst = !backendServerRepository.existsByEnvironment(env);

        BackendServer server = new BackendServer(
                request.getName(),
                env,
                request.getVersion(),
                request.getJnlpUrl(),
                request.getCheckUrl(),
                isFirst
        );

        BackendServer saved = backendServerRepository.save(server);
        return BackendServerResponse.from(saved);
    }

    @Transactional
    public void deregister(String name) {
        BackendServer server = backendServerRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Backend not found: " + name));

        if (server.isPrimary()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot deregister primary backend: " + name);
        }

        backendServerRepository.delete(server);
    }

    @Transactional(readOnly = true)
    public List<BackendServerResponse> listAll() {
        return backendServerRepository.findAll().stream()
                .map(BackendServerResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public BackendServerResponse setPrimary(String name) {
        BackendServer target = backendServerRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Backend not found: " + name));

        // Clear existing primary for this environment
        backendServerRepository.findByEnvironmentAndPrimaryTrue(target.getEnvironment())
                .ifPresent(current -> {
                    current.setPrimary(false);
                    backendServerRepository.save(current);
                });

        target.setPrimary(true);
        BackendServer saved = backendServerRepository.save(target);
        return BackendServerResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<BackendServer> findAll() {
        return backendServerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public BackendServer getPrimaryForEnvironment(Environment env) {
        return backendServerRepository.findByEnvironmentAndPrimaryTrue(env)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No primary backend for environment: " + env));
    }

    private Environment parseEnvironment(String env) {
        try {
            return Environment.valueOf(env.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid environment: " + env);
        }
    }
}

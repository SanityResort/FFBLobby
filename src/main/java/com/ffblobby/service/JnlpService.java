package com.ffblobby.service;

import com.ffblobby.dto.JnlpResponse;
import com.ffblobby.entity.BackendServer;
import com.ffblobby.entity.Environment;
import com.ffblobby.entity.GameAssociation;
import com.ffblobby.repository.BackendServerRepository;
import com.ffblobby.repository.GameAssociationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class JnlpService {

    private static final Logger log = LoggerFactory.getLogger(JnlpService.class);

    private final BackendServerRepository backendServerRepository;
    private final GameAssociationRepository gameAssociationRepository;
    private final RestTemplate restTemplate;

    public JnlpService(BackendServerRepository backendServerRepository,
                       GameAssociationRepository gameAssociationRepository,
                       RestTemplate restTemplate) {
        this.backendServerRepository = backendServerRepository;
        this.gameAssociationRepository = gameAssociationRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public JnlpResponse resolveJnlp(String environmentStr, String gameName, Long gameId) {
        Environment env;
        try {
            env = Environment.valueOf(environmentStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid environment: " + environmentStr);
        }

        BackendServer backend;

        if (gameName != null && !gameName.isBlank()) {
            backend = resolveByGameName(gameName, env);
        } else if (gameId != null) {
            backend = resolveByGameId(gameId, env);
        } else {
            backend = getPrimary(env);
        }

        return new JnlpResponse(backend.getJnlpUrl(), backend.getName(), env.name());
    }

    private BackendServer resolveByGameName(String gameName, Environment env) {
        Optional<GameAssociation> assocOpt =
                gameAssociationRepository.findByGameNameAndEnvironment(gameName, env);

        if (assocOpt.isPresent()) {
            GameAssociation assoc = assocOpt.get();
            BackendServer backend = assoc.getBackendServer();
            gameAssociationRepository.delete(assoc);
            return backend;
        } else {
            BackendServer primary = getPrimary(env);
            GameAssociation newAssoc = new GameAssociation(gameName, primary, env);
            gameAssociationRepository.save(newAssoc);
            return primary;
        }
    }

    private BackendServer resolveByGameId(Long gameId, Environment env) {
        List<BackendServer> backends = backendServerRepository.findByEnvironment(env);

        for (BackendServer backend : backends) {
            if (backend.getCheckUrl() == null || backend.getCheckUrl().isBlank()) {
                continue;
            }
            String url = backend.getCheckUrl() + "?gameId=" + gameId;
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    return backend;
                }
            } catch (Exception e) {
                log.debug("Backend {} check failed for gameId {}: {}", backend.getName(), gameId, e.getMessage());
            }
        }

        return getPrimary(env);
    }

    private BackendServer getPrimary(Environment env) {
        return backendServerRepository.findByEnvironmentAndPrimaryTrue(env)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No primary backend found for environment: " + env));
    }
}

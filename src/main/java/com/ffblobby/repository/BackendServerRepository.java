package com.ffblobby.repository;

import com.ffblobby.entity.BackendServer;
import com.ffblobby.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BackendServerRepository extends JpaRepository<BackendServer, Long> {
    Optional<BackendServer> findByName(String name);
    List<BackendServer> findByEnvironment(Environment environment);
    Optional<BackendServer> findByEnvironmentAndPrimaryTrue(Environment environment);
    boolean existsByEnvironment(Environment environment);
}

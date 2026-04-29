package com.ffblobby.repository;

import com.ffblobby.entity.Environment;
import com.ffblobby.entity.GameAssociation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameAssociationRepository extends JpaRepository<GameAssociation, Long> {
    Optional<GameAssociation> findByGameNameAndEnvironment(String gameName, Environment environment);
}

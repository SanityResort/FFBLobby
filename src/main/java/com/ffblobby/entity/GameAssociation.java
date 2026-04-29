package com.ffblobby.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "game_association")
public class GameAssociation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String gameName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "backend_server_id", nullable = false)
    private BackendServer backendServer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Environment environment;

    public GameAssociation() {}

    public GameAssociation(String gameName, BackendServer backendServer, Environment environment) {
        this.gameName = gameName;
        this.backendServer = backendServer;
        this.environment = environment;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public BackendServer getBackendServer() { return backendServer; }
    public void setBackendServer(BackendServer backendServer) { this.backendServer = backendServer; }

    public Environment getEnvironment() { return environment; }
    public void setEnvironment(Environment environment) { this.environment = environment; }
}

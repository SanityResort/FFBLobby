package com.ffblobby.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "backend_server")
public class BackendServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Environment environment;

    private String version;

    private String jnlpUrl;

    private String checkUrl;

    @Column(nullable = false)
    private boolean primary = false;

    public BackendServer() {}

    public BackendServer(String name, Environment environment, String version,
                         String jnlpUrl, String checkUrl, boolean primary) {
        this.name = name;
        this.environment = environment;
        this.version = version;
        this.jnlpUrl = jnlpUrl;
        this.checkUrl = checkUrl;
        this.primary = primary;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Environment getEnvironment() { return environment; }
    public void setEnvironment(Environment environment) { this.environment = environment; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getJnlpUrl() { return jnlpUrl; }
    public void setJnlpUrl(String jnlpUrl) { this.jnlpUrl = jnlpUrl; }

    public String getCheckUrl() { return checkUrl; }
    public void setCheckUrl(String checkUrl) { this.checkUrl = checkUrl; }

    public boolean isPrimary() { return primary; }
    public void setPrimary(boolean primary) { this.primary = primary; }
}

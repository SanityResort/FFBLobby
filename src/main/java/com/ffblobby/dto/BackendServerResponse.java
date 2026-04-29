package com.ffblobby.dto;

import com.ffblobby.entity.BackendServer;

public class BackendServerResponse {

    private Long id;
    private String name;
    private String environment;
    private String version;
    private String jnlpUrl;
    private String checkUrl;
    private boolean primary;

    public BackendServerResponse() {}

    public static BackendServerResponse from(BackendServer server) {
        BackendServerResponse resp = new BackendServerResponse();
        resp.id = server.getId();
        resp.name = server.getName();
        resp.environment = server.getEnvironment() != null ? server.getEnvironment().name() : null;
        resp.version = server.getVersion();
        resp.jnlpUrl = server.getJnlpUrl();
        resp.checkUrl = server.getCheckUrl();
        resp.primary = server.isPrimary();
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getJnlpUrl() { return jnlpUrl; }
    public void setJnlpUrl(String jnlpUrl) { this.jnlpUrl = jnlpUrl; }

    public String getCheckUrl() { return checkUrl; }
    public void setCheckUrl(String checkUrl) { this.checkUrl = checkUrl; }

    public boolean isPrimary() { return primary; }
    public void setPrimary(boolean primary) { this.primary = primary; }
}

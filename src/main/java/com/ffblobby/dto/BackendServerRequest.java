package com.ffblobby.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class BackendServerRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Environment is required")
    private String environment;

    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "Version must be in semver format (e.g. 1.0.0)")
    private String version;

    private String jnlpUrl;
    private String checkUrl;

    public BackendServerRequest() {}

    public BackendServerRequest(String name, String environment, String version,
                                 String jnlpUrl, String checkUrl) {
        this.name = name;
        this.environment = environment;
        this.version = version;
        this.jnlpUrl = jnlpUrl;
        this.checkUrl = checkUrl;
    }

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
}

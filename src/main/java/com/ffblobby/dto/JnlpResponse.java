package com.ffblobby.dto;

public class JnlpResponse {

    private String jnlpUrl;
    private String backendName;
    private String environment;

    public JnlpResponse() {}

    public JnlpResponse(String jnlpUrl, String backendName, String environment) {
        this.jnlpUrl = jnlpUrl;
        this.backendName = backendName;
        this.environment = environment;
    }

    public String getJnlpUrl() { return jnlpUrl; }
    public void setJnlpUrl(String jnlpUrl) { this.jnlpUrl = jnlpUrl; }

    public String getBackendName() { return backendName; }
    public void setBackendName(String backendName) { this.backendName = backendName; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
}

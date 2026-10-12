package com.sentineltrace.dto;

public class SimulationRequest {

    private String scenario;
    private Integer count;
    private String sourceIp;
    private String username;

    public SimulationRequest() {
    }

    public SimulationRequest(String scenario, Integer count, String sourceIp, String username) {
        this.scenario = scenario;
        this.count = count;
        this.sourceIp = sourceIp;
        this.username = username;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

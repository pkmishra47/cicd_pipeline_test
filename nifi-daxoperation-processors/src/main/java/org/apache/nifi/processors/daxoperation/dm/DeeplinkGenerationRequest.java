package org.apache.nifi.processors.daxoperation.dm;

public class DeeplinkGenerationRequest {
    private String ttl;
    private DeeplinkRequestData data;

    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }

    public DeeplinkRequestData getData() {
        return data;
    }

    public void setData(DeeplinkRequestData data) {
        this.data = data;
    }
}

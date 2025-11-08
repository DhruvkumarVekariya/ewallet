package com.hcl.ewallet.gateway;



public class GatewayResponse {
    private boolean success;
    private String providerReference;
    private String message;

    public GatewayResponse() {}

    public GatewayResponse(boolean success, String providerReference, String message) {
        this.success = success;
        this.providerReference = providerReference;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public void setProviderReference(String providerReference) {
        this.providerReference = providerReference;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

package com.mastercard.gateway.android.sdk2;

import com.mastercard.gateway.android.sdk2.api.ErrorResponse;

public class GatewayException extends Exception {

    int statusCode;
    ErrorResponse error;


    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public ErrorResponse getErrorResponse() {
        return error;
    }

    public void setErrorResponse(ErrorResponse error) {
        this.error = error;
    }
}

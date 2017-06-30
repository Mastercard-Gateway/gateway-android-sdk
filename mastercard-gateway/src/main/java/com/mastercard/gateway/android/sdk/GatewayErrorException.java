
package com.mastercard.gateway.android.sdk;


public class GatewayErrorException
    extends Exception
{

    private final ErrorResponse response;

    public GatewayErrorException(ErrorResponse response) {
        this.response = response;
    }

    public ErrorResponse getResponse() {
        return this.response;
    }

}

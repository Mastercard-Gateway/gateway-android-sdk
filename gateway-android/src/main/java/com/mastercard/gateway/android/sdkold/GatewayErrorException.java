
package com.mastercard.gateway.android.sdkold;


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

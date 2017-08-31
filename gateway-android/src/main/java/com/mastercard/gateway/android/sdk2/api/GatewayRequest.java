package com.mastercard.gateway.android.sdk2.api;


public interface GatewayRequest<T extends GatewayResponse> {

    HttpRequest buildHttpRequest();

    Class<T> getResponseClass();
}

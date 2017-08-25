package com.mastercard.gateway.android.sdk2.api;


public interface GatewayCallback<T extends GatewayResponse> {
    void onSuccess(T response);
    void onError(Throwable throwable);
}

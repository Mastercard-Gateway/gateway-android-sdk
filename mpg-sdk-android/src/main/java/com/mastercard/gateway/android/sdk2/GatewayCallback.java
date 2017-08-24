package com.mastercard.gateway.android.sdk2;


public interface GatewayCallback<T extends GatewayResponse> {
    void onSuccess(T response);
    void onError(Throwable throwable);
}

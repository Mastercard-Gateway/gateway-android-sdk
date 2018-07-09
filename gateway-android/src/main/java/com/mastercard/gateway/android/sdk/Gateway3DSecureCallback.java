package com.mastercard.gateway.android.sdk;

public interface Gateway3DSecureCallback {

    /**
     *
     * @param response
     */
    void on3DSecureComplete(GatewayMap response);

    /**
     *
     */
    void on3DSecureCancel();
}

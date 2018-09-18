package com.mastercard.gateway.android.sdk;

public interface Gateway3DSecureCallback {

    /**
     * Callback method when webview-based 3DS authentication is complete
     *
     * @param response A response map
     */
    void on3DSecureComplete(GatewayMap response);

    /**
     * Callback when a user cancels the 3DS authentication flow. (typically on back press)
     */
    void on3DSecureCancel();
}

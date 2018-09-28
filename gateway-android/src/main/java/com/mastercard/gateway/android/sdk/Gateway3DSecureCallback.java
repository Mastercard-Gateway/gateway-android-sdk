package com.mastercard.gateway.android.sdk;

public interface Gateway3DSecureCallback {

    /**
     * Callback method when webview-based 3DS authentication is complete
     *
     * @param acsResult A response map containing the ACS result
     */
    void on3DSecureComplete(GatewayMap acsResult);

    /**
     * Callback when a user cancels the 3DS authentication flow. (typically on back press)
     */
    void on3DSecureCancel();
}

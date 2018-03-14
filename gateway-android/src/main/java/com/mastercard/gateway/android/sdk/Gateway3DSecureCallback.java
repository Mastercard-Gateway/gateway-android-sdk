package com.mastercard.gateway.android.sdk;

public interface Gateway3DSecureCallback {
    void on3DSecureCancel();

    void on3DSecureError(String errorMessage);

    void on3DSecureComplete(String summaryStatus, String threeDSecureId);
}

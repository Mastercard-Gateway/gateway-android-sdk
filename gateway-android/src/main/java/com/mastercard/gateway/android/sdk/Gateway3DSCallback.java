package com.mastercard.gateway.android.sdk;

public interface Gateway3DSCallback {
    void on3DSecureCancel();
    void on3DSecureError(SummaryStatus summaryStatus);
    void on3DSecureSuccess(String threeDSecureId, SummaryStatus summaryStatus);
}

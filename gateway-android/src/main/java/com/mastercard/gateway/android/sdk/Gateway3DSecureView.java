package com.mastercard.gateway.android.sdk;


interface Gateway3DSecureView {

    String getDefaultTitle();
    String getExtraTitle();
    String getExtraHtml();
    void setToolbarTitle(String title);
    void setWebViewHtml(String html);
    void loadWebViewUrl(String url);
    void intentToEmail(String url);
    void error(int errorResId);
    void success(String summaryStatus, String threeDSecureId);
    void cancel();
}

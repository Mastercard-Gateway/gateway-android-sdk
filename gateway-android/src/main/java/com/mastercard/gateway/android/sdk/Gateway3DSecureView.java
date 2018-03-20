package com.mastercard.gateway.android.sdk;


import android.net.Uri;

interface Gateway3DSecureView {

    String getDefaultTitle();
    String getExtraTitle();
    String getExtraHtml();
    void setToolbarTitle(String title);
    void setWebViewHtml(String html);
    void loadWebViewUrl(Uri uri);
    void intentToEmail(Uri uri);
    void error(int errorResId);
    void success(String summaryStatus, String threeDSecureId);
    void cancel();
}

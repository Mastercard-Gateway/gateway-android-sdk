package com.mastercard.gateway.android.sdk;


import android.net.Uri;

import java.util.Map;

interface Gateway3DSecureView {

    String getDefaultTitle();
    String getExtraTitle();
    String getExtraHtml();
    void setToolbarTitle(String title);
    void setWebViewHtml(String html);
    void loadWebViewUrl(Uri uri);
    void intentToEmail(Uri uri);
    void complete(Map<String, String> data);
//    void error(int errorResId);
//    void success(String summaryStatus, String threeDSecureId);
    void cancel();
}

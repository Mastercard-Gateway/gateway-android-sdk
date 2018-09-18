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
    void complete(String acsResult);
    void cancel();
}

package com.mastercard.gateway.android.sdk;

import android.net.Uri;

class Gateway3DSecurePresenter {

    static final String REDIRECT_SCHEME = "gatewaysdk:";
    static final String QUERY_3DSECURE_ID = "3DSecureId";
    static final String QUERY_SUMMARY_STATUS = "summaryStatus";

    Gateway3DSecureView view;

    void attachView(Gateway3DSecureView view) {
        this.view = view;

        // init html
        String extraHtml = view.getExtraHtml();
        if (extraHtml == null) {
            view.cancel();
            return;
        } else {
            view.setWebViewHtml(extraHtml);
        }

        // init title
        String defaultTitle = view.getDefaultTitle();
        String extraTitle = view.getExtraTitle();
        view.setToolbarTitle(extraTitle != null ? extraTitle : defaultTitle);
    }

    void detachView() {
        view = null;
    }

    void webViewUrlChanges(String url) {
        if (url.startsWith(REDIRECT_SCHEME)) {
            handle3DSecureResult(url);
        } else if (url.startsWith("mailto:")) {
            view.intentToEmail(url);
        } else {
            view.loadWebViewUrl(url);
        }
    }

    void handle3DSecureResult(String url) {
        String summaryStatus = null;
        String threeDSecureId = null;

        // parse response info from url redirect
        try {
            Uri uri = Uri.parse(url);
            summaryStatus = uri.getQueryParameter(QUERY_SUMMARY_STATUS);
            threeDSecureId = uri.getQueryParameter(QUERY_3DSECURE_ID);
        } catch (Exception e) {
            // unable to parse or find result data
        }

        // check that we got the correct data back
        if (summaryStatus == null) {
            view.error(R.string.gateway_error_missing_summary_status);
        } else if (threeDSecureId == null) {
            view.error(R.string.gateway_error_missing_3d_secure_id);
        } else {
            view.success(summaryStatus, threeDSecureId);
        }
    }
}

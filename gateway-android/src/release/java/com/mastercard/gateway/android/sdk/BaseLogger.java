package com.mastercard.gateway.android.sdk;


import javax.net.ssl.HttpsURLConnection;


class BaseLogger implements Logger {

    @Override
    public void logRequest(HttpsURLConnection c, String data) {
        // no-op
    }

    @Override
    public void logResponse(HttpsURLConnection c, String data) {
        // no-op
    }

    @Override
    public void logDebug(String message) {
        // no-op
    }
}

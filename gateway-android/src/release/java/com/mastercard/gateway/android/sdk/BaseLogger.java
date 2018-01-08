package com.mastercard.gateway.android.sdk;


import java.net.HttpURLConnection;


class BaseLogger implements Logger {

    @Override
    public void logRequest(HttpURLConnection c, String data) {
        // no-op
    }

    @Override
    public void logResponse(HttpURLConnection c, String data) {
        // no-op
    }

    @Override
    public void logDebug(String message) {
        // no-op
    }
}

package com.mastercard.gateway.android.sdk;


import com.mastercard.gateway.android.sdk.api.HttpResponse;

import java.net.HttpURLConnection;


class BaseLogger implements Logger {

    public void logRequest(HttpURLConnection c, String data) {
        // no-op
    }

    public void logResponse(HttpResponse response) {
        // no-op
    }
}

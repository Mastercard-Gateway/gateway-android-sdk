package com.mastercard.gateway.android.sdk;

import com.mastercard.gateway.android.sdk.api.HttpResponse;

import java.net.HttpURLConnection;

interface Logger {
    void logRequest(HttpURLConnection c, String data);
    void logResponse(HttpResponse response);
}

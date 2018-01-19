package com.mastercard.gateway.android.sdk;


import android.util.Log;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Set;

class BaseLogger implements Logger {

    @Override
    public void logRequest(HttpURLConnection c, String data) {
        String log = "REQUEST: " + c.getRequestMethod() + " " + c.getURL().toString();

        if (data != null) {
            log += "\n-- Data: " + data;
        }

        // log request headers
        Map<String, List<String>> properties = c.getRequestProperties();
        Set<String> keys = properties.keySet();
        for (String key : keys) {
            List<String> values = properties.get(key);
            for (String value : values) {
                log += "\n-- " + key + ": " + value;
            }
        }

        String[] parts = log.split("\n");
        for (String part : parts) {
            logDebug(part);
        }
    }

    @Override
    public void logResponse(HttpURLConnection c, String data) {
        String log = "RESPONSE: ";

        // log response headers
        Map<String, List<String>> headers = c.getHeaderFields();
        Set<String> keys = headers.keySet();

        int i = 0;
        for (String key : keys) {
            List<String> values = headers.get(key);
            for (String value : values) {
                if (i == 0 && key == null) {
                    log += value;

                    if (data != null && data.length() > 0) {
                        log += "\n-- Data: " + data;
                    }
                } else {
                    log += "\n-- " + (key == null ? "" : key + ": ") + value;
                }
                i++;
            }
        }

        String[] parts = log.split("\n");
        for (String part : parts) {
            logDebug(part);
        }
    }

    @Override
    public void logDebug(String message) {
        Log.d(Gateway.class.getSimpleName(), message);
    }
}

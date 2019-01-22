package com.mastercard.gateway.android.sdk;

import java.util.HashMap;
import java.util.Map;

class GatewayRequest {

    String url;
    Gateway.Method method;

    Map<String, String> extraHeaders = new HashMap<>();

    GatewayMap payload;
}

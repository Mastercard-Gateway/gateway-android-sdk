package com.mastercard.gateway.android.sdk.api;


import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

public interface GatewayRequest<T extends GatewayResponse> {

    HttpRequest buildHttpRequest();

    TypeAdapter<T> getResponseTypeAdapter(Gson gson);
}

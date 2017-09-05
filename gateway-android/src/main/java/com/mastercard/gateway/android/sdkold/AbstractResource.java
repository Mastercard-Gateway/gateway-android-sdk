
package com.mastercard.gateway.android.sdkold;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class AbstractResource {

    private final Comms comms;
    private Gson gson = new GsonBuilder().create();
    private String baseURL;

    public AbstractResource(Comms comms, String urlBase) {
        this.comms = comms;
        this.baseURL = urlBase;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    protected<T extends Payload >T send(String method, String path, Payload request, Class<T> responseType)
        throws CommsException, GatewayErrorException
    {
        String jsonRequest = gson.toJson(request);
        String jsonResponse = comms.send(method, (baseURL + path), jsonRequest);
        ErrorResponse error = gson.fromJson(jsonResponse, ErrorResponse.class);
        if ((error!= null)&&(ErrorResponse.Result.ERROR == error.result())) {
            throw new GatewayErrorException(error);
        }
        if (responseType!= null) {
            return gson.fromJson(jsonResponse, responseType);
        } else {
            return null;
        }
    }

}

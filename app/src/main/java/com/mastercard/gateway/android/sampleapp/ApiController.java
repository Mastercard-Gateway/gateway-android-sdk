package com.mastercard.gateway.android.sampleapp;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mastercard.gateway.android.sdk.CommsException;
import com.mastercard.gateway.android.sdk.CommsTimeoutException;
import com.mastercard.gateway.android.sdk.GatewayComms;
import com.mastercard.gateway.android.sdk.merchant.session.WithSessionResource;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * ApiController object used to send create and update session requests. Conforms to the singleton
 * pattern.
 */
public class ApiController {
    private static final ApiController INSTANCE = new ApiController();

    private static final Gson GSON = new GsonBuilder().create();

    protected GatewayComms gatewayComms = new GatewayComms();
    protected MerchantBackendComms merchantComms = new MerchantBackendComms();
    protected WithSessionResource session;
    protected String username;
    protected String password;

    String herokuUrl;

    interface CreateSessionCallback {
        void onSuccess(String sessionId);

        void onError(Throwable throwable);
    }

    interface CompleteSessionCallback {
        void onSuccess(String result);

        void onError(Throwable throwable);
    }

    private ApiController() {
    }

    public static ApiController getInstance() {
        return INSTANCE;
    }

    public void setAuthDetails(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setHerokuUrl(String url) {
        herokuUrl = url;
    }

    public void createSession(final CreateSessionCallback callback) {
        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (callback != null) {
                    if (message.obj instanceof Throwable) {
                        callback.onError((Throwable) message.obj);
                    } else {
                        callback.onSuccess((String) message.obj);
                    }
                }
                return true;
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message m = handler.obtainMessage();
                try {
                    m.obj = executeCreateSession();
                } catch (Exception e) {
                    m.obj = e;
                }
                handler.sendMessage(m);
            }
        }).start();
    }

    public void completeSession(final String sessionId, final String amount, final String currency, final CompleteSessionCallback callback) {
        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (callback != null) {
                    if (message.obj instanceof Throwable) {
                        callback.onError((Throwable) message.obj);
                    } else {
                        callback.onSuccess((String) message.obj);
                    }
                }
                return true;
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message m = handler.obtainMessage();
                try {
                    m.obj = executeCompleteSession(sessionId, amount, currency);
                } catch (Exception e) {
                    m.obj = e;
                }
                handler.sendMessage(m);
            }
        }).start();
    }

    String executeCreateSession() throws Exception {
        String jsonResponse = merchantComms.doJsonRequest(new URL(herokuUrl + "/session.php"), "", "POST", null, null, HttpsURLConnection.HTTP_OK);

        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = GSON.fromJson(jsonResponse, type);

        if (!map.containsKey("result") || "SUCCESS".equalsIgnoreCase((String) map.get("result"))) {
            throw new RuntimeException("Create session result: " + map.get("result"));
        }

        String sessionId = ((Map<String, String>) map.get("session")).get("id");
        Log.i("createSession", "Created session with ID: " + sessionId);
        return sessionId;
    }

    String executeCompleteSession(String sessionId, String amount, String currency) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("sessionId", sessionId);
        json.addProperty("amount", amount);
        json.addProperty("currency", currency);
        String jsonString = GSON.toJson(json);

        String jsonResponse = merchantComms.doJsonRequest(new URL(herokuUrl + "/session.php"), jsonString, "PUT", null, null, HttpsURLConnection.HTTP_OK);

        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = GSON.fromJson(jsonResponse, type);

        if (!map.containsKey("result") || "SUCCESS".equalsIgnoreCase((String) map.get("result"))) {
            throw new RuntimeException("Payment result: " + map.get("result"));
        }

        return (String) map.get("result");
    }

//    public MerchantSimulatorResponse createSession(String productId, String behaviour,
//                                                   int timeoutMillis) {
//
//        merchantComms.setTimeoutMilliseconds(timeoutMillis);
//
//        String jsonRequest = " { \"productId\": \"" + productId + "\" }";
//
//        String jsonResponse = null;
//        String address = actualUrl(BuildConfig.CREATE_SESSION_URL, behaviour);
//
//        Log.d("createSession", "Address: " + address);
//
//        try {
//            jsonResponse = merchantComms.doJsonRequest(new URL(address),
//                    jsonRequest, "PUT", username, password, HttpsURLConnection.HTTP_OK);
//        } catch (MalformedURLException e) {
//            Log.e("createSession", "Bad host name");
//        } catch (CommsTimeoutException e) {
//            Log.e("createSession", "Timeout");
//            MerchantSimulatorResponse response = new MerchantSimulatorResponse();
//            response.status = "TIMEOUT";
//            return response;
//        } catch (CommsException e) {
//            Log.e("createSession", e.getMessage(), e.getCause());
//            return null;
//        }
//
//        Log.d("createSession", "Response: " + jsonResponse);
//
//        try {
//            MerchantSimulatorResponse response =
//                    GSON.fromJson(jsonResponse, MerchantSimulatorResponse.class);
//
//            if ("SUCCESS".equals(response.status)) {
//                session = new WithSessionResource(
//                        gatewayComms, BuildConfig.MERCHANT_ID, response.sessionID);
//
//                session.setBaseURL(BuildConfig.GATEWAY_BASE_URL);
//                Log.i("createSession", "Session created with id " + session.getSessionId());
//            }
//
//            return response;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }

    public MerchantSimulatorResponse completeSession(String productId, String price,
                                                     String behaviour, int timeoutMillis) {

        merchantComms.setTimeoutMilliseconds(timeoutMillis);

        String jsonRequest = " {" +
                "\"productId\": \"" + productId + "\", " +
                "\"sessionId\": \"" + session.getSessionId() + "\", " +
                "\"amount\": \"" + price + '"' +
                " }";

        String jsonResponse;
        String address = actualUrl(BuildConfig.COMPLETE_SESSION_URL, behaviour);

        Log.d("completeSession", "Address: " + address);
        Log.d("completeSession", "Request: " + jsonRequest);

        try {
            jsonResponse = merchantComms.doJsonRequest(new URL(address),
                    jsonRequest, "POST", username, password, HttpsURLConnection.HTTP_OK);
        } catch (MalformedURLException e) {
            Log.e("completeSession", "Bad host name");
            return null;
        } catch (CommsTimeoutException e) {
            Log.e("completeSession", "Timeout");
            MerchantSimulatorResponse response = new MerchantSimulatorResponse();
            response.status = "TIMEOUT";
            return response;
        } catch (CommsException e) {
            Log.e("completeSession", e.getMessage(), e.getCause());
            return null;
        }

        Log.d("completeSession", "Response: " + jsonResponse);

        try {
            MerchantSimulatorResponse response =
                    GSON.fromJson(jsonResponse, MerchantSimulatorResponse.class);

            if ("SUCCESS".equals(response.status)) {
                session = null;
                Log.i("completeSession", "Pay successful; session terminated");
            }

            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String actualUrl(String realUrl, String behaviour) {
        switch (behaviour) {
            case "succeed":
                return realUrl;
            case "timeout":
                return BuildConfig.TIMEOUT_URL;
            default:
                return BuildConfig.NOT_FOUND_URL;
        }
    }
}


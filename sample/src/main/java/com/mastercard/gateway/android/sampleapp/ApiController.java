/*
 * Copyright (c) 2016 Mastercard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mastercard.gateway.android.sampleapp;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.BoolRes;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mastercard.gateway.android.sdk.GatewayMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import static android.text.TextUtils.isEmpty;

/**
 * ApiController object used to send create and update session requests. Conforms to the singleton
 * pattern.
 */
public class ApiController {

    private static final ApiController INSTANCE = new ApiController();

    static final Gson GSON = new GsonBuilder().create();

    String merchantServerUrl;


    interface CreateSessionCallback {
        void onSuccess(String sessionId, String apiVersion);

        void onError(Throwable throwable);
    }

    interface Check3DSecureEnrollmentCallback {
        void onSuccess(GatewayMap response);

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

    public void setMerchantServerUrl(String url) {
        merchantServerUrl = url;
    }

    public void createSession(final CreateSessionCallback callback) {
        final Handler handler = new Handler(message -> {
            if (callback != null) {
                if (message.obj instanceof Throwable) {
                    callback.onError((Throwable) message.obj);
                } else {
                    Pair<String, String> pair = (Pair<String, String>) message.obj;
                    callback.onSuccess(pair.first, pair.second);
                }
            }
            return true;
        });

        new Thread(() -> {
            Message m = handler.obtainMessage();
            try {
                m.obj = executeCreateSession();
            } catch (Exception e) {
                m.obj = e;
            }
            handler.sendMessage(m);
        }).start();
    }

    public void check3DSecureEnrollment(final String sessionId, final String amount, final String currency, final String threeDSecureId, final Check3DSecureEnrollmentCallback callback) {
        final Handler handler = new Handler(message -> {
            if (callback != null) {
                if (message.obj instanceof Throwable) {
                    callback.onError((Throwable) message.obj);
                } else {
                    callback.onSuccess((GatewayMap) message.obj);
                }
            }
            return true;
        });

        new Thread(() -> {
            Message m = handler.obtainMessage();
            try {
                m.obj = executeCheck3DSEnrollment(sessionId, amount, currency, threeDSecureId);
            } catch (Exception e) {
                m.obj = e;
            }
            handler.sendMessage(m);
        }).start();
    }

    public void completeSession(final String sessionId, final String orderId, final String transactionId, final String amount, final String currency, final String threeDSecureId, final Boolean isGooglePay, final CompleteSessionCallback callback) {
        final Handler handler = new Handler(message -> {
            if (callback != null) {
                if (message.obj instanceof Throwable) {
                    callback.onError((Throwable) message.obj);
                } else {
                    callback.onSuccess((String) message.obj);
                }
            }
            return true;
        });

        new Thread(() -> {
            Message m = handler.obtainMessage();
            try {
                m.obj = executeCompleteSession(sessionId, orderId, transactionId, amount, currency, threeDSecureId, isGooglePay);
            } catch (Exception e) {
                m.obj = e;
            }
            handler.sendMessage(m);
        }).start();
    }

    Pair<String, String> executeCreateSession() throws Exception {
        String jsonResponse = doJsonRequest(new URL(merchantServerUrl + "/session.php"), "", "POST", null, null, HttpsURLConnection.HTTP_OK);

        GatewayMap response = new GatewayMap(jsonResponse);

        if (!response.containsKey("gatewayResponse")) {
            throw new RuntimeException("Could not read gateway response");
        }

        if (!response.containsKey("gatewayResponse.result") || !"SUCCESS".equalsIgnoreCase((String) response.get("gatewayResponse.result"))) {
            throw new RuntimeException("Create session result: " + response.get("gatewayResponse.result"));
        }

        String apiVersion = (String) response.get("apiVersion");
        String sessionId = (String) response.get("gatewayResponse.session.id");
        Log.i("createSession", "Created session with ID " + sessionId + " with API version " + apiVersion);

        return new Pair<>(sessionId, apiVersion);
    }

    GatewayMap executeCheck3DSEnrollment(String sessionId, String amount, String currency, String threeDSecureId) throws Exception {
        GatewayMap request = new GatewayMap()
                .set("apiOperation", "CHECK_3DS_ENROLLMENT")
                .set("session.id", sessionId)
                .set("order.amount", amount)
                .set("order.currency", currency)
                .set("3DSecure.authenticationRedirect.responseUrl", merchantServerUrl + "/3DSecureResult.php?3DSecureId=" + threeDSecureId);

        String jsonRequest = GSON.toJson(request);

        String jsonResponse = doJsonRequest(new URL(merchantServerUrl + "/3DSecure.php?3DSecureId=" + threeDSecureId), jsonRequest, "PUT", null, null, HttpsURLConnection.HTTP_OK);

        GatewayMap response = new GatewayMap(jsonResponse);

        if (!response.containsKey("gatewayResponse")) {
            throw new RuntimeException("Could not read gateway response");
        }

        // if there is an error result, throw it
        if (response.containsKey("gatewayResponse.result") && "ERROR".equalsIgnoreCase((String) response.get("gatewayResponse.result"))) {
            throw new RuntimeException("Check 3DS Enrollment Error: " + response.get("gatewayResponse.error.explanation"));
        }

        return response;
    }

    String executeCompleteSession(String sessionId, String orderId, String transactionId, String amount, String currency, String threeDSecureId, Boolean isGooglePay) throws Exception {
        GatewayMap request = new GatewayMap()
                .set("apiOperation", "PAY")
                .set("session.id", sessionId)
                .set("order.amount", amount)
                .set("order.currency", currency)
                .set("sourceOfFunds.type", "CARD")
                .set("transaction.source", "INTERNET")
                .set("transaction.frequency", "SINGLE");

        if (threeDSecureId != null) {
            request.put("3DSecureId", threeDSecureId);
        }

        if (isGooglePay) {
            request.put("order.walletProvider", "GOOGLE_PAY");
        }

        String jsonRequest = GSON.toJson(request);

        String jsonResponse = doJsonRequest(new URL(merchantServerUrl + "/transaction.php?order=" + orderId + "&transaction=" + transactionId), jsonRequest, "PUT", null, null, HttpsURLConnection.HTTP_OK);

        GatewayMap response = new GatewayMap(jsonResponse);

        if (!response.containsKey("gatewayResponse")) {
            throw new RuntimeException("Could not read gateway response");
        }

        if (!response.containsKey("gatewayResponse.result") || !"SUCCESS".equalsIgnoreCase((String) response.get("gatewayResponse.result"))) {
            throw new RuntimeException("Error processing payment");
        }

        return (String) response.get("gatewayResponse.result");
    }


    /**
     * Initialise a new SSL context using the algorithm, key manager(s), trust manager(s) and
     * source of randomness.
     *
     * @throws NoSuchAlgorithmException if the algorithm is not supported by the android platform
     * @throws KeyManagementException   if initialization of the context fails
     */
    void initialiseSslContext() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, null, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    }

    /**
     * Open an HTTP or HTTPS connection to a particular URL
     *
     * @param address a valid HTTP[S] URL to connect to
     * @return an HTTP or HTTPS connection as appropriate
     * @throws KeyManagementException   if initialization of the SSL context fails
     * @throws NoSuchAlgorithmException if the SSL algorithm is not supported by the android platform
     * @throws MalformedURLException    if the address was not in the HTTP or HTTPS scheme
     * @throws IOException              if the connection could not be opened
     */
    HttpURLConnection openConnection(URL address)
            throws KeyManagementException, NoSuchAlgorithmException, IOException {

        switch (address.getProtocol().toUpperCase()) {
            case "HTTPS":
                initialiseSslContext();
                break;
            case "HTTP":
                break;
            default:
                throw new MalformedURLException("Not an HTTP[S] address");
        }

        HttpURLConnection connection = (HttpURLConnection) address.openConnection();
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(60000);
        return connection;
    }

    /**
     * Send a JSON object to an open HTTP[S] connection
     *
     * @param connection an open HTTP[S] connection, as returned by {@link #openConnection(URL)}
     * @param method     an HTTP method, e.g. PUT, POST or GET
     * @param json       a valid JSON-formatted object
     * @param username   user name for basic authorization (can be null for no auth)
     * @param password   password for basic authorization (can be null for no auth)
     * @return an HTTP response code
     * @throws IOException if the connection could not be written to
     */
    int makeJsonRequest(HttpURLConnection connection, String method, String json,
                               String username, String password) throws IOException {

        connection.setDoOutput(true);
        connection.setRequestMethod(method);
        connection.setFixedLengthStreamingMode(json.getBytes().length);
        connection.setRequestProperty("Content-Type", "application/json");

        if (!isEmpty(username) && !isEmpty(password)) {
            String basicAuth = username + ':' + password;
            basicAuth = Base64.encodeToString(basicAuth.getBytes(), Base64.DEFAULT);
            connection.setRequestProperty("Authorization", "Basic " + basicAuth);
        }

        PrintWriter out = new PrintWriter(connection.getOutputStream());
        out.print(json);
        out.close();

        return connection.getResponseCode();
    }

    /**
     * Retrieve a JSON response from an open HTTP[S] connection. This would typically be called
     * after {@link #makeJsonRequest(HttpURLConnection, String, String, String, String)}
     *
     * @param connection an open HTTP[S] connection
     * @return a json object in string form
     * @throws IOException if the connection could not be read from
     */
    String getJsonResponse(HttpURLConnection connection) throws IOException {
        StringBuilder responseOutput = new StringBuilder();
        String line;
        BufferedReader br = null;

        try {
            // If the HTTP response code is 4xx or 5xx, we need error rather than input stream
            InputStream stream = (connection.getResponseCode() < 400)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            br = new BufferedReader(new InputStreamReader(stream));

            while ((line = br.readLine()) != null) {
                responseOutput.append(line);
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    /* Ignore close exception */
                }
            }
        }

        return responseOutput.toString();
    }

    /**
     * End-to-end method to send some json to an url and retrieve a response
     *
     * @param address             url to send the request to
     * @param jsonRequest         a valid JSON-formatted object
     * @param httpMethod          an HTTP method, e.g. PUT, POST or GET
     * @param username            user name for basic authorization (can be null for no auth)
     * @param password            password for basic authorization (can be null for no auth)
     * @param expectResponseCodes permitted HTTP response codes, e.g. HTTP_OK (200)
     * @return a json response object in string form
     */
    String doJsonRequest(URL address, String jsonRequest, String httpMethod, String username, String password, int... expectResponseCodes) {

        HttpURLConnection connection;
        int responseCode;

        try {
            connection = openConnection(address);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Couldn't initialise SSL context", e);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't open an HTTP[S] connection", e);
        }

        try {
            responseCode =
                    makeJsonRequest(connection, httpMethod, jsonRequest, username, password);

            if (!contains(expectResponseCodes, responseCode)) {
                throw new RuntimeException("Unexpected response code " + responseCode);
            }
        } catch (SocketTimeoutException e) {
            throw new RuntimeException("Timeout whilst sending JSON data");
        } catch (IOException e) {
            throw new RuntimeException("Error sending JSON data", e);
        }

        try {
            String responseBody = getJsonResponse(connection);

            if (responseBody == null) {
                throw new RuntimeException("No data in response");
            }

            return responseBody;
        } catch (SocketTimeoutException e) {
            throw new RuntimeException("Timeout whilst retrieving JSON response");
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving JSON response", e);
        }
    }


    static boolean contains(int[] haystack, int needle) {
        for (int candidate : haystack) {
            if (candidate == needle) {
                return true;
            }
        }

        return false;
    }
}


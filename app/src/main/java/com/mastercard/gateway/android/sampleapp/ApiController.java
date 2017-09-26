package com.mastercard.gateway.android.sampleapp;

import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
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

    public void setMerchantServerUrl(String url) {
        merchantServerUrl = url;
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

    public void completeSession(final String sessionId, final String orderId, final String transactionId, final String amount, final String currency, final CompleteSessionCallback callback) {
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
                    m.obj = executeCompleteSession(sessionId, orderId, transactionId, amount, currency);
                } catch (Exception e) {
                    m.obj = e;
                }
                handler.sendMessage(m);
            }
        }).start();
    }

    String executeCreateSession() throws Exception {
        String jsonResponse = doJsonRequest(new URL(merchantServerUrl + "/session.php"), "", "POST", null, null, HttpsURLConnection.HTTP_OK);

        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> map = GSON.fromJson(jsonResponse, type);

        if (!map.containsKey("result") || !"SUCCESS".equalsIgnoreCase((String) map.get("result"))) {
            throw new RuntimeException("Create session result: " + map.get("result"));
        }

        String sessionId = ((Map<String, String>) map.get("session")).get("id");
        Log.i("createSession", "Created session with ID: " + sessionId);
        return sessionId;
    }

    String executeCompleteSession(String sessionId, String orderId, String transactionId, String amount, String currency) throws Exception {
        JsonObject order = new JsonObject();
        order.addProperty("amount", amount);
        order.addProperty("currency", currency);

        JsonObject session = new JsonObject();
        session.addProperty("id", sessionId);

        JsonObject sourceOfFunds = new JsonObject();
        sourceOfFunds.addProperty("type", "CARD");

        JsonObject transaction = new JsonObject();
        transaction.addProperty("frequency", "SINGLE");

        JsonObject json = new JsonObject();
        json.addProperty("apiOperation", "PAY");
        json.add("order", order);
        json.add("session", session);
        json.add("sourceOfFunds", sourceOfFunds);
        json.add("transaction", transaction);

        String jsonString = GSON.toJson(json);

        String jsonResponse = doJsonRequest(new URL(merchantServerUrl + "/transaction.php?order=" + orderId + "&transaction=" + transactionId), jsonString, "PUT", null, null, HttpsURLConnection.HTTP_OK);

        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> map = GSON.fromJson(jsonResponse, type);

        if (!map.containsKey("result") || !"SUCCESS".equalsIgnoreCase((String) map.get("result"))) {
            throw new RuntimeException("Payment result: " + map.get("result") + "; Payload: " + jsonResponse);
        }

        return (String) map.get("result");
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


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

package com.mastercard.gateway.android.sdk;


import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mastercard.gateway.android.sdk.api.GatewayCallback;
import com.mastercard.gateway.android.sdk.api.GatewayException;
import com.mastercard.gateway.android.sdk.api.HttpRequest;
import com.mastercard.gateway.android.sdk.api.HttpResponse;
import com.mastercard.gateway.android.sdk.api.UpdateSessionRequest;
import com.mastercard.gateway.android.sdk.api.model.Card;
import com.mastercard.gateway.android.sdk.api.model.Expiry;
import com.mastercard.gateway.android.sdk.api.model.Provided;
import com.mastercard.gateway.android.sdk.api.model.SourceOfFunds;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import io.reactivex.Single;

/**
 * The public interface to the Gateway SDK.
 * <p>
 * Example set up:
 * <p>
 * <code>
 * Gateway gateway = new Gateway();
 * gateway.setMerchantId("your-merchant-id");
 * gateway.setRegion(Gateway.Region.NORTH_AMERICA);
 * </code>
 */
@SuppressWarnings("unused,WeakerAccess")
public class Gateway {

    /**
     * The available gateway regions
     */
    public enum Region {
        ASIA_PACIFIC("ap"),
        EUROPE("eu"),
        NORTH_AMERICA("na"),
        MTF("test");

        String prefix;

        Region(String prefix) {
            this.prefix = prefix;
        }

        String getPrefix() {
            return prefix;
        }
    }


    static final int API_VERSION = 44;

    static final String INTERMEDIATE_CA = "-----BEGIN CERTIFICATE-----\n" +
            "MIIFAzCCA+ugAwIBAgIEUdNg7jANBgkqhkiG9w0BAQsFADCBvjELMAkGA1UEBhMC\n" +
            "VVMxFjAUBgNVBAoTDUVudHJ1c3QsIEluYy4xKDAmBgNVBAsTH1NlZSB3d3cuZW50\n" +
            "cnVzdC5uZXQvbGVnYWwtdGVybXMxOTA3BgNVBAsTMChjKSAyMDA5IEVudHJ1c3Qs\n" +
            "IEluYy4gLSBmb3IgYXV0aG9yaXplZCB1c2Ugb25seTEyMDAGA1UEAxMpRW50cnVz\n" +
            "dCBSb290IENlcnRpZmljYXRpb24gQXV0aG9yaXR5IC0gRzIwHhcNMTQxMDIyMTcw\n" +
            "NTE0WhcNMjQxMDIzMDczMzIyWjCBujELMAkGA1UEBhMCVVMxFjAUBgNVBAoTDUVu\n" +
            "dHJ1c3QsIEluYy4xKDAmBgNVBAsTH1NlZSB3d3cuZW50cnVzdC5uZXQvbGVnYWwt\n" +
            "dGVybXMxOTA3BgNVBAsTMChjKSAyMDEyIEVudHJ1c3QsIEluYy4gLSBmb3IgYXV0\n" +
            "aG9yaXplZCB1c2Ugb25seTEuMCwGA1UEAxMlRW50cnVzdCBDZXJ0aWZpY2F0aW9u\n" +
            "IEF1dGhvcml0eSAtIEwxSzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEB\n" +
            "ANo/ltBNuS9E59s5XptQ7lylYdpBZ1MJqgCajld/KWvbx+EhJKo60I1HI9Ltchbw\n" +
            "kSHSXbe4S6iDj7eRMmjPziWTLLJ9l8j+wbQXugmeA5CTe3xJgyJoipveR8MxmHou\n" +
            "fUAL0u8+07KMqo9Iqf8A6ClYBve2k1qUcyYmrVgO5UK41epzeWRoUyW4hM+Ueq4G\n" +
            "RQyja03Qxr7qGKQ28JKyuhyIjzpSf/debYMcnfAf5cPW3aV4kj2wbSzqyc+UQRlx\n" +
            "RGi6RzwE6V26PvA19xW2nvIuFR4/R8jIOKdzRV1NsDuxjhcpN+rdBQEiu5Q2Ko1b\n" +
            "Nf5TGS8IRsEqsxpiHU4r2RsCAwEAAaOCAQkwggEFMA4GA1UdDwEB/wQEAwIBBjAP\n" +
            "BgNVHRMECDAGAQH/AgEAMDMGCCsGAQUFBwEBBCcwJTAjBggrBgEFBQcwAYYXaHR0\n" +
            "cDovL29jc3AuZW50cnVzdC5uZXQwMAYDVR0fBCkwJzAloCOgIYYfaHR0cDovL2Ny\n" +
            "bC5lbnRydXN0Lm5ldC9nMmNhLmNybDA7BgNVHSAENDAyMDAGBFUdIAAwKDAmBggr\n" +
            "BgEFBQcCARYaaHR0cDovL3d3dy5lbnRydXN0Lm5ldC9ycGEwHQYDVR0OBBYEFIKi\n" +
            "cHTdvFM/z3vU981/p2DGCky/MB8GA1UdIwQYMBaAFGpyJnrQHu995ztpUdRsjZ+Q\n" +
            "EmarMA0GCSqGSIb3DQEBCwUAA4IBAQA/HBpb/0AiHY81DC2qmSerwBEycNc2KGml\n" +
            "jbEnmUK+xJPrSFdDcSPE5U6trkNvknbFGe/KvG9CTBaahqkEOMdl8PUM4ErfovrO\n" +
            "GhGonGkvG9/q4jLzzky8RgzAiYDRh2uiz2vUf/31YFJnV6Bt0WRBFG00Yu0GbCTy\n" +
            "BrwoAq8DLcIzBfvLqhboZRBD9Wlc44FYmc1r07jHexlVyUDOeVW4c4npXEBmQxJ/\n" +
            "B7hlVtWNw6f1sbZlnsCDNn8WRTx0S5OKPPEr9TVwc3vnggSxGJgO1JxvGvz8pzOl\n" +
            "u7sY82t6XTKH920l5OJ2hiEeEUbNdg5vT6QhcQqEpy02qUgiUX6C\n" +
            "-----END CERTIFICATE-----\n";


    Logger logger = new BaseLogger();
    String merchantId;
    Region region;


    /**
     * Constructs a new instance.
     */
    public Gateway() {
    }

    /**
     * Gets the current Merchant ID
     *
     * @return The current Merchant ID
     */
    public String getMerchantId() {
        return merchantId;
    }

    /**
     * Sets the current Merchant ID
     *
     * @param merchantId A valid Merchant ID
     * @return The <tt>Gateway</tt> instance
     * @throws IllegalArgumentException If the provided Merchant ID is null
     */
    public Gateway setMerchantId(String merchantId) {
        if (merchantId == null) {
            throw new IllegalArgumentException("Merchant ID may not be null");
        }
        this.merchantId = merchantId;
        return this;
    }

    /**
     * Gets the current {@link Region}
     *
     * @return The region
     */
    public Region getRegion() {
        return region;
    }

    /**
     * Sets the current {@link Region} to target
     *
     * @param region The region
     * @return The <tt>Gateway</tt> instance
     * @throws IllegalArgumentException If the provided Merchant ID is null
     */
    public Gateway setRegion(Region region) {
        if (region == null) {
            throw new IllegalArgumentException("Region may not be null");
        }

        this.region = region;

        return this;
    }

//    /**
//     * Updates a Mastercard Gateway session with basic card information.
//     * <p>
//     * Creates a {@link Card} object from the card information and calls
//     * {@link Gateway#updateSessionWithCardInfo(String, Card, GatewayCallback)}
//     *
//     * @param sessionId    A session ID from the Mastercard Gateway
//     * @param nameOnCard   The cardholder's name
//     * @param cardNumber   The card number
//     * @param securityCode The card security code
//     * @param expiryMM     The card expiration month (format: MM)
//     * @param expiryYY     The card expiration year (format: YY)
//     * @param callback     A callback to handle success and error messages
//     * @see Card
//     */
//    public void updateSessionWithCardInfo(String sessionId, String nameOnCard, String cardNumber, String securityCode, String expiryMM, String expiryYY, GatewayCallback<UpdateSessionResponse> callback) {
//        Card card = buildCard(nameOnCard, cardNumber, securityCode, expiryMM, expiryYY);
//        updateSessionWithCardInfo(sessionId, card, callback);
//    }

//    /**
//     * Updates a Mastercard Gateway session with the provided card information.
//     * <p>
//     * Creates an {@link UpdateSessionRequest} object from the <tt>Card</tt> and calls
//     * {@link Gateway#updateSession(String, UpdateSessionRequest, GatewayCallback)}
//     *
//     * @param sessionId A session ID from the Mastercard Gateway
//     * @param card      The card object
//     * @param callback  A callback to handle success and error messages
//     * @see UpdateSessionRequest
//     */
//    public void updateSessionWithCardInfo(String sessionId, Card card, GatewayCallback<UpdateSessionResponse> callback) {
//        UpdateSessionRequest request = buildUpdateSessionRequest(card);
//        updateSession(sessionId, request, callback);
//    }

//    /**
//     * Updates a Mastercard Gateway session with the provided information.
//     * <p>
//     * This will execute the necessary network request on a background thread
//     * and return the response (or error) to the provided callback.
//     *
//     * @param sessionId A session ID from the Mastercard Gateway
//     * @param request   The request object
//     * @param callback  A callback to handle success and error messages
//     * @throws IllegalArgumentException If the provided session id is null
//     */
//    public void updateSession(String sessionId, UpdateSessionRequest request, GatewayCallback<UpdateSessionResponse> callback) {
//        if (sessionId == null) {
//            throw new IllegalArgumentException("Session Id may not be null");
//        }
//
//        runGatewayRequest(getUpdateSessionUrl(sessionId), request, callback);
//    }

    /**
     * Updates a Mastercard Gateway session with the provided information.
     * <p>
     * This will execute the necessary network request on a background thread
     * and return the response (or error) to the provided callback.
     *
     * @param sessionId A session ID from the Mastercard Gateway
     * @param request   A map of the request data
     * @param callback  A callback to handle success and error messages
     * @throws IllegalArgumentException If the provided session id is null
     */
    public void updateSession(String sessionId, Map<String, Object> request, GatewayCallback callback) {
        String url = getUpdateSessionUrl(sessionId);
        request.put("apiOperation", "UPDATE_PAYER_DATA");
        runGatewayRequest(url, HttpRequest.Method.PUT, request, callback);
    }

//    /**
//     * Updates a Mastercard Gateway session with basic card information.
//     * <p>
//     * Creates a {@link Card} object from the card information and calls
//     * {@link Gateway#updateSessionWithCardInfo(String, Card)}
//     * <p>
//     * Does not adhere to any particular scheduler
//     *
//     * @param sessionId    A session ID from the Mastercard Gateway
//     * @param nameOnCard   The cardholder's name
//     * @param cardNumber   The card number
//     * @param securityCode The card security code
//     * @param expiryMM     The card expiration month (format: MM)
//     * @param expiryYY     The card expiration year (format: YY)
//     * @return A <tt>Single</tt> of the response object
//     * @see Card
//     * @see <a href="http://reactivex.io/RxJava/javadoc/io/reactivex/Single.html">RxJava: Single</a>
//     */
//    public Single<UpdateSessionResponse> updateSessionWithCardInfo(String sessionId, String nameOnCard, String cardNumber, String securityCode, String expiryMM, String expiryYY) {
//        Card card = buildCard(nameOnCard, cardNumber, securityCode, expiryMM, expiryYY);
//        return updateSessionWithCardInfo(sessionId, card);
//    }

//    /**
//     * Updates a Mastercard Gateway session with the provided card information.
//     * <p>
//     * Creates an {@link UpdateSessionRequest} object from the <tt>Card</tt> and calls
//     * {@link Gateway#updateSession(String, UpdateSessionRequest, GatewayCallback)}
//     * <p>
//     * Does not adhere to any particular scheduler
//     *
//     * @param sessionId A session ID from the Mastercard Gateway
//     * @param card      The card object
//     * @return A <tt>Single</tt> of the response object
//     * @see UpdateSessionRequest
//     * @see <a href="http://reactivex.io/RxJava/javadoc/io/reactivex/Single.html">RxJava: Single</a>
//     */
//    public Single<UpdateSessionResponse> updateSessionWithCardInfo(String sessionId, Card card) {
//        UpdateSessionRequest request = buildUpdateSessionRequest(card);
//        return updateSession(sessionId, request);
//    }

//    /**
//     * Updates a Mastercard Gateway session with the provided information.
//     * <p>
//     * Does not adhere to any particular scheduler
//     *
//     * @param sessionId A session ID from the Mastercard Gateway
//     * @param request   The request object
//     * @return A <tt>Single</tt> of the response object
//     * @throws IllegalArgumentException If the provided session id is null
//     * @see <a href="http://reactivex.io/RxJava/javadoc/io/reactivex/Single.html">RxJava: Single</a>
//     */
//    public Single<UpdateSessionResponse> updateSession(String sessionId, UpdateSessionRequest request) {
//        if (sessionId == null) {
//            throw new IllegalArgumentException("Session Id may not be null");
//        }
//
//        return runGatewayRequest(getUpdateSessionUrl(sessionId), request);
//    }

    /**
     * Updates a Mastercard Gateway session with the provided information.
     * <p>
     * Does not adhere to any particular scheduler
     *
     * @param sessionId A session ID from the Mastercard Gateway
     * @param request   A map of the request data
     * @return A <tt>Single</tt> of the response map
     * @throws IllegalArgumentException If the provided session id is null
     * @see <a href="http://reactivex.io/RxJava/javadoc/io/reactivex/Single.html">RxJava: Single</a>
     */
    public Single<Map<String, Object>> updateSession(String sessionId, Map<String, Object> request) {
        String url = getUpdateSessionUrl(sessionId);
        request.put("apiOperation", "UPDATE_PAYER_DATA");
        return runGatewayRequest(url, HttpRequest.Method.PUT, request);
    }


    String getApiUrl() {
        if (region == null) {
            throw new IllegalStateException("You must initialize the the Gateway instance with a Region before use");
        }

        return "https://" + region.getPrefix() + "-gateway.mastercard.com/api/rest/version/" + API_VERSION;
    }

    String getUpdateSessionUrl(String sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session Id may not be null");
        }

        if (merchantId == null) {
            throw new IllegalStateException("You must initialize the the Gateway instance with a Merchant Id before use");
        }

        return getApiUrl() + "/merchant/" + merchantId + "/session/" + sessionId;
    }

    void runGatewayRequest(String url, HttpRequest.Method method, Map<String, Object> payload, GatewayCallback callback) {
        // create handler on current thread
        Handler handler = new Handler(msg -> handleCallbackMessage(callback, msg.obj));

        new Thread(() -> {
            Message m = handler.obtainMessage();
            try {
                m.obj = executeGatewayRequest(url, method, payload);
            } catch (Exception e) {
                m.obj = e;
            }

            handler.sendMessage(m);
        }).start();
    }

    Single<Map<String, Object>> runGatewayRequest(String url, HttpRequest.Method method, Map<String, Object> gatewayRequest) {
        return Single.fromCallable(() -> executeGatewayRequest(url, method, gatewayRequest));
    }

    Card buildCard(String nameOnCard, String cardNumber, String securityCode, String expiryMM, String expiryYY) {
        return Card.builder()
                .nameOnCard(nameOnCard)
                .number(cardNumber)
                .securityCode(securityCode)
                .expiry(Expiry.builder()
                        .month(expiryMM)
                        .year(expiryYY)
                        .build()
                )
                .build();
    }

    UpdateSessionRequest buildUpdateSessionRequest(Card card) {
        return UpdateSessionRequest.builder()
                .apiOperation("UPDATE_PAYER_DATA")
                .sourceOfFunds(SourceOfFunds.builder()
                        .provided(Provided.builder()
                                .card(card)
                                .build()
                        )
                        .build()
                )
                .build();
    }

    // handler callback method when executing a request on a new thread
    @SuppressWarnings("unchecked")
    boolean handleCallbackMessage(GatewayCallback callback, Object arg) {
        if (callback != null) {
            if (arg instanceof Throwable) {
                callback.onError((Throwable) arg);
            } else {
                callback.onSuccess((Map<String, Object>) arg);
            }
        }
        return true;
    }

    Map<String, Object> executeGatewayRequest(String endpoint, HttpRequest.Method method, Map<String, Object> payload) throws Exception {
        // init gson
        Gson gson = new Gson();

        // init ssl context with limiting trust managers
        SSLContext context = createSslContext();

        // init connection
        URL url = new URL(endpoint);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        if (url.getProtocol().startsWith("https")) {
            ((HttpsURLConnection) c).setSSLSocketFactory(context.getSocketFactory());
        }
        c.setConnectTimeout(15000);
        c.setReadTimeout(60000);
        c.setRequestMethod(method.name());
        c.setRequestProperty("User-Agent", "Gateway-Android-SDK/" + BuildConfig.VERSION_NAME);
        c.setRequestProperty("Content-Type", "application/json");
        c.setDoOutput(true);

        String json = gson.toJson(payload);

        // log request
        logger.logRequest(c, json);

        // write data
        if (json != null) {
            OutputStream os = c.getOutputStream();
            os.write(json.getBytes("UTF-8"));
            os.close();
        }

        c.connect();

        HttpResponse response = new HttpResponse(c);

        c.disconnect();

        // log response
        logger.logResponse(response);

        // if response contains exception, rethrow it
        if (response.hasException()) {
            throw response.getException();
        }

        // parse the response body
        Map<String, Object> responseMap = gson.fromJson(response.getPayload(), new TypeToken<Map<String, Object>>() {
        }.getType());

        // if response has bad status code, create a gateway exception and throw it
        if (!response.isOk()) {
            String message = "An error occurred";
            if (responseMap.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) responseMap.get("error");
                message = (String) error.get("explanation");
            }

            GatewayException exception = new GatewayException(message);
            exception.setStatusCode(response.getStatusCode());
            exception.setErrorResponseMap(responseMap);

            throw exception;
        }

        return responseMap;
    }


//    <T extends GatewayResponse> T executeGatewayRequest(String endpoint, GatewayRequest<T> gatewayRequest) throws Exception {
//        // build the http request from the gateway request object
//        HttpRequest httpRequest = gatewayRequest.buildHttpRequest().withEndpoint(endpoint);
//
//        // init ssl context with limiting trust managers
//        SSLContext context = createSslContext();
//
//        // init connection
//        URL url = new URL(httpRequest.endpoint());
//        HttpURLConnection c = (HttpURLConnection) url.openConnection();
//        if (url.getProtocol().startsWith("https")) {
//            ((HttpsURLConnection) c).setSSLSocketFactory(context.getSocketFactory());
//        }
//        c.setConnectTimeout(15000);
//        c.setReadTimeout(60000);
//        c.setRequestProperty("User-Agent", "Gateway-Android-SDK/" + BuildConfig.VERSION_NAME);
//        c.setRequestProperty("Content-Type", httpRequest.contentType());
//        c.setDoOutput(true);
//
//        HttpRequest.Method method = httpRequest.method();
//        if (method != null) {
//            c.setRequestMethod(method.name());
//        }
//
//        String payload = httpRequest.payload();
//
//        // log request
//        logger.logRequest(c, payload);
//
//        // write data
//        if (payload != null) {
//            OutputStream os = c.getOutputStream();
//            os.write(payload.getBytes("UTF-8"));
//            os.close();
//        }
//
//        c.connect();
//
//        HttpResponse response = new HttpResponse(c);
//
//        c.disconnect();
//
//        // log response
//        logger.logResponse(response);
//
//        // if response contains exception, rethrow it
//        if (response.hasException()) {
//            throw response.getException();
//        }
//
//        Gson gson = new GsonBuilder()
//                .registerTypeAdapterFactory(GatewayTypeAdapterFactory.create())
//                .create();
//
//        // if response has bad status code, create a gateway exception and throw it
//        if (!response.isOk()) {
//            ErrorResponse errorResponse = gson.fromJson(response.getPayload(), ErrorResponse.class);
//            Error error = errorResponse.error();
//
//            GatewayException exception = new GatewayException(error == null ? null : error.explanation());
//            exception.setStatusCode(response.getStatusCode());
//            exception.setErrorResponse(errorResponse);
//
//            throw exception;
//        }
//
//        // build the response object from the payload
//        return gson.fromJson(response.getPayload(), gatewayRequest.getResponseClass());
//    }

    SSLContext createSslContext() throws Exception {
        // create and initialize a KeyStore
        KeyStore keyStore = createSslKeyStore();

        // create a TrustManager that trusts the INTERMEDIATE_CA in our KeyStore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        TrustManager[] trustManagers = tmf.getTrustManagers();

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trustManagers, null);

        return context;
    }

    KeyStore createSslKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);

        // add our trusted cert to the keystore
        keyStore.setCertificateEntry("gateway.mastercard.com", readCertificate(INTERMEDIATE_CA));

        return keyStore;
    }

    X509Certificate readCertificate(String cert) throws CertificateException {
        byte[] bytes = cert.getBytes();
        InputStream is = new ByteArrayInputStream(bytes);

        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is);
    }
}

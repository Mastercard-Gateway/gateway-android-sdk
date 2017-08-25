package com.mastercard.gateway.android.sdk2;


import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mastercard.gateway.android.sdk2.api.ErrorResponse;
import com.mastercard.gateway.android.sdk2.api.GatewayCallback;
import com.mastercard.gateway.android.sdk2.api.GatewayException;
import com.mastercard.gateway.android.sdk2.api.GatewayRequest;
import com.mastercard.gateway.android.sdk2.api.GatewayResponse;
import com.mastercard.gateway.android.sdk2.api.HttpRequest;
import com.mastercard.gateway.android.sdk2.api.HttpResponse;
import com.mastercard.gateway.android.sdk2.api.UpdateSessionRequest;
import com.mastercard.gateway.android.sdk2.api.UpdateSessionResponse;
import com.mastercard.gateway.android.sdk2.api.model.Card;
import com.mastercard.gateway.android.sdk2.api.model.Expiry;
import com.mastercard.gateway.android.sdk2.api.model.Provided;
import com.mastercard.gateway.android.sdk2.api.model.SourceOfFunds;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import io.reactivex.Single;

public class Gateway {

    String apiEndpoint;
    String merchantId;
    Map<String, String> certificates = new HashMap<>();


    public Gateway() {
    }

    /**
     * @return
     */
    public String getApiEndpoint() {
        return apiEndpoint;
    }

    /**
     * @param apiEndpoint
     */
    public Gateway setApiEndpoint(String apiEndpoint) throws MalformedURLException {
        // TODO sanitize or validate this?
        this.apiEndpoint = apiEndpoint;
        return this;
    }

    /**
     * @return
     */
    public String getMerchantId() {
        return merchantId;
    }

    /**
     * @param merchantId
     */
    public Gateway setMerchantId(String merchantId) {
        this.merchantId = merchantId;
        return this;
    }

    /**
     * @param alias
     * @param certificate
     */
    public Gateway addTrustedCertificate(String alias, String certificate) {
        certificates.put(alias, certificate);
        return this;
    }

    /**
     * @param alias
     */
    public void removeTrustedCertificate(String alias) {
        certificates.remove(alias);
    }

    /**
     *
     */
    public void clearTrustedCertificates() {
        certificates.clear();
    }

    /**
     *
     * @param nameOnCard
     * @param cardNumber
     * @param securityCode
     * @param expiryMM
     * @param expiryYY
     * @param callback
     */
    public void updateSessionWithPayerData(String sessionId, String nameOnCard, String cardNumber, String securityCode, String expiryMM, String expiryYY, GatewayCallback<UpdateSessionResponse> callback) {
        String url = apiEndpoint + "/merchant/" + merchantId + "/session/" + sessionId;
        UpdateSessionRequest request = buildUpdateSessionRequest(nameOnCard, cardNumber, securityCode, expiryMM, expiryYY);

        runGatewayRequest(url, request, callback);
    }

    /**
     *
     * @param nameOnCard
     * @param cardNumber
     * @param securityCode
     * @param expiryMM
     * @param expiryYY
     * @return
     */
    public Single<UpdateSessionResponse> updateSessionWithPayerData(String sessionId, String nameOnCard, String cardNumber, String securityCode, String expiryMM, String expiryYY) {
        String url = apiEndpoint + "/merchant/" + merchantId + "/session/" + sessionId;
        UpdateSessionRequest request = buildUpdateSessionRequest(nameOnCard, cardNumber, securityCode, expiryMM, expiryYY);

        return runGatewayRequest(url, request);
    }




    void runGatewayRequest(String url, GatewayRequest gatewayRequest, GatewayCallback callback) {
        // create handler on current thread
        Handler handler = new Handler(msg -> handleCallbackMessage(callback, msg.obj));

        new Thread(() -> {
            Message m = handler.obtainMessage();
            try {
                m.obj = executeGatewayRequest(url, gatewayRequest);
            } catch (Exception e) {
                m.obj = e;
            }

            handler.sendMessage(m);
        }).start();
    }

    <T extends GatewayResponse> Single<T> runGatewayRequest(String url, GatewayRequest<T> gatewayRequest) {
        return Single.fromCallable(() -> executeGatewayRequest(url, gatewayRequest));
    }

    UpdateSessionRequest buildUpdateSessionRequest(String nameOnCard, String cardNumber, String securityCode, String expiryMM, String expiryYY) {
        return UpdateSessionRequest.builder()
                .apiOperation("UPDATE_PAYER_DATA")
                .sourceOfFunds(SourceOfFunds.builder()
                        .provided(Provided.builder()
                                .card(Card.builder()
                                        .nameOnCard(nameOnCard)
                                        .number(cardNumber)
                                        .securityCode(securityCode)
                                        .expiry(Expiry.builder()
                                                .month(expiryMM)
                                                .year(expiryYY)
                                                .build()
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build();
    }

    // handler callback method when executing a request on a new thread
    @SuppressWarnings("unchecked")
    <T extends GatewayResponse> boolean handleCallbackMessage(GatewayCallback<T> callback, Object arg) {
        if (callback != null) {
            if (arg instanceof Throwable) {
                callback.onError((Throwable) arg);
            } else {
                callback.onSuccess((T) arg);
            }
        }
        return true;
    }

    <T extends GatewayResponse> T executeGatewayRequest(String url, GatewayRequest<T> gatewayRequest) throws Exception {
        // build the http request from the gateway request object
        HttpRequest httpRequest = gatewayRequest.buildHttpRequest().withEndpoint(url);

        // init ssl context with limiting trust managers
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, createTrustManagers(), null);

        // init connection
        URL url = new URL(httpRequest.endpoint());
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        if (url.getProtocol().startsWith("https")) {
            ((HttpsURLConnection) c).setSSLSocketFactory(context.getSocketFactory());
        }
        c.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
        c.setReadTimeout(Constants.SOCKET_TIMEOUT);
        c.setRequestProperty("User-Agent", Constants.USER_AGENT);
        c.setRequestProperty("Content-Type", httpRequest.contentType());
        c.setDoOutput(true);
        c.setRequestMethod(httpRequest.method().name());

        String payload = httpRequest.payload();

        // log request
        logRequest(c, payload);

        // write data
        if (payload != null) {
            OutputStream os = c.getOutputStream();
            os.write(payload.getBytes("UTF-8"));
            os.close();
        }

        c.connect();

        HttpResponse response = new HttpResponse(c);

        c.disconnect();

        // log response
        logResponse(response);

        // if response contains exception, rethrow it
        if (response.hasException()) {
            throw response.getException();
        }

        Gson gson = new GsonBuilder().create();

        // if response has bad status code, create a gateway exception and throw it
        if (!response.isOk()) {
            GatewayException exception = new GatewayException();
            exception.setStatusCode(response.getStatusCode());
            exception.setErrorResponse(gson.fromJson(response.getPayload(), ErrorResponse.class));

            throw exception;
        }

        // build the response object from the payload
        return gson.fromJson(response.getPayload(), gatewayRequest.getResponseClass());
    }

    TrustManager[] createTrustManagers() {
        try {
            // create and initialize a KeyStore
            KeyStore keyStore = createSSLKeyStore();

            // create a TrustManager that trusts the CA in our KeyStore
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            return tmf.getTrustManagers();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TrustManager[0];
    }

    KeyStore createSSLKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);

        // add our trusted cert to the keystore
        keyStore.setCertificateEntry(Constants.EU_CA_ALIAS, readPemCertificate(Constants.EU_CA));
        // TODO US
        // TODO AU

        // add user-provided trusted certs to keystore
        for (String alias : certificates.keySet()) {
            keyStore.setCertificateEntry(alias, readPemCertificate(certificates.get(alias)));
        }

        return keyStore;
    }

    Certificate readPemCertificate(String pemCert) throws CertificateException {
        // add our trusted cert to the keystore
        ByteArrayInputStream is = new ByteArrayInputStream(Base64.decode(pemCert, Base64.DEFAULT));
        return CertificateFactory.getInstance("X.509").generateCertificate(is);
    }

    void logRequest(HttpURLConnection c, String data) {
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
            Log.d(Gateway.class.getSimpleName(), part);
        }
    }


    void logResponse(HttpResponse response) {
        String log = "RESPONSE: ";

        // log response headers
        Map<String, List<String>> headers = response.getConnection().getHeaderFields();
        Set<String> keys = headers.keySet();

        int i = 0;
        for (String key : keys) {
            List<String> values = headers.get(key);
            for (String value : values) {
                if (i == 0 && key == null) {
                    log += value;

                    String payload = response.getPayload();
                    if (payload.length() > 0) {
                        log += "\n-- Data: " + payload;
                    }
                } else {
                    log += "\n-- " + (key == null ? "" : key + ": ") + value;
                }
                i++;
            }
        }

        String[] parts = log.split("\n");
        for (String part : parts) {
            Log.d(Gateway.class.getSimpleName(), part);
        }
    }
}

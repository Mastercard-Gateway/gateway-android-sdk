package com.mastercard.gateway.android.sdk;


import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mastercard.gateway.android.sdk.api.ErrorResponse;
import com.mastercard.gateway.android.sdk.api.GatewayCallback;
import com.mastercard.gateway.android.sdk.api.GatewayException;
import com.mastercard.gateway.android.sdk.api.GatewayRequest;
import com.mastercard.gateway.android.sdk.api.GatewayResponse;
import com.mastercard.gateway.android.sdk.api.HttpRequest;
import com.mastercard.gateway.android.sdk.api.HttpResponse;
import com.mastercard.gateway.android.sdk.api.UpdateSessionRequest;
import com.mastercard.gateway.android.sdk.api.UpdateSessionResponse;
import com.mastercard.gateway.android.sdk.api.model.Card;
import com.mastercard.gateway.android.sdk.api.model.Expiry;
import com.mastercard.gateway.android.sdk.api.model.Provided;
import com.mastercard.gateway.android.sdk.api.model.SourceOfFunds;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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

    public enum Region {
        TEST("test"),
        EUROPE("eu"),
        NORTH_AMERICA("na"),
        ASIA_PACIFIC("ap");

        String urlPrefix;

        Region(String urlPrefix) {
            this.urlPrefix = urlPrefix;
        }
    }

    Region region = Region.TEST;
    int apiVersion = BuildConfig.DEFAULT_API_VERSION;
    Map<String, String> certificates = new HashMap<>();

    String merchantId;


    /**
     *
     */
    public Gateway() {
    }

    /**
     *
     * @return
     */
    public Region getRegion() {
        return region;
    }

    /**
     *
     * @param region
     * @return
     */
    public Gateway setRegion(Region region) {
        this.region = region;
        return this;
    }

    /**
     *
     * @param regionName
     * @return
     */
    public Gateway setRegion(String regionName) {
        this.region = Region.TEST;

        for (Region region : Region.values()) {
            if (region.name().equalsIgnoreCase(regionName)) {
                this.region = region;
                break;
            }
        }

        return this;
    }

    /**
     *
     * @return
     */
    public int getApiVersion() {
        return apiVersion;
    }

    /**
     *
     * @param version
     * @return
     */
    public Gateway setApiVersion(int version) {
        apiVersion = version;
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
    public void updateSessionWithCardInfo(String sessionId, String nameOnCard, String cardNumber, String securityCode, String expiryMM, String expiryYY, GatewayCallback<UpdateSessionResponse> callback) {
        Card card = buildCard(nameOnCard, cardNumber, securityCode, expiryMM, expiryYY);
        updateSessionWithCardInfo(sessionId, card, callback);
    }

    /**
     *
     * @param sessionId
     * @param card
     * @param callback
     */
    public void updateSessionWithCardInfo(String sessionId, Card card, GatewayCallback<UpdateSessionResponse> callback) {
        UpdateSessionRequest request = buildUpdateSessionRequest(card);
        updateSession(sessionId, request, callback);
    }

    /**
     *
     * @param sessionId
     * @param request
     * @param callback
     */
    public void updateSession(String sessionId, UpdateSessionRequest request, GatewayCallback<UpdateSessionResponse> callback) {
        runGatewayRequest(getUpdateSessionUrl(sessionId), request, callback);
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
    public Single<UpdateSessionResponse> updateSessionWithCardInfo(String sessionId, String nameOnCard, String cardNumber, String securityCode, String expiryMM, String expiryYY) {
        Card card = buildCard(nameOnCard, cardNumber, securityCode, expiryMM, expiryYY);
        return updateSessionWithCardInfo(sessionId, card);
    }

    /**
     *
     * @param sessionId
     * @param card
     * @return
     */
    public Single<UpdateSessionResponse> updateSessionWithCardInfo(String sessionId, Card card) {
        UpdateSessionRequest request = buildUpdateSessionRequest(card);
        return updateSession(sessionId, request);
    }

    /**
     *
     * @param sessionId
     * @param request
     * @return
     */
    public Single<UpdateSessionResponse> updateSession(String sessionId, UpdateSessionRequest request) {
        return runGatewayRequest(getUpdateSessionUrl(sessionId), request);
    }


    String getApiUrl() {
        return "https://" + region.urlPrefix + "-gateway.mastercard.com/api/rest/version/" + apiVersion;
    }

    String getUpdateSessionUrl(String sessionId) {
        return getApiUrl() + "/merchant/" + merchantId + "/session/" + sessionId;
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

    <T extends GatewayResponse> T executeGatewayRequest(String endpoint, GatewayRequest<T> gatewayRequest) throws Exception {
        // build the http request from the gateway request object
        HttpRequest httpRequest = gatewayRequest.buildHttpRequest().withEndpoint(endpoint);

        // init ssl context with limiting trust managers
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, createTrustManagers(), null);

        // init connection
        URL url = new URL(httpRequest.endpoint());
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        if (url.getProtocol().startsWith("https")) {
            ((HttpsURLConnection) c).setSSLSocketFactory(context.getSocketFactory());
        }
        c.setConnectTimeout(15000);
        c.setReadTimeout(60000);
        c.setRequestProperty("User-Agent", "Gateway-Android-SDK/" + BuildConfig.VERSION_NAME);
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
            exception.setErrorResponse(ErrorResponse.typeAdapter(gson).fromJson(response.getPayload()));

            throw exception;
        }

        // build the response object from the payload
        return gatewayRequest.getResponseTypeAdapter(gson).fromJson(response.getPayload());
    }

    TrustManager[] createTrustManagers() {
        try {
            // create and initialize a KeyStore
            KeyStore keyStore = createSSLKeyStore();

            // create a TrustManager that trusts the INTERMEDIATE_CA in our KeyStore
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
        keyStore.setCertificateEntry("gateway.mastercard.com", readPemCertificate(BuildConfig.INTERMEDIATE_CA));

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

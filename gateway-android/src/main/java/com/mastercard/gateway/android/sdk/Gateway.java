package com.mastercard.gateway.android.sdk;


import android.os.Handler;
import android.os.Message;
import android.support.annotation.VisibleForTesting;
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

/**
 * The public interface to the Gateway SDK.
 * <p>
 * Example use case:
 * <p>
 * <code>
 * Gateway gateway = new Gateway();
 * gateway.setBaseUrl("https://your-gateway-url.com");
 * gateway.setMerchantId("your-merchant-id");
 * gateway.
 * </code>
 */
@SuppressWarnings("unused,WeakerAccess")
public class Gateway {

    @VisibleForTesting
    int apiVersion = BuildConfig.DEFAULT_API_VERSION;

    @VisibleForTesting
    Map<String, Certificate> certificates = new HashMap<>();

    @VisibleForTesting
    String merchantId;

    @VisibleForTesting
    URL baseUrl;


    /**
     * Constructs a new instance.
     */
    public Gateway() {
    }

    /**
     * Gets the current base url for the Gateway
     *
     * @return The base url
     */
    public URL getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the base url for the Gateway.
     * <p>
     * Example:
     * <code>
     * gateway.setBaseUrl("https://some-gateway-url.com")
     * </code>
     *
     * @param url The Gateway url
     * @return The <tt>Gateway</tt> instance
     */
    public Gateway setBaseUrl(String url) {
        if (url == null) {
            throw new IllegalArgumentException("Url may not be null");
        }

        try {
            setBaseUrl(new URL(url));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Incorrect url format", e);
        }

        return this;
    }

    /**
     * Sets the base url for the Gateway
     *
     * @param url The Gateway url
     * @return The <tt>Gateway</tt> instance
     */
    public Gateway setBaseUrl(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("Url may not be null");
        }

        try {
            // store updated baseUrl with only protocol/host from original
            this.baseUrl = new URL("https", url.getHost(), "");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Incorrect url format", e);
        }

        return this;
    }

    /**
     * Gets the current API version.
     *
     * @return The current API version
     */
    public int getApiVersion() {
        return apiVersion;
    }

    /**
     * Sets the current API version.
     *
     * @param version A valid API version
     * @return The <tt>Gateway</tt> instance
     * @see <a href="https://test-gateway.mastercard.com/api/documentation/apiDocumentation/rest-json/index.html">Gateway API Versions</a> for a list of available version numbers
     */
    public Gateway setApiVersion(int version) {
        apiVersion = version;
        return this;
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
     * Adds a PEM-encoded certificate to the trust store.
     * <p>
     * The Mastercard Gateway certificate is already
     * registered and can not be removed. However, if you require another certificate
     * to be trusted, you may use this method to add additional certificates to the trust store.
     *
     * @param alias       An alias for the certificate
     * @param certificate A valid PEM-encoded X509 certificate
     * @return The <tt>Gateway</tt> instance
     * @throws IllegalArgumentException If either the alias or certificate is null, or the certificate can not be read
     */
    public Gateway addTrustedCertificate(String alias, String certificate) {
        // parse and validate PEM cert
        Certificate cert;
        try {
            cert = readPemCertificate(certificate);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to read PEM certificate", e);
        }

        addTrustedCertificate(alias, cert);

        return this;
    }

    /**
     * Adds a certificate to the trust store.
     * <p>
     * The Mastercard Gateway certificate is already
     * registered and can not be removed. However, if you require another certificate
     * to be trusted, you may use this method to add additional certificates to the trust store.
     *
     * @param alias       An alias for the certificate
     * @param certificate An X509 certificate
     * @return The <tt>Gateway</tt> instance
     * @throws IllegalArgumentException If either the alias or certificate is null
     */
    public Gateway addTrustedCertificate(String alias, Certificate certificate) {
        if (alias == null) {
            throw new IllegalArgumentException("Alias may not be null");
        }

        if (certificate == null) {
            throw new IllegalArgumentException("Certificate may not be null");
        }

        certificates.put(alias, certificate);

        return this;
    }


    /**
     * Removes a trusted certificate from the trust store.
     * <p>
     * The Mastercard Gateway certificate can not be removed.
     *
     * @param alias The alias of the certificate
     */
    public void removeTrustedCertificate(String alias) {
        certificates.remove(alias);
    }

    /**
     * Clears all additional trusted certificates.
     * <p>
     * The Mastercard Gateway certificate can not be removed.
     */
    public void clearTrustedCertificates() {
        certificates.clear();
    }

    /**
     * Updates a Mastercard Gateway session with basic card information.
     * <p>
     * Creates a {@link Card} object from the card information and calls
     * {@link Gateway#updateSessionWithCardInfo(String, Card, GatewayCallback)}
     *
     * @param sessionId    A session ID from the Mastercard Gateway
     * @param nameOnCard   The cardholder's name
     * @param cardNumber   The card number
     * @param securityCode The card security code
     * @param expiryMM     The card expiration month (format: MM)
     * @param expiryYY     The card expiration year (format: YY)
     * @param callback     A callback to handle success and error messages
     * @see Card
     */
    public void updateSessionWithCardInfo(String sessionId, String nameOnCard, String cardNumber, String securityCode, String expiryMM, String expiryYY, GatewayCallback<UpdateSessionResponse> callback) {
        Card card = buildCard(nameOnCard, cardNumber, securityCode, expiryMM, expiryYY);
        updateSessionWithCardInfo(sessionId, card, callback);
    }

    /**
     * Updates a Mastercard Gateway session with the provided card information.
     * <p>
     * Creates an {@link UpdateSessionRequest} object from the <tt>Card</tt> and calls
     * {@link Gateway#updateSession(String, UpdateSessionRequest, GatewayCallback)}
     *
     * @param sessionId A session ID from the Mastercard Gateway
     * @param card      The card object
     * @param callback  A callback to handle success and error messages
     * @see UpdateSessionRequest
     */
    public void updateSessionWithCardInfo(String sessionId, Card card, GatewayCallback<UpdateSessionResponse> callback) {
        UpdateSessionRequest request = buildUpdateSessionRequest(card);
        updateSession(sessionId, request, callback);
    }

    /**
     * Updates a Mastercard Gateway session with the provided information.
     * <p>
     * This will execute the necessary network request on a background thread
     * and return the response (or error) to the provided callback.
     *
     * @param sessionId A session ID from the Mastercard Gateway
     * @param request   The request object
     * @param callback  A callback to handle success and error messages
     */
    public void updateSession(String sessionId, UpdateSessionRequest request, GatewayCallback<UpdateSessionResponse> callback) {
        runGatewayRequest(getUpdateSessionUrl(sessionId), request, callback);
    }

    /**
     * Updates a Mastercard Gateway session with basic card information.
     * <p>
     * Creates a {@link Card} object from the card information and calls
     * {@link Gateway#updateSessionWithCardInfo(String, Card)}
     * <p>
     * Does not adhere to any particular scheduler
     *
     * @param sessionId    A session ID from the Mastercard Gateway
     * @param nameOnCard   The cardholder's name
     * @param cardNumber   The card number
     * @param securityCode The card security code
     * @param expiryMM     The card expiration month (format: MM)
     * @param expiryYY     The card expiration year (format: YY)
     * @return A <tt>Single</tt> of the response object
     * @see Card
     * @see <a href="http://reactivex.io/RxJava/javadoc/io/reactivex/Single.html">RxJava: Single</a>
     */
    public Single<UpdateSessionResponse> updateSessionWithCardInfo(String sessionId, String nameOnCard, String cardNumber, String securityCode, String expiryMM, String expiryYY) {
        Card card = buildCard(nameOnCard, cardNumber, securityCode, expiryMM, expiryYY);
        return updateSessionWithCardInfo(sessionId, card);
    }

    /**
     * Updates a Mastercard Gateway session with the provided card information.
     * <p>
     * Creates an {@link UpdateSessionRequest} object from the <tt>Card</tt> and calls
     * {@link Gateway#updateSession(String, UpdateSessionRequest, GatewayCallback)}
     * <p>
     * Does not adhere to any particular scheduler
     *
     * @param sessionId A session ID from the Mastercard Gateway
     * @param card      The card object
     * @return A <tt>Single</tt> of the response object
     * @see UpdateSessionRequest
     * @see <a href="http://reactivex.io/RxJava/javadoc/io/reactivex/Single.html">RxJava: Single</a>
     */
    public Single<UpdateSessionResponse> updateSessionWithCardInfo(String sessionId, Card card) {
        UpdateSessionRequest request = buildUpdateSessionRequest(card);
        return updateSession(sessionId, request);
    }

    /**
     * Updates a Mastercard Gateway session with the provided information.
     * <p>
     * Does not adhere to any particular scheduler
     *
     * @param sessionId A session ID from the Mastercard Gateway
     * @param request   The request object
     * @return A <tt>Single</tt> of the response object
     * @see <a href="http://reactivex.io/RxJava/javadoc/io/reactivex/Single.html">RxJava: Single</a>
     */
    public Single<UpdateSessionResponse> updateSession(String sessionId, UpdateSessionRequest request) {
        return runGatewayRequest(getUpdateSessionUrl(sessionId), request);
    }


    @VisibleForTesting
    String getApiUrl() {
        return baseUrl.toString() + "/api/rest/version/" + apiVersion;
    }

    @VisibleForTesting
    String getUpdateSessionUrl(String sessionId) {
        return getApiUrl() + "/merchant/" + merchantId + "/session/" + sessionId;
    }

    @VisibleForTesting
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

    @VisibleForTesting
    <T extends GatewayResponse> Single<T> runGatewayRequest(String url, GatewayRequest<T> gatewayRequest) {
        return Single.fromCallable(() -> executeGatewayRequest(url, gatewayRequest));
    }

    @VisibleForTesting
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

    @VisibleForTesting
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
    @VisibleForTesting
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

    @VisibleForTesting
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

        HttpRequest.Method method = httpRequest.method();
        if (method != null) {
            c.setRequestMethod(method.name());
        }

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

    @VisibleForTesting
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

    @VisibleForTesting
    KeyStore createSSLKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);

        // add our trusted cert to the keystore
        keyStore.setCertificateEntry("gateway.mastercard.com", readPemCertificate(BuildConfig.INTERMEDIATE_CA));

        // add user-provided trusted certs to keystore
        for (String alias : certificates.keySet()) {
            keyStore.setCertificateEntry(alias, certificates.get(alias));
        }

        return keyStore;
    }

    @VisibleForTesting
    Certificate readPemCertificate(String pemCert) throws CertificateException {
        // add our trusted cert to the keystore
        ByteArrayInputStream is = new ByteArrayInputStream(Base64.decode(pemCert, Base64.DEFAULT));
        return CertificateFactory.getInstance("X.509").generateCertificate(is);
    }

    @VisibleForTesting
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

    @VisibleForTesting
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

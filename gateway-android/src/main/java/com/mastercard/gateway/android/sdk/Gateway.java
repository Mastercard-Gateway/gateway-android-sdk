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


import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.wallet.AutoResolveHelper;

import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

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
        ASIA_PACIFIC("ap."),
        EUROPE("eu."),
        NORTH_AMERICA("na."),
        INDIA("in."),
        CHINA("cn."),
        MTF("mtf."),
        SAUDI_ARABIA("ksa.");

        String prefix;

        Region(String prefix) {
            this.prefix = prefix;
        }

        String getPrefix() {
            return prefix;
        }
    }

    // internally supported request methods
    enum Method {
        PUT
    }


    static final int MIN_API_VERSION = 39;
    static final int CONNECTION_TIMEOUT = 15000;
    static final int READ_TIMEOUT = 60000;
    static final int REQUEST_3D_SECURE = 10000;
    static final int REQUEST_GOOGLE_PAY_LOAD_PAYMENT_DATA = 10001;
    static final String API_OPERATION = "UPDATE_PAYER_DATA";
    static final String USER_AGENT = "Gateway-Android-SDK/" + BuildConfig.VERSION_NAME;


    Logger logger = new BaseLogger();
    Gson gson = new Gson();
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

    /**
     * Updates a Mastercard Gateway session with the provided information.<br>
     * The API version number provided MUST match the version used when the session was created.
     * <p>
     * This will execute the necessary network request on a background thread
     * and return the response (or error) to the provided callback.
     *
     * @param sessionId  A session ID from the Mastercard Gateway
     * @param apiVersion The API version number used when the session was created
     * @param payload    A map of the request data
     * @param callback   A callback to handle success and error messages
     * @throws IllegalArgumentException If the provided session id is null
     */
    public void updateSession(String sessionId, String apiVersion, GatewayMap payload, GatewayCallback callback) {
        GatewayRequest request = buildUpdateSessionRequest(sessionId, apiVersion, payload);

        runGatewayRequest(request, callback);
    }

    /**
     * Updates a Mastercard Gateway session with the provided information.
     * The API version number provided MUST match the version used when the session was created.
     * <p>
     * Does not adhere to any particular scheduler
     *
     * @param sessionId  A session ID from the Mastercard Gateway
     * @param apiVersion The API version number used when the session was created
     * @param payload    A map of the request data
     * @return A <tt>Single</tt> of the response map
     * @throws IllegalArgumentException If the provided session id is null
     * @see <a href="http://reactivex.io/RxJava/javadoc/io/reactivex/Single.html">RxJava: Single</a>
     */
    public Single<GatewayMap> updateSession(String sessionId, String apiVersion, GatewayMap payload) {
        GatewayRequest request = buildUpdateSessionRequest(sessionId, apiVersion, payload);

        return runGatewayRequest(request);
    }

    GatewayRequest buildUpdateSessionRequest(String sessionId, String apiVersion, GatewayMap payload) {
        GatewayRequest request = new GatewayRequest();
        request.url = getUpdateSessionUrl(sessionId, apiVersion);
        request.method = Method.PUT;
        request.payload = payload;
        request.payload.put("device.browser", USER_AGENT);

        // version 50 of the API dropped the requirement for the apiOperation parameter
        // 50+ uses the standard Update Session API
        if (Integer.parseInt(apiVersion) < 50) {
            request.payload.put("apiOperation", API_OPERATION);
        } else {
            // Auth header required for v50+
            request.extraHeaders.put("Authorization", createAuthHeader(sessionId));
        }

        return request;
    }

    /**
     * Starts the {@link Gateway3DSecureActivity} for result, initializing it with the provided html
     *
     * @param activity The calling activity context
     * @param html     The initial HTML to render in the web view
     */
    public static void start3DSecureActivity(Activity activity, String html) {
        start3DSecureActivity(activity, html, null);
    }

    /**
     * Starts the {@link Gateway3DSecureActivity} for result, initializing it with the provided html
     *
     * @param activity The calling activity context
     * @param html     The initial HTML to render in the web view
     * @param title    An optional title to render in the toolbar
     */
    public static void start3DSecureActivity(Activity activity, String html, String title) {
        Intent intent = new Intent(activity, Gateway3DSecureActivity.class);
        start3DSecureActivity(activity, html, title, intent);
    }

    // separated for testability
    static void start3DSecureActivity(Activity activity, String html, String title, Intent intent) {
        intent.putExtra(Gateway3DSecureActivity.EXTRA_HTML, html); // required

        if (title != null) {
            intent.putExtra(Gateway3DSecureActivity.EXTRA_TITLE, title);
        }

        activity.startActivityForResult(intent, REQUEST_3D_SECURE);
    }

    /**
     * A convenience method for handling activity result messages returned from {@link Gateway3DSecureActivity}.
     * This method should be called within the calling Activity's onActivityResult() lifecycle method.
     * This helper only works if the 3-D Secure Activity was launched using the
     * {@link Gateway#start3DSecureActivity(Activity, String, String)} method.
     *
     * @param requestCode The request code returning from the activity result
     * @param resultCode The result code returning from the activity result
     * @param data The intent data returning from the activity result
     * @param callback An implementation of {@link Gateway3DSecureCallback}
     * @return True if handled, False otherwise
     * @see Gateway#start3DSecureActivity(Activity, String)
     * @see Gateway#start3DSecureActivity(Activity, String, String)
     */
    public static boolean handle3DSecureResult(int requestCode, int resultCode, Intent data, Gateway3DSecureCallback callback) {
        if (callback == null) {
            return false;
        }

        if (requestCode == REQUEST_3D_SECURE) {
            if (resultCode == Activity.RESULT_OK) {
                String acsResultJson = data.getStringExtra(Gateway3DSecureActivity.EXTRA_ACS_RESULT);
                GatewayMap acsResult = new GatewayMap(acsResultJson);

                callback.on3DSecureComplete(acsResult);
            } else {
                callback.on3DSecureCancel();
            }

            return true;
        }

        return false;
    }


    /**
     * A convenience method for initializing the request to get Google Pay card info
     *
     * @param paymentsClient An instance of the PaymentClient
     * @param request A properly formatted PaymentDataRequest
     * @param activity The calling activity
     * @see <a href="https://developers.google.com/pay/api/android/guides/tutorial#paymentsclient">Payments Client</a>
     * @see <a href="https://developers.google.com/pay/api/android/guides/tutorial#paymentdatarequest">Payment Data Request</a>
     */
    public static void requestGooglePayData(PaymentsClient paymentsClient, PaymentDataRequest request, Activity activity) {
        AutoResolveHelper.resolveTask(paymentsClient.loadPaymentData(request), activity, REQUEST_GOOGLE_PAY_LOAD_PAYMENT_DATA);
    }

    /**
     * A convenience method for handling activity result messages returned from Google Pay.
     * This method should be called withing the calling Activity's onActivityResult() lifecycle method.
     * This helper only works if the Google Pay dialog was launched using the
     * {@link Gateway#requestGooglePayData(PaymentsClient, PaymentDataRequest, Activity)} method.
     *
     * @param requestCode The request code returning from the activity result
     * @param resultCode The result code returning from the activity result
     * @param data The intent data returning from the activity result
     * @param callback An implementation of {@link GatewayGooglePayCallback}
     * @return True if handled, False otherwise
     * @see Gateway#requestGooglePayData(PaymentsClient, PaymentDataRequest, Activity)
     */
    public static boolean handleGooglePayResult(int requestCode, int resultCode, Intent data, GatewayGooglePayCallback callback) {
        if (callback == null) {
            return false;
        }

        if (requestCode == REQUEST_GOOGLE_PAY_LOAD_PAYMENT_DATA) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    PaymentData paymentData = PaymentData.getFromIntent(data);
                    JSONObject json = new JSONObject(paymentData.toJson());
                    callback.onReceivedPaymentData(json);
                } catch (Exception e) {
                    callback.onGooglePayError(Status.RESULT_INTERNAL_ERROR);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                callback.onGooglePayCancelled();
            } else if (resultCode == AutoResolveHelper.RESULT_ERROR) {
                Status status = AutoResolveHelper.getStatusFromIntent(data);
                callback.onGooglePayError(status);
            }

            return true;
        }

        return false;
    }


    String getApiUrl(String apiVersion) {
        if (Integer.valueOf(apiVersion) < MIN_API_VERSION) {
            throw new IllegalArgumentException("API version must be >= " + MIN_API_VERSION);
        }

        if (region == null) {
            throw new IllegalStateException("You must initialize the the Gateway instance with a Region before use");
        }

        return "https://" + region.getPrefix() + "gateway.mastercard.com/api/rest/version/" + apiVersion;
    }

    String getUpdateSessionUrl(String sessionId, String apiVersion) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session Id may not be null");
        }

        if (merchantId == null) {
            throw new IllegalStateException("You must initialize the the Gateway instance with a Merchant Id before use");
        }

        return getApiUrl(apiVersion) + "/merchant/" + merchantId + "/session/" + sessionId;
    }

    void runGatewayRequest(GatewayRequest request, GatewayCallback callback) {
        // create handler on current thread
        Handler handler = new Handler(msg -> handleCallbackMessage(callback, msg.obj));

        new Thread(() -> {
            Message m = handler.obtainMessage();
            try {
                m.obj = executeGatewayRequest(request);
            } catch (Exception e) {
                m.obj = e;
            }

            handler.sendMessage(m);
        }).start();
    }

    Single<GatewayMap> runGatewayRequest(GatewayRequest request) {
        return Single.fromCallable(() -> executeGatewayRequest(request));
    }

    // handler callback method when executing a request on a new thread
    @SuppressWarnings("unchecked")
    boolean handleCallbackMessage(GatewayCallback callback, Object arg) {
        if (callback != null) {
            if (arg instanceof Throwable) {
                callback.onError((Throwable) arg);
            } else {
                callback.onSuccess((GatewayMap) arg);
            }
        }
        return true;
    }

    GatewayMap executeGatewayRequest(GatewayRequest request) throws Exception {
        // init connection
        HttpsURLConnection c = createHttpsUrlConnection(request);

        // encode request data to json
        String requestData = gson.toJson(request.payload);

        // log request data
        logger.logRequest(c, requestData);

        // write request data
        if (requestData != null) {
            OutputStream os = c.getOutputStream();
            os.write(requestData.getBytes("UTF-8"));
            os.close();
        }

        // initiate the connection
        c.connect();

        String responseData = null;
        int statusCode = c.getResponseCode();
        boolean isStatusOk = (statusCode >= 200 && statusCode < 300);

        // if connection has output stream, get the data
        // socket time-out exceptions will be thrown here
        if (c.getDoInput()) {
            InputStream is = isStatusOk ? c.getInputStream() : c.getErrorStream();
            responseData = inputStreamToString(is);
            is.close();
        }

        c.disconnect();

        // log response
        logger.logResponse(c, responseData);

        // parse the response body
        GatewayMap response = new GatewayMap(responseData);

        // if response static is good, return response
        if (isStatusOk) {
            return response;
        }

        // otherwise, create a gateway exception and throw it
        String message = (String) response.get("error.explanation");
        if (message == null) {
            message = "An error occurred";
        }

        GatewayException exception = new GatewayException(message);
        exception.setStatusCode(statusCode);
        exception.setErrorResponse(response);

        throw exception;
    }

    HttpsURLConnection createHttpsUrlConnection(GatewayRequest request) throws Exception {
        // parse url
        URL url = new URL(request.url);

        HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
        c.setSSLSocketFactory(createSocketFactory());
        c.setConnectTimeout(CONNECTION_TIMEOUT);
        c.setReadTimeout(READ_TIMEOUT);
        c.setRequestMethod(request.method.name());
        c.setDoOutput(true);

        c.setRequestProperty("User-Agent", USER_AGENT);
        c.setRequestProperty("Content-Type", "application/json");

        // add extra headers
        if (request.extraHeaders != null) {
            for (String key : request.extraHeaders.keySet()) {
                c.setRequestProperty(key, request.extraHeaders.get(key));
            }
        }

        return c;
    }

    SSLSocketFactory createSocketFactory() throws Exception {
        // custom ssl context to enforce certificate pinning
        SSLContext sslContext = new GatewaySSLContextProvider().createSSLContext();

        // custom socket factory to enable TLSv1.2 on all supported devices
        return new GatewayTLSSocketFactory(sslContext);
    }

    String createAuthHeader(String sessionId) {
        String value = "merchant." + merchantId + ":" + sessionId;
        return "Basic " + Base64.encodeToString(value.getBytes(), Base64.NO_WRAP);
    }

    boolean isStatusCodeOk(int statusCode) {
        return (statusCode >= 200 && statusCode < 300);
    }

    String inputStreamToString(InputStream is) throws IOException {
        // get buffered reader from stream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        // read stream into string builder
        StringBuilder total = new StringBuilder();

        String line;
        while ((line = rd.readLine()) != null) {
            total.append(line);
        }

        return total.toString();
    }
}

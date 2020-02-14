package com.mastercard.gateway.android.sdk;


import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;

import org.apache.tools.ant.filters.StringInputStream;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import io.reactivex.Single;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class GatewayTest {

    Gateway gateway;

    @Before
    public void setUp() throws Exception {
        gateway = spy(new Gateway());
    }

    @Test
    public void testSetMerchantIdThrowsExceptionIfNull() throws Exception {
        try {
            gateway.setMerchantId(null);

            fail("Null merchant ID should throw illegal argument exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void setMerchantIdWorksAsExpected() throws Exception {
        gateway.setMerchantId("MERCHANT_ID");

        assertEquals(gateway.merchantId, "MERCHANT_ID");
    }

    @Test
    public void testSetRegionThrowsExceptionIfNull() throws Exception {
        try {
            gateway.setRegion(null);

            fail("Null region should throw illegal argument exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testSetRegionWorksAsIntended() throws Exception {
        gateway.setRegion(Gateway.Region.ASIA_PACIFIC);

        assertEquals(Gateway.Region.ASIA_PACIFIC, gateway.region);
    }

    @Test
    public void testBuildUpdateSessionRequestWorksAsExpected() {
        String sessionId = "session_id";
        String apiVersion = "1";
        GatewayMap payload = new GatewayMap();

        String expectedUrl = "some url";

        doReturn(expectedUrl).when(gateway).getUpdateSessionUrl(sessionId, apiVersion);

        GatewayRequest request = gateway.buildUpdateSessionRequest(sessionId, apiVersion, payload);

        assertTrue(request.payload.containsKey("device.browser"));
        assertTrue(request.payload.containsKey("apiOperation"));
        assertEquals(Gateway.API_OPERATION, request.payload.get("apiOperation"));
        assertEquals(Gateway.USER_AGENT, request.payload.get("device.browser"));
    }

    @Test
    public void testBuildUpdateSessionRequestHandlesApiVersion50() {
        String sessionId = "somesession";
        gateway.merchantId = "MERCHANT_ID";

        String apiVersion = "50";
        GatewayMap payload = new GatewayMap();

        String expectedUrl = "some url";
        String expectedAuthHeader = "Basic bWVyY2hhbnQuTUVSQ0hBTlRfSUQ6c29tZXNlc3Npb24=";

        doReturn(expectedUrl).when(gateway).getUpdateSessionUrl(sessionId, apiVersion);

        GatewayRequest request = gateway.buildUpdateSessionRequest(sessionId, apiVersion, payload);

        assertTrue(request.payload.containsKey("device.browser"));
        assertFalse(request.payload.containsKey("apiOperation"));
        assertEquals(Gateway.USER_AGENT, request.payload.get("device.browser"));

        assertTrue(request.extraHeaders.containsKey("Authorization"));
        assertEquals(expectedAuthHeader, request.extraHeaders.get("Authorization"));
    }

    @Test
    public void testStart3DSecureActivitySkipsTitleIfNull() {
        Activity activity = mock(Activity.class);
        Intent intent = new Intent();
        String testHtml = "html";

        Gateway.start3DSecureActivity(activity, testHtml, null, intent);

        verify(activity).startActivityForResult(intent, Gateway.REQUEST_3D_SECURE);
        assertTrue(intent.hasExtra(Gateway3DSecureActivity.EXTRA_HTML));
        assertFalse(intent.hasExtra(Gateway3DSecureActivity.EXTRA_TITLE));
        assertEquals(testHtml, intent.getStringExtra(Gateway3DSecureActivity.EXTRA_HTML));
    }

    @Test
    public void testStart3DSecureActivityWorksAsExpected() {
        Activity activity = mock(Activity.class);
        Intent intent = new Intent();
        String testHtml = "html";
        String testTitle = "title";

        Gateway.start3DSecureActivity(activity, testHtml, testTitle, intent);

        verify(activity).startActivityForResult(intent, Gateway.REQUEST_3D_SECURE);
        assertTrue(intent.hasExtra(Gateway3DSecureActivity.EXTRA_HTML));
        assertTrue(intent.hasExtra(Gateway3DSecureActivity.EXTRA_TITLE));
        assertEquals(testHtml, intent.getStringExtra(Gateway3DSecureActivity.EXTRA_HTML));
        assertEquals(testTitle, intent.getStringExtra(Gateway3DSecureActivity.EXTRA_TITLE));
    }

    @Test
    public void testHandle3DSecureResultReturnsFalseWithNullCallback() {
        assertFalse(Gateway.handle3DSecureResult(0, 0, null, null));
    }

    @Test
    public void testHandle3DSSecureResultReturnsFalseIfInvalidRequestCode() {
        int invalidRequestCode = 10;
        Gateway3DSecureCallback callback = mock(Gateway3DSecureCallback.class);

        assertFalse(Gateway.handle3DSecureResult(invalidRequestCode, 0, null, callback));
    }

    @Test
    public void testHandle3DSecureResultCallsCancelIfResultNotOk() {
        int validRequestCode = Gateway.REQUEST_3D_SECURE;
        int resultCode = Activity.RESULT_CANCELED;
        Gateway3DSecureCallback callback = mock(Gateway3DSecureCallback.class);

        boolean result = Gateway.handle3DSecureResult(validRequestCode, resultCode, null, callback);

        assertTrue(result);
        verify(callback).on3DSecureCancel();
    }

    @Test
    public void testHandle3DSecureResultCallsCompleteIfResultOK() {
        int validRequestCode = Gateway.REQUEST_3D_SECURE;
        int resultCode = Activity.RESULT_OK;
        Intent data = mock(Intent.class);
        String acsResultJson = "{\"foo\":\"bar\"}";

        Gateway3DSecureCallback callback = spy(new Gateway3DSecureCallback() {
            @Override
            public void on3DSecureComplete(GatewayMap response) {
                assertNotNull(response);
                assertTrue(response.containsKey("foo"));
                assertEquals("bar", response.get("foo"));
            }

            @Override
            public void on3DSecureCancel() {
                fail("Should never have called cancel");
            }
        });

        doReturn(acsResultJson).when(data).getStringExtra(Gateway3DSecureActivity.EXTRA_ACS_RESULT);


        boolean result = Gateway.handle3DSecureResult(validRequestCode, resultCode, data, callback);

        assertTrue(result);
        verify(callback).on3DSecureComplete(any());
    }

    @Test
    public void testHandleGooglePayResultReturnsFalseWithNullCallback() {
        assertFalse(Gateway.handleGooglePayResult(0, 0, null, null));
    }

    @Test
    public void testHandleGooglePayResultReturnsFalseIfInvalidRequestCode() {
        int invalidRequestCode = 10;
        GatewayGooglePayCallback callback = mock(GatewayGooglePayCallback.class);

        assertFalse(Gateway.handleGooglePayResult(invalidRequestCode, 0, null, callback));
    }

    @Test
    public void testHandleGooglePayResultCallsError() {
        int requestCode = Gateway.REQUEST_GOOGLE_PAY_LOAD_PAYMENT_DATA;
        int resultCode = AutoResolveHelper.RESULT_ERROR;

        // mock autoresolvehelper method
        Status mockStatus = mock(Status.class);
        Intent mockData = mock(Intent.class);
        doReturn(mockStatus).when(mockData).getParcelableExtra("com.google.android.gms.common.api.AutoResolveHelper.status");

        GatewayGooglePayCallback callback = spy(new GatewayGooglePayCallback() {
            @Override
            public void onReceivedPaymentData(JSONObject paymentData) {
                fail("Should not have received payment data");
            }

            @Override
            public void onGooglePayCancelled() {
                fail("Should not have called cancelled");
            }

            @Override
            public void onGooglePayError(Status status) {
                assertEquals(mockStatus, status);
            }
        });

        boolean result = Gateway.handleGooglePayResult(requestCode, resultCode, mockData, callback);

        assertTrue(result);
        verify(callback).onGooglePayError(any());
    }

    @Test
    public void testHandleGooglePayResultCallsCancelled() {
        int requestCode = Gateway.REQUEST_GOOGLE_PAY_LOAD_PAYMENT_DATA;
        int resultCode = Activity.RESULT_CANCELED;

        GatewayGooglePayCallback callback = mock(GatewayGooglePayCallback.class);

        boolean result = Gateway.handleGooglePayResult(requestCode, resultCode, null, callback);

        assertTrue(result);
        verify(callback).onGooglePayCancelled();
    }

    @Test
    public void testHandleGooglePayResultCallsPaymentDataOnSuccess() {
        int requestCode = Gateway.REQUEST_GOOGLE_PAY_LOAD_PAYMENT_DATA;
        int resultCode = Activity.RESULT_OK;
        PaymentData pData = PaymentData.fromJson("{}");
        Intent data = new Intent();
        pData.putIntoIntent(data);

        GatewayGooglePayCallback callback = mock(GatewayGooglePayCallback.class);

        boolean result = Gateway.handleGooglePayResult(requestCode, resultCode, data, callback);

        assertTrue(result);
        verify(callback).onReceivedPaymentData(any());
    }

    @Test
    public void testGetApiUrlThrowsExceptionIfRegionIsNull() throws Exception {
        String apiVersion = "44";
        gateway.region = null;

        try {
            String apiUrl = gateway.getApiUrl(apiVersion);

            fail("Null region should have caused illegal state exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }
    }

    @Test
    public void testGetApiUrlThrowsExceptionIfApiVersionIsLessThanMin() throws Exception {
        String apiVersion = String.valueOf(Gateway.MIN_API_VERSION - 1);

        try {
            String apiUrl = gateway.getApiUrl(apiVersion);

            fail("Api version less than minimum value should have caused illegal argument exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }


    @Test
    public void testGetApiUrlWorksAsIntended() throws Exception {
        gateway.region = Gateway.Region.NORTH_AMERICA;
        String expectedUrl = "https://na.gateway.mastercard.com/api/rest/version/" + Gateway.MIN_API_VERSION;

        assertEquals(expectedUrl, gateway.getApiUrl(String.valueOf(Gateway.MIN_API_VERSION)));
    }

    @Test
    public void testGetUpdateSessionUrlThrowsExceptionIfSessionIdIsNull() throws Exception {
        try {
            gateway.getUpdateSessionUrl(null, String.valueOf(Gateway.MIN_API_VERSION));

            fail("Null session id should throw illegal argument exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }


    @Test
    public void testGetUpdateSessionUrlThrowsExceptionIfMerchantIdIsNull() throws Exception {
        gateway.merchantId = null;

        try {
            String url = gateway.getUpdateSessionUrl("sess1234", String.valueOf(Gateway.MIN_API_VERSION));

            fail("Null merchant id should have caused illegal state exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }
    }

    @Test
    public void testGetUpdateSessionUrlWorksAsIntended() throws Exception {
        gateway.merchantId = "somemerchant";
        gateway.region = Gateway.Region.NORTH_AMERICA;
        String expectedUrl = "https://na.gateway.mastercard.com/api/rest/version/" + Gateway.MIN_API_VERSION + "/merchant/somemerchant/session/sess1234";

        String actualUrl = gateway.getUpdateSessionUrl("sess1234", String.valueOf(Gateway.MIN_API_VERSION));

        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    public void testHandleCallbackMessageCallsOnErrorWithThrowableArg() throws Exception {
        GatewayCallback callback = mock(GatewayCallback.class);
        Throwable arg = new Exception("Some exception");

        gateway.handleCallbackMessage(callback, arg);

        verify(callback).onError(arg);
    }

    @Test
    public void testHandleCallbackMessageCallsSuccessWithNonThrowableArg() throws Exception {
        GatewayCallback callback = mock(GatewayCallback.class);
        GatewayMap arg = mock(GatewayMap.class);

        gateway.handleCallbackMessage(callback, arg);

        verify(callback).onSuccess(arg);
    }

    @Test
    public void testCreateConnectionWorksAsIntended() throws Exception {
        GatewayRequest request = new GatewayRequest();
        request.url = "https://www.mastercard.com";
        request.method = Gateway.Method.PUT;

        SSLSocketFactory socketFactory = mock(SSLSocketFactory.class);
        doReturn(socketFactory).when(gateway).createSocketFactory();

        HttpsURLConnection c = gateway.createHttpsUrlConnection(request);

        assertEquals(request.url, c.getURL().toString());
        assertEquals(socketFactory, c.getSSLSocketFactory());
        assertEquals(Gateway.CONNECTION_TIMEOUT, c.getConnectTimeout());
        assertEquals(Gateway.READ_TIMEOUT, c.getReadTimeout());
        assertEquals("PUT", c.getRequestMethod());
        assertEquals(Gateway.USER_AGENT, c.getRequestProperty("User-Agent"));
        assertEquals("application/json", c.getRequestProperty("Content-Type"));
        assertTrue(c.getDoOutput());
    }

    @Test
    public void testIsStatusOkWorksAsIntended() {
        int tooLow = 199;
        int tooHigh = 300;
        int justRight = 200;

        assertFalse(gateway.isStatusCodeOk(tooLow));
        assertFalse(gateway.isStatusCodeOk(tooHigh));
        assertTrue(gateway.isStatusCodeOk(justRight));
    }

    @Test
    public void testInputStreamToStringWorksAsExpected() {
        String expectedResult = "here is some string data";
        InputStream testInputStream = new StringInputStream(expectedResult);

        try {
            String result = gateway.inputStreamToString(testInputStream);
            assertEquals(expectedResult, result);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCreateAuthHeaderWorksAsExpected() {
        String sessionId = "somesession";
        gateway.merchantId = "MERCHANT_ID";

        String expectedAuthHeader = "Basic bWVyY2hhbnQuTUVSQ0hBTlRfSUQ6c29tZXNlc3Npb24=";

        String authHeader = gateway.createAuthHeader(sessionId);

        assertEquals(expectedAuthHeader, authHeader);
    }
}

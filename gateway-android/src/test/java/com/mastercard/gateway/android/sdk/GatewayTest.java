package com.mastercard.gateway.android.sdk;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.URL;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
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
    public void testUpdateSessionThrowsExceptionIfSessionIdIsNull() throws Exception {
        try {
            gateway.updateSession(null, mock(GatewayMap.class), null);

            fail("Null session id should throw illegal argument exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testGetApiUrlThrowsExceptionIfRegionIsNull() throws Exception {
        gateway.region = null;

        try {
            String apiUrl = gateway.getApiUrl();

            fail("Null region should have caused illegal state exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }
    }

    @Test
    public void testGetApiUrlWorksAsIntended() throws Exception {
        gateway.region = Gateway.Region.NORTH_AMERICA;
        String expectedUrl = "https://na-gateway.mastercard.com/api/rest/version/" + Gateway.API_VERSION;

        assertEquals(expectedUrl, gateway.getApiUrl());
    }

    @Test
    public void testGetUpdateSessionUrlThrowsExceptionIfMerchantIdIsNull() throws Exception {
        gateway.merchantId = null;

        try {
            String url = gateway.getUpdateSessionUrl("sess1234");

            fail("Null merchant id should have caused illegal state exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }
    }

    @Test
    public void testGetUpdateSessionUrlWorksAsIntended() throws Exception {
        gateway.merchantId = "somemerchant";
        gateway.region = Gateway.Region.NORTH_AMERICA;
        String expectedUrl = "https://na-gateway.mastercard.com/api/rest/version/" + Gateway.API_VERSION + "/merchant/somemerchant/session/sess1234";

        String actualUrl = gateway.getUpdateSessionUrl("sess1234");

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
        GatewayMap arg = new GatewayMap();

        gateway.handleCallbackMessage(callback, arg);

        verify(callback).onSuccess(arg);
    }

    @Test
    public void testCreateSslKeystoreContainsInternalCertificate() throws Exception {
        doReturn(mock(X509Certificate.class)).when(gateway).readCertificate(any());

        KeyStore keyStore = gateway.createSslKeyStore();

        assertTrue(keyStore.containsAlias("gateway.mastercard.com"));
    }

    @Test
    public void testReadingInternalCertificateWorksAsExpected() throws Exception {
        X509Certificate certificate = gateway.readCertificate(Gateway.INTERMEDIATE_CA);
        String expectedSerialNo = "1372807406";

        assertNotNull(certificate);
        assertEquals(expectedSerialNo, certificate.getSerialNumber().toString());
    }

    @Test
    public void testCreateConnectionWorksAsIntended() throws Exception {
        String endpoint = "https://www.mastercard.com";
        URL url = new URL(endpoint);

        SSLContext context = mock(SSLContext.class);
        SSLSocketFactory socketFactory = mock(SSLSocketFactory.class);
        doReturn(socketFactory).when(context).getSocketFactory();

        String expectedMethod = "PUT";
        String expectedUserAgent = Gateway.USER_AGENT_PREFIX + "/" + BuildConfig.VERSION_NAME;
        String expectedContentType = "application/json";


        HttpsURLConnection c = gateway.createHttpsUrlConnection(url, context, Gateway.Method.PUT);

        assertEquals(url, c.getURL());
        assertEquals(socketFactory, c.getSSLSocketFactory());
        assertEquals(Gateway.CONNECTION_TIMEOUT, c.getConnectTimeout());
        assertEquals(Gateway.READ_TIMEOUT, c.getReadTimeout());
        assertEquals(expectedMethod, c.getRequestMethod());
        assertEquals(expectedUserAgent, c.getRequestProperty("User-Agent"));
        assertEquals(expectedContentType, c.getRequestProperty("Content-Type"));
        assertTrue(c.getDoOutput());
    }
}

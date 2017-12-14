package com.mastercard.gateway.android.sdk;


import com.mastercard.gateway.android.sdk.api.GatewayCallback;
import com.mastercard.gateway.android.sdk.api.GatewayResponse;
import com.mastercard.gateway.android.sdk.api.UpdateSessionRequest;
import com.mastercard.gateway.android.sdk.api.UpdateSessionResponse;
import com.mastercard.gateway.android.sdk.api.model.Card;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

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
            gateway.updateSession(null, mock(UpdateSessionRequest.class), null);

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
    public void testBuildCardWorksAsIntended() throws Exception {
        String nameOnCard = "Test Card";
        String number = "5111111111111118";
        String cvc = "100";
        String expiryMM = "05";
        String expiryYY = "21";

        Card card = gateway.buildCard(nameOnCard, number, cvc, expiryMM, expiryYY);

        assertEquals(nameOnCard, card.nameOnCard());
        assertEquals(number, card.number());
        assertEquals(cvc, card.securityCode());
        assertEquals(expiryMM, card.expiry().month());
        assertEquals(expiryYY, card.expiry().year());
    }

    @Test
    public void testBuildUpdateSessionRequestWorksAsIntended() throws Exception {
        String nameOnCard = "Test Card";
        String number = "5111111111111118";
        String cvc = "100";
        String expiryMM = "05";
        String expiryYY = "21";

        Card card = gateway.buildCard(nameOnCard, number, cvc, expiryMM, expiryYY);

        UpdateSessionRequest request = gateway.buildUpdateSessionRequest(card);

        assertEquals("UPDATE_PAYER_DATA", request.apiOperation());
        assertEquals(card, request.sourceOfFunds().provided().card());
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
        GatewayResponse arg = UpdateSessionResponse.builder().build();

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
}

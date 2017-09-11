package com.mastercard.gateway.android.sdk;


import com.mastercard.gateway.android.sdk.api.GatewayCallback;
import com.mastercard.gateway.android.sdk.api.GatewayResponse;
import com.mastercard.gateway.android.sdk.api.Region;
import com.mastercard.gateway.android.sdk.api.UpdateSessionRequest;
import com.mastercard.gateway.android.sdk.api.UpdateSessionResponse;
import com.mastercard.gateway.android.sdk.api.model.Card;

import org.junit.Before;
import org.junit.Test;

import java.security.cert.Certificate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class GatewayTest {

    Gateway gateway;

    @Before
    public void setUp() throws Exception {
        gateway = spy(new Gateway());
    }

    @Test
    public void testSetRegionThrowsExceptionIfNull() throws Exception {
        try {
            gateway.setRegion((Region) null);

            fail("Null Region should throw a exception");
        } catch (Exception e) {
            // success
        }
    }

    @Test
    public void testSetRegionWorksAsIntended() throws Exception {
        gateway.setRegion(Region.EUROPE);

        assertEquals(gateway.region, Region.EUROPE);
    }

    @Test
    public void testSetRegionCorrectlyMatchesStringName() throws Exception {
        gateway.setRegion("NORTH_AMERICA");

        assertEquals(gateway.region, Region.NORTH_AMERICA);

        gateway.setRegion("europe");

        assertEquals(gateway.region, Region.EUROPE);
    }

    @Test
    public void setRegionDefaultsToTestIfNoStringNameMatches() throws Exception {
        gateway.setRegion("BANANA");

        assertEquals(gateway.region, Region.TEST);
    }

    @Test
    public void setApiVersionWorksAsIntended() throws Exception {
        gateway.setApiVersion(40);

        assertEquals(gateway.apiVersion, 40);
    }

    @Test
    public void testSetMerchantIdThrowsExceptionIfNull() throws Exception {
        try {
            gateway.setMerchantId(null);

            fail("Null merchant ID should throw exception");
        } catch (Exception e) {
            // success
        }
    }

    @Test
    public void setMerchantIdWorksAsExpected() throws Exception {
        gateway.setMerchantId("MERCHANT_ID");

        assertEquals(gateway.merchantId, "MERCHANT_ID");
    }

    @Test
    public void testAddTrustedCertificateThrowsExceptionOnParseError() throws Exception {
        doThrow(IllegalArgumentException.class).when(gateway).readPemCertificate(any());

        try {
            gateway.addTrustedCertificate("alias", "bogus cert data");

            fail("A failure to parse the certificate should throw an exception");
        } catch (Exception e) {
            // success
        }
    }

    @Test
    public void testAddTrustedCertificateThrowsExceptionOnNullParameters() throws Exception {
        try {
            gateway.addTrustedCertificate(null, (Certificate) null);

            fail("Null alias should throw exception");
        } catch (Exception e) {
            // success
        }

        try {
            gateway.addTrustedCertificate("some alias", (Certificate) null);

            fail("Null certificate should throw exception");
        } catch (Exception e) {
            // success
        }
    }

    @Test
    public void testAddTrustedCertificateWorksAsIntended() throws Exception {
        Certificate certificate = mock(Certificate.class);

        assertEquals(gateway.certificates.size(), 0);

        gateway.addTrustedCertificate("alias", certificate);

        assertEquals(gateway.certificates.size(), 1);
        assertTrue(gateway.certificates.containsKey("alias"));
    }

    @Test
    public void testRemoveTrustedCertificateWorksAsIntended() throws Exception {
        Certificate certificate = mock(Certificate.class);

        gateway.addTrustedCertificate("alias", certificate);

        assertTrue(gateway.certificates.containsKey("alias"));

        gateway.removeTrustedCertificate("alias");

        assertEquals(gateway.certificates.size(), 0);
    }

    @Test
    public void testClearTrustedCertificatesWorksAsIntended() throws Exception {
        Certificate certificate = mock(Certificate.class);
        gateway.addTrustedCertificate("alias1", certificate);
        gateway.addTrustedCertificate("alias2", certificate);

        assertEquals(gateway.certificates.size(), 2);

        gateway.clearTrustedCertificates();

        assertEquals(gateway.certificates.size(), 0);
    }


    @Test
    public void testGetApiUrlWorksAsIntended() throws Exception {
        String expectedUrl = "https://eu-gateway.mastercard.com/api/rest/version/40";

        gateway.setRegion(Region.EUROPE).setApiVersion(40);

        assertEquals(gateway.getApiUrl(), expectedUrl);
    }

    @Test
    public void testGetUpdateSessionUrlWorksAsIntended() throws Exception {
        String expectedUrl = "https://eu-gateway.mastercard.com/api/rest/version/40/merchant/somemerchant/session/sess1234";

        gateway.setMerchantId("somemerchant")
                .setRegion(Region.EUROPE)
                .setApiVersion(40);

        assertEquals(gateway.getUpdateSessionUrl("sess1234"), expectedUrl);
    }

    @Test
    public void testBuildCardWorksAsIntended() throws Exception {
        String nameOnCard = "Test Card";
        String number = "5111111111111118";
        String cvc = "100";
        String expiryMM = "05";
        String expiryYY = "21";

        Card card = gateway.buildCard(nameOnCard, number, cvc, expiryMM, expiryYY);

        assertEquals(card.nameOnCard(), nameOnCard);
        assertEquals(card.number(), number);
        assertEquals(card.securityCode(), cvc);
        assertEquals(card.expiry().month(), expiryMM);
        assertEquals(card.expiry().year(), expiryYY);
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

        assertEquals(request.apiOperation(), "UPDATE_PAYER_DATA");
        assertEquals(request.sourceOfFunds().provided().card(), card);
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
}

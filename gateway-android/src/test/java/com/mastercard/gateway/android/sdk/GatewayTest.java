package com.mastercard.gateway.android.sdk;


import android.util.Base64;

import com.mastercard.gateway.android.sdk.api.GatewayCallback;
import com.mastercard.gateway.android.sdk.api.GatewayResponse;
import com.mastercard.gateway.android.sdk.api.UpdateSessionRequest;
import com.mastercard.gateway.android.sdk.api.UpdateSessionResponse;
import com.mastercard.gateway.android.sdk.api.model.Card;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class GatewayTest {

    Gateway gateway;

    @Before
    public void setUp() throws Exception {
        gateway = spy(new Gateway());
    }

    @Test
    public void testSetBaseUrlThrowsExceptionIfNull() throws Exception {
        try {
            gateway.setBaseUrl((String) null);

            fail("Null url should throw exception");
        } catch (Exception e) {
            // success
        }

        try {
            gateway.setBaseUrl((URL) null);

            fail("Null url should throw exception");
        } catch (Exception e) {
            // success
        }
    }

    @Test
    public void testSetBaseUrlWithStringThrowsExceptionIfInvalidFormat() throws Exception {
        try {
            gateway.setBaseUrl("i am not a url");

            fail("Invalid baseUrl format should throw exception");
        } catch (Exception e) {
            // success
        }
    }

    @Test
    public void testSetBaseUrlWorksAsIntended() throws Exception {
        gateway.setBaseUrl("https://gateway.somebank.com");

        URL url = gateway.getBaseUrl();
        assertEquals("https://gateway.somebank.com", url.toString());
    }

    @Test
    public void testSetBaseUrlOnlyKeepsHostnameFromOriginal() throws Exception {
        gateway.setBaseUrl("http://gateway.somebank.com/a/path/to/nowhere?query=somequery");

        URL url = gateway.getBaseUrl();
        assertEquals("https://gateway.somebank.com", url.toString());
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
        doThrow(IllegalArgumentException.class).when(gateway).readCertificate((String) any());

        try {
            gateway.addTrustedCertificate("alias", "bogus cert data");

            fail("A failure to parse the certificate should throw an exception");
        } catch (Exception e) {
            // success
        }

        try {
            gateway.addTrustedCertificate("alias", getResourceInputStream("boguscert.txt"));

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

        assertEquals(1, gateway.certificates.size());
        assertTrue(gateway.certificates.containsKey("alias"));
    }

    @Test
    public void testRemoveTrustedCertificateWorksAsIntended() throws Exception {
        Certificate certificate = mock(Certificate.class);

        gateway.addTrustedCertificate("alias", certificate);

        assertTrue(gateway.certificates.containsKey("alias"));

        gateway.removeTrustedCertificate("alias");

        assertEquals(0, gateway.certificates.size());
    }

    @Test
    public void testClearTrustedCertificatesWorksAsIntended() throws Exception {
        Certificate certificate = mock(Certificate.class);
        gateway.addTrustedCertificate("alias1", certificate);
        gateway.addTrustedCertificate("alias2", certificate);

        assertEquals(2, gateway.certificates.size());

        gateway.clearTrustedCertificates();

        assertEquals(0, gateway.certificates.size());
    }


    @Test
    public void testGetApiUrlWorksAsIntended() throws Exception {
        String expectedUrl = "https://somegatewayurl.com/api/rest/version/" + Gateway.API_VERSION;

        gateway.setBaseUrl("https://somegatewayurl.com");

        assertEquals(expectedUrl, gateway.getApiUrl());
    }

    @Test
    public void testGetUpdateSessionUrlWorksAsIntended() throws Exception {
        String expectedUrl = "https://somegatewayurl.com/api/rest/version/" + Gateway.API_VERSION + "/merchant/somemerchant/session/sess1234";

        gateway.setMerchantId("somemerchant")
                .setBaseUrl("https://somegatewayurl.com");

        assertEquals(expectedUrl, gateway.getUpdateSessionUrl("sess1234"));
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
        doReturn(mock(X509Certificate.class)).when(gateway).readCertificate((String) any());

        KeyStore keyStore = gateway.createSslKeyStore();

        assertTrue(keyStore.containsAlias("gateway.mastercard.com"));
    }

    @Test
    public void testCreateSslKeystoreContainsAdditionalTrustedCertificates() throws Exception {
        doReturn(mock(X509Certificate.class)).when(gateway).readCertificate((String) any());

        gateway.addTrustedCertificate("alias1", mock(Certificate.class));
        gateway.addTrustedCertificate("alias2", mock(Certificate.class));

        KeyStore keyStore = gateway.createSslKeyStore();

        assertEquals(3, keyStore.size());
        assertTrue(keyStore.containsAlias("custom.alias1"));
        assertTrue(keyStore.containsAlias("custom.alias2"));
    }

    @Test
    public void testReadCertificateFromStringWorksAsExpected() throws Exception {
        // test valid PEM data
        String cert = getPemCertString();
        X509Certificate certificate = gateway.readCertificate(cert);

        assertNotNull(certificate);
        assertEquals("1372807406", certificate.getSerialNumber().toString());

        // test base64 encoded DER data
        cert = getBase64DerCert();
        certificate = gateway.readCertificate(cert);

        assertNotNull(certificate);
        assertEquals("1372807406", certificate.getSerialNumber().toString());
    }

    @Test
    public void testReadCertificateFromInputStreamWorksAsExpected() throws Exception {
        // test input stream from raw DER file
        InputStream is = getResourceInputStream("gateway.cer");
        X509Certificate certificate = gateway.readCertificate(is);
        is.close();

        assertNotNull(certificate);
        assertEquals("1372807406", certificate.getSerialNumber().toString());

        // test input stream from raw PEM file
        is = getResourceInputStream("gateway.pem");
        certificate = gateway.readCertificate(is);
        is.close();

        assertNotNull(certificate);
        assertEquals("1372807406", certificate.getSerialNumber().toString());
    }


    String getBase64DerCert() throws Exception {
        InputStream is = getResourceInputStream("gateway.cer");

        // base64 encode the DER data
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int read;
        byte[] data = new byte[1024];
        while ((read = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, read);
        }
        buffer.flush();
        byte[] bytes = buffer.toByteArray();

        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    String getPemCertString() throws Exception {
        InputStream is = getResourceInputStream("gateway.pem");

        // base64 encode the DER data
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int read;
        byte[] data = new byte[1024];
        while ((read = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, read);
        }
        buffer.flush();
        byte[] bytes = buffer.toByteArray();

        return new String(bytes, "UTF-8");
    }

    InputStream getResourceInputStream(String name) {
        return getClass().getResourceAsStream(name);
    }
}

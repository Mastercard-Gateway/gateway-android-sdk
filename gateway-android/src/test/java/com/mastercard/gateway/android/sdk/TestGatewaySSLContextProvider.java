package com.mastercard.gateway.android.sdk;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class TestGatewaySSLContextProvider {

    GatewaySSLContextProvider trustProvider;

    @Before
    public void setUp() throws Exception {
        trustProvider = spy(new GatewaySSLContextProvider());
    }

    @Test
    public void testCreateSslKeystoreContainsInternalCertificate() throws Exception {
        doReturn(mock(X509Certificate.class)).when(trustProvider).readCertificate(any());

        KeyStore keyStore = trustProvider.createKeyStore();

        assertTrue(keyStore.containsAlias("gateway.mastercard.com"));
    }

    @Test
    public void testReadingInternalCertificateWorksAsExpected() throws Exception {
        X509Certificate certificate = trustProvider.readCertificate(GatewaySSLContextProvider.INTERMEDIATE_CA);
        String expectedSerialNo = "1372807406";

        assertNotNull(certificate);
        assertEquals(expectedSerialNo, certificate.getSerialNumber().toString());
    }
}

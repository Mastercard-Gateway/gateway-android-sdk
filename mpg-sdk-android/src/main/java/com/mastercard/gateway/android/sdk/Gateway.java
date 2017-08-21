package com.mastercard.gateway.android.sdk;


import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class Gateway {



    Response executePost(String tag, URL url, String data) throws Exception {
        // init ssl context with limiting trust managers
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, createTrustManagers(), null);

        // init connection
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        if (url.getProtocol().startsWith("https")) {
            ((HttpsURLConnection) c).setSSLSocketFactory(context.getSocketFactory());
        }
        c.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
        c.setReadTimeout(Constants.SOCKET_TIMEOUT);
        c.setRequestProperty("User-Agent", Constants.USER_AGENT);
        c.setRequestProperty("Content-Type", "application/json");
        c.setDoInput(true);
        c.setDoOutput(true);
        c.setRequestMethod("POST");

        // log request
        logRequest(tag, c, data);

        // write data
        if (data != null) {
            OutputStream os = c.getOutputStream();
            os.write(data.getBytes("UTF-8"));
            os.close();
        }

        c.connect();

        Response response = new Response(c);

        c.disconnect();

        // log response
        logResponse(tag, response);

        return response;
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
        keyStore.setCertificateEntry(Constants.KEYSTORE_CA_ALIAS, readCertificate(Constants.INTERMEDIATE_CA));

        return keyStore;
    }

    Certificate readCertificate(String pemCert) throws CertificateException {
        // add our trusted cert to the keystore
        ByteArrayInputStream is = new ByteArrayInputStream(Base64.decode(pemCert, Base64.DEFAULT));
        return CertificateFactory.getInstance("X.509").generateCertificate(is);
    }

    void logRequest(String tag, HttpURLConnection c, String data) {
        Log.d(tag, "REQUEST: " + c.getRequestMethod() + " " + c.getURL().toString());

        if (data != null) {
            Log.d(tag, "-- Data: " + data);
        }

        // log request headers
        Map<String, List<String>> properties = c.getRequestProperties();
        Set<String> keys = properties.keySet();
        for (String key : keys) {
            List<String> values = properties.get(key);
            for (String value : values) {
                Log.d(tag, "-- " + key + ": " + value);
            }
        }
    }


    void logResponse(String tag, Response response) {
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
            Log.d(tag, part);
        }
    }
}

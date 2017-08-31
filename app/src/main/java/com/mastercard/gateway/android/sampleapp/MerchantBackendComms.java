package com.mastercard.gateway.android.sampleapp;

import android.util.Base64;

import com.mastercard.gateway.android.sdk.CommsException;
import com.mastercard.gateway.android.sdk.CommsTimeoutException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import static android.text.TextUtils.isEmpty;

/**
 * Handles communication with the merchant backend
 */
public class MerchantBackendComms {
    private String sslAlgorithm = "TLS";
    private KeyManager[] keyManagers = null;
    private TrustManager[] trustManagers = null;
    private SecureRandom randomnessSource = null;
    private int timeoutMillis = 0; // no timeout by default

    private static boolean contains( int[] haystack, int needle ) {
        for ( int candidate : haystack ) {
            if ( candidate == needle ) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return the algorithm being used to manage connections; TLS by default
     */
    public String getSslAlgorithm() {
        return sslAlgorithm;
    }

    /**
     * @param sslAlgorithm the algorithm being used to manage connections; TLS by default
     */
    public void setSslAlgorithm( String sslAlgorithm ) {
        this.sslAlgorithm = sslAlgorithm;
    }

    /**
     * @return any custom key managers; null by default
     */
    public KeyManager[] getKeyManagers() {
        return keyManagers;
    }

    /**
     * @param keyManagers any custom key managers; null by default
     */
    public void setKeyManagers( KeyManager...keyManagers ) {
        this.keyManagers = keyManagers;
    }

    /**
     * @return any custom trust managers; null by default
     */
    public TrustManager[] getTrustManagers() {
        return trustManagers;
    }

    /**
     * @param trustManagers any custom trust managers; null by default
     */
    public void setTrustManagers( TrustManager...trustManagers ) {
        this.trustManagers = trustManagers;
    }

    /**
     * @return any custom source of randomness; null by default
     */
    public SecureRandom getRandomnessSource() {
        return randomnessSource;
    }

    /**
     * @param randomnessSource any custom source of randomness; null by default
     */
    public void setRandomnessSource( SecureRandom randomnessSource ) {
        this.randomnessSource = randomnessSource;
    }

    /**
     * @return timeout in milliseconds for connecting or reading data. 0 means no timeout.
     */
    public int getTimeoutMilliseconds() {
        return timeoutMillis;
    }

    /**
     * @param millis timeout in milliseconds for connecting or reading data. 0 means no timeout.
     */
    public void setTimeoutMilliseconds( int millis ) {
        this.timeoutMillis = millis;
    }

    /**
     * Initialise a new SSL context using the algorithm, key manager(s), trust manager(s) and
     * source of randomness.
     *
     * @throws NoSuchAlgorithmException if the algorithm is not supported by the android platform
     * @throws KeyManagementException if initialization of the context fails
     */
    public void initialiseSslContext() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance( sslAlgorithm );
        context.init( keyManagers, trustManagers, randomnessSource );
        HttpsURLConnection.setDefaultSSLSocketFactory( context.getSocketFactory() );
    }

    /**
     * Open an HTTP or HTTPS connection to a particular URL
     * @param address a valid HTTP[S] URL to connect to
     * @return an HTTP or HTTPS connection as appropriate
     * @throws KeyManagementException if initialization of the SSL context fails
     * @throws NoSuchAlgorithmException if the SSL algorithm is not supported by the android platform
     * @throws MalformedURLException if the address was not in the HTTP or HTTPS scheme
     * @throws IOException if the connection could not be opened
     */
    public HttpURLConnection openConnection( URL address )
            throws KeyManagementException, NoSuchAlgorithmException, IOException {

        switch ( address.getProtocol().toUpperCase() ) {
            case "HTTPS":
                initialiseSslContext();
                break;
            case "HTTP":
                break;
            default:
                throw new MalformedURLException( "Not an HTTP[S] address" );
        }

        HttpURLConnection connection = (HttpURLConnection) address.openConnection();
        connection.setConnectTimeout( timeoutMillis );
        connection.setReadTimeout( timeoutMillis );
        return connection;
    }

    /**
     * Send a JSON object to an open HTTP[S] connection
     *
     * @param connection an open HTTP[S] connection, as returned by {@link #openConnection(URL)}
     * @param method an HTTP method, e.g. PUT, POST or GET
     * @param json a valid JSON-formatted object
     * @param username user name for basic authorization (can be null for no auth)
     * @param password password for basic authorization (can be null for no auth)
     * @return an HTTP response code
     * @throws IOException if the connection could not be written to
     */
    public int makeJsonRequest( HttpURLConnection connection, String method, String json,
                                String username, String password ) throws IOException {

        connection.setDoOutput( true );
        connection.setRequestMethod( method );
        connection.setFixedLengthStreamingMode( json.getBytes().length );
        connection.setRequestProperty( "Content-Type", "application/json" );

        if ( !isEmpty( username ) && !isEmpty( password ) ) {
            String basicAuth = username + ':' + password;
            basicAuth = Base64.encodeToString( basicAuth.getBytes(), Base64.DEFAULT );
            connection.setRequestProperty( "Authorization", "Basic " + basicAuth );
        }

        PrintWriter out = new PrintWriter( connection.getOutputStream() );
        out.print( json );
        out.close();

        return connection.getResponseCode();
    }

    /**
     * Retrieve a JSON response from an open HTTP[S] connection. This would typically be called
     * after {@link #makeJsonRequest(HttpURLConnection, String, String, String, String)}
     *
     * @param connection an open HTTP[S] connection
     * @return a json object in string form
     * @throws IOException if the connection could not be read from
     */
    public String getJsonResponse( HttpURLConnection connection ) throws IOException {
        StringBuilder responseOutput = new StringBuilder();
        String line;
        BufferedReader br = null;

        try {
            // If the HTTP response code is 4xx or 5xx, we need error rather than input stream
            InputStream stream = ( connection.getResponseCode() < 400 )
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            br = new BufferedReader( new InputStreamReader( stream ) );

            while ( ( line = br.readLine() ) != null ) {
                responseOutput.append( line );
            }
        }
        finally {
            if ( br != null ) {
                try {
                    br.close();
                }
                catch ( Exception e ) {
                    /* Ignore close exception */
                }
            }
        }

        return responseOutput.toString();
    }

    /**
     * End-to-end method to send some json to an url and retrieve a response
     *
     * @param address url to send the request to
     * @param jsonRequest a valid JSON-formatted object
     * @param httpMethod an HTTP method, e.g. PUT, POST or GET
     * @param username user name for basic authorization (can be null for no auth)
     * @param password password for basic authorization (can be null for no auth)
     * @param expectResponseCodes permitted HTTP response codes, e.g. HTTP_OK (200)
     * @return a json response object in string form
     * @throws CommsException if there was a problem opening a connection, writing to it, reading
     *      from it, or if the response code was not the one we expected.
     * @throws CommsTimeoutException if the connection timed out whilst connecting or reading
     */
    public String doJsonRequest( URL address, String jsonRequest, String httpMethod,
            String username, String password, int...expectResponseCodes ) throws CommsException {

        HttpURLConnection connection;
        int responseCode;

        try {
            connection = openConnection( address );
        }
        catch ( NoSuchAlgorithmException|KeyManagementException e ) {
            throw new CommsException( "Couldn't initialise SSL context", e );
        }
        catch ( IOException e ) {
            throw new CommsException( "Couldn't open an HTTP[S] connection", e );
        }

        try {
            responseCode =
                    makeJsonRequest( connection, httpMethod, jsonRequest, username, password );

            if ( !contains( expectResponseCodes, responseCode ) ) {
                throw new CommsException( "Unexpected response code " + responseCode );
            }
        }
        catch ( SocketTimeoutException e ) {
            throw new CommsTimeoutException( "Timeout whilst sending JSON data" );
        }
        catch ( IOException e ) {
            throw new CommsException( "Error sending JSON data", e );
        }

        try {
            String responseBody = getJsonResponse( connection );

            if ( responseBody == null ) {
                throw new CommsException( "No data in response" );
            }

            return responseBody;
        }
        catch ( SocketTimeoutException e ) {
            throw new CommsTimeoutException( "Timeout whilst retrieving JSON response" );
        }
        catch ( IOException e ) {
            throw new CommsException( "Error retrieving JSON response", e );
        }
    }
}

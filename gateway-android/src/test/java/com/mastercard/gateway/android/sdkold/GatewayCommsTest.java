package com.mastercard.gateway.android.sdkold;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import static com.mastercard.gateway.android.sdkold.GatewayComms.PERMITTED_RESPONSE_CODES;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests over the comms module
 */
public class GatewayCommsTest {
    private GatewayComms comms;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        comms = new GatewayComms();
    }

    @Test
    public void testSetGetSslAlgorithm() {
        assertEquals( "Unexpected algorithm", "TLS", comms.getSslAlgorithm() );
        comms.setSslAlgorithm( "Al Gore Rhythm" );
        assertEquals( "SSL algorithm not set", "Al Gore Rhythm", comms.getSslAlgorithm() );
    }

    @Test
    public void testSetGetKeyManagers() {
        KeyManager km1 = new KeyManager() {};
        KeyManager km2 = new KeyManager() {};
        assertNull( "Unexpected key manager", comms.getKeyManagers() );
        comms.setKeyManagers( km1, km2 );

        assertArrayEquals( "Key managers not set",
                new KeyManager[] { km1, km2 }, comms.getKeyManagers() );
    }

    @Test
    public void testSetGetTrustManagers() {
        TrustManager tm1 = new TrustManager() {};
        TrustManager tm2 = new TrustManager() {};
        assertNull( "Unexpected trust manager", comms.getTrustManagers() );
        comms.setTrustManagers( tm1, tm2 );

        assertArrayEquals( "Trust managers not set",
                new TrustManager[] { tm1, tm2 }, comms.getTrustManagers() );
    }

    @Test
    public void testSetGetRandomnessSource() {
        SecureRandom sr = new SecureRandom();
        assertNull( "Unexpected source of randomness", comms.getRandomnessSource() );
        comms.setRandomnessSource( sr );
        assertSame( "Source of randomness not set", sr, comms.getRandomnessSource() );
    }

    @Test
    public void testSetGetTimeoutMilliseconds() {
        assertEquals( "Unexpected timeout", 0, comms.getTimeoutMilliseconds() );
        comms.setTimeoutMilliseconds( 5000 );
        assertEquals( "Timeout not set", 5000, comms.getTimeoutMilliseconds() );
    }

    @Test
    public void testSetGetNumberAttempts() {
        assertEquals( "Unexpected number of attempts", 1, comms.getNumberAttempts() );
        comms.setNumberAttempts( 5 );
        assertSame( "Number of attempts not set", 5, comms.getNumberAttempts() );
    }

    @Test ( expected = IllegalArgumentException.class )
    public void testSetNumberAttemptsInvalid() {
        comms.setNumberAttempts( 0 );
    }

    @Test
    public void testOpenConnectionHttp() throws Exception {
        URL httpUrl = new URL( "http://127.0.0.1" );
        GatewayComms commsSpy = Mockito.spy( comms );
        commsSpy.setTimeoutMilliseconds( 123 );
        HttpURLConnection connection = commsSpy.openConnection( httpUrl );
        assertFalse( "HTTPS connection", connection instanceof HttpsURLConnection );
        assertEquals( "Connect timeout", 123, connection.getConnectTimeout() );
        assertEquals( "Read timeout", 123, connection.getReadTimeout() );
        Mockito.verify( commsSpy, Mockito.never() ).initialiseSslContext();
    }

    @Test
    public void testOpenConnectionHttps() throws Exception {
        URL httpsUrl = new URL( "https://127.0.0.1" );
        GatewayComms commsSpy = Mockito.spy( comms );
        commsSpy.setTimeoutMilliseconds( 123 );
        HttpURLConnection connection = commsSpy.openConnection( httpsUrl );
        assertTrue( "HTTPS connection", connection instanceof HttpsURLConnection );
        assertEquals( "Connect timeout", 123, connection.getConnectTimeout() );
        assertEquals( "Read timeout", 123, connection.getReadTimeout() );
        Mockito.verify( commsSpy ).initialiseSslContext();
    }

    @Test ( expected = MalformedURLException.class )
    public void testOpenConnectionNotHttp() throws Exception {
        comms.openConnection( new URL( "ftp://127.0.0.1" ) );
    }

    @Test
    public void testMakeJsonRequest() throws Exception {
        HttpsURLConnection connection = Mockito.mock( HttpsURLConnection.class );
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        String json = "{\"foo\": 1}";
        int len = json.length();

        Mockito.when( connection.getOutputStream() ).thenReturn( stream );
        Mockito.when( connection.getResponseCode() ).thenReturn( HttpURLConnection.HTTP_ACCEPTED );

        assertEquals( "HttpResponse code", HttpURLConnection.HTTP_ACCEPTED,
                comms.makeJsonRequest( connection, "POST", json ) );

        assertEquals( "Output", json, stream.toString() );

        Mockito.verify( connection ).setDoOutput( true );
        Mockito.verify( connection ).setRequestMethod( "POST" );
        Mockito.verify( connection ).setFixedLengthStreamingMode( len );
        Mockito.verify( connection ).setRequestProperty( "Content-Type", "application/json" );
    }

    @Test ( expected = SocketTimeoutException.class )
    public void testMakeJsonRequestConnectTimeout() throws Exception {
        HttpsURLConnection connection = Mockito.mock( HttpsURLConnection.class );
        Mockito.when( connection.getOutputStream() ).thenThrow( SocketTimeoutException.class );

        comms.makeJsonRequest( connection, "POST", "{\"foo\": 1}" );
    }

    @Test
    public void testGetJsonResponse() throws Exception {
        HttpsURLConnection connection = Mockito.mock( HttpsURLConnection.class );
        String json = "{\"bar\": 2}";

        Mockito.when( connection.getResponseCode() ).thenReturn( HttpURLConnection.HTTP_ACCEPTED );
        Mockito.when( connection.getInputStream() ).thenReturn(
                new ByteArrayInputStream( json.getBytes() ) );

        assertEquals( "Unexpected output", json, comms.getJsonResponse( connection ) );
    }

    @Test
    public void testGetJsonResponseError() throws Exception {
        HttpsURLConnection connection = Mockito.mock( HttpsURLConnection.class );
        String json = "{\"bar\": 2}";

        Mockito.when( connection.getResponseCode() ).thenReturn( HttpURLConnection.HTTP_BAD_REQUEST );
        Mockito.when( connection.getErrorStream() ).thenReturn(
                new ByteArrayInputStream( json.getBytes() ) );

        assertEquals( "Unexpected output", json, comms.getJsonResponse( connection ) );
    }

    @Test ( expected = SocketTimeoutException.class )
    public void testGetJsonResponseReadTimeout() throws Exception {
        HttpsURLConnection connection = Mockito.mock( HttpsURLConnection.class );
        Mockito.when( connection.getResponseCode() ).thenReturn( HttpURLConnection.HTTP_ACCEPTED );
        Mockito.when( connection.getInputStream() ).thenThrow( SocketTimeoutException.class );

        comms.getJsonResponse( connection );
    }

    @Test
    public void testDoJsonRequest() throws Exception {
        GatewayComms commsSpy = Mockito.spy( comms );
        HttpsURLConnection connection = Mockito.mock( HttpsURLConnection.class );

        URL address = new URL( "https://banana.com" );
        String jsonReq = "{\"foo\": 1}";
        String jsonResp = "{\"foo\": 1}";

        Mockito.doReturn( connection ).when( commsSpy ).openConnection( address );
        Mockito.doReturn( HttpURLConnection.HTTP_ACCEPTED ).when( commsSpy )
                .makeJsonRequest( connection, "POST", jsonReq );

        Mockito.doReturn( jsonResp ).when( commsSpy ).getJsonResponse( connection );

        assertEquals( "Unexpected response", jsonResp, commsSpy.doJsonRequest(
                address, jsonReq, "POST", HttpURLConnection.HTTP_ACCEPTED ) );
    }

    @Test
    public void testDoJsonRequestMultipleAccepted() throws Exception {
        GatewayComms commsSpy = Mockito.spy( comms );
        HttpsURLConnection connection = Mockito.mock( HttpsURLConnection.class );

        URL address = new URL( "https://banana.com" );
        String jsonReq = "{\"foo\": 1}";
        String jsonResp = "{\"foo\": 1}";

        Mockito.doReturn( connection ).when( commsSpy ).openConnection( address );
        Mockito.doReturn( HttpURLConnection.HTTP_ACCEPTED ).when( commsSpy )
                .makeJsonRequest( connection, "POST", jsonReq );

        Mockito.doReturn( jsonResp ).when( commsSpy ).getJsonResponse( connection );

        assertEquals( "Unexpected response", jsonResp, commsSpy.doJsonRequest(
                address, jsonReq, "POST", HttpURLConnection.HTTP_FORBIDDEN,
                HttpURLConnection.HTTP_PARTIAL, HttpURLConnection.HTTP_ACCEPTED ) );
    }

    @Test
    public void testDoJsonRequestIOExReturn() throws Exception {
        expectedException.expect( CommsException.class );
        expectedException.expectCause( isA( IOException.class ) );
        expectedException.expectMessage( "Error retrieving JSON response" );

        GatewayComms commsSpy = Mockito.spy( comms );
        HttpsURLConnection connection = Mockito.mock( HttpsURLConnection.class );

        URL address = new URL( "https://banana.com" );
        String jsonReq = "{\"foo\": 1}";

        Mockito.doReturn( connection ).when( commsSpy ).openConnection( address );
        Mockito.doReturn( HttpURLConnection.HTTP_ACCEPTED ).when( commsSpy )
                .makeJsonRequest( connection, "POST", jsonReq );

        Mockito.doThrow( IOException.class ).when( commsSpy ).getJsonResponse( connection );

        commsSpy.doJsonRequest( address, jsonReq, "POST", HttpURLConnection.HTTP_ACCEPTED );
    }

    @Test
    public void testDoJsonRequestReadTimeout() throws Exception {
        expectedException.expect( CommsTimeoutException.class );
        expectedException.expectMessage( "Timeout whilst retrieving JSON response" );

        GatewayComms commsSpy = Mockito.spy( comms );
        HttpsURLConnection connection = Mockito.mock( HttpsURLConnection.class );

        URL address = new URL( "https://banana.com" );
        String jsonReq = "{\"foo\": 1}";

        Mockito.doReturn( connection ).when( commsSpy ).openConnection( address );
        Mockito.doReturn( HttpURLConnection.HTTP_ACCEPTED ).when( commsSpy )
                .makeJsonRequest( connection, "POST", jsonReq );

        Mockito.doThrow( SocketTimeoutException.class ).when( commsSpy )
                .getJsonResponse( connection );

        commsSpy.doJsonRequest( address, jsonReq, "POST", HttpURLConnection.HTTP_ACCEPTED );
    }

    @Test
    public void testDoJsonRequestNullReturn() throws Exception {
        expectedException.expect( CommsException.class );
        expectedException.expectMessage( "No data in response" );

        GatewayComms commsSpy = Mockito.spy( comms );
        HttpsURLConnection connection = Mockito.mock( HttpsURLConnection.class );

        URL address = new URL( "https://banana.com" );
        String jsonReq = "{\"foo\": 1}";

        Mockito.doReturn( connection ).when( commsSpy ).openConnection( address );
        Mockito.doReturn( HttpURLConnection.HTTP_ACCEPTED ).when( commsSpy )
                .makeJsonRequest( connection, "POST", jsonReq );

        Mockito.doReturn( null ).when( commsSpy ).getJsonResponse( connection );

        commsSpy.doJsonRequest( address, jsonReq, "POST", HttpURLConnection.HTTP_ACCEPTED );
    }

    @Test
    public void testDoJsonRequestIOExRequest() throws Exception {
        expectedException.expect( CommsException.class );
        expectedException.expectCause( isA( IOException.class ) );
        expectedException.expectMessage( "Error sending JSON data" );

        GatewayComms commsSpy = Mockito.spy( comms );
        HttpsURLConnection connection = Mockito.mock( HttpsURLConnection.class );

        URL address = new URL( "https://banana.com" );
        String jsonReq = "{\"foo\": 1}";

        Mockito.doReturn( connection ).when( commsSpy ).openConnection( address );
        Mockito.doThrow( IOException.class ).when( commsSpy )
                .makeJsonRequest( connection, "POST", jsonReq );

        commsSpy.doJsonRequest( address, jsonReq, "POST", HttpURLConnection.HTTP_ACCEPTED );
    }

    @Test
    public void testDoJsonRequestConnectTimeout() throws Exception {
        expectedException.expect( CommsTimeoutException.class );
        expectedException.expectMessage( "Timeout whilst sending JSON data" );

        GatewayComms commsSpy = Mockito.spy( comms );
        HttpsURLConnection connection = Mockito.mock( HttpsURLConnection.class );

        URL address = new URL( "https://banana.com" );
        String jsonReq = "{\"foo\": 1}";

        Mockito.doReturn( connection ).when( commsSpy ).openConnection( address );
        Mockito.doThrow( SocketTimeoutException.class ).when( commsSpy )
                .makeJsonRequest( connection, "POST", jsonReq );

        commsSpy.doJsonRequest( address, jsonReq, "POST", HttpURLConnection.HTTP_ACCEPTED );
    }

    @Test
    public void testDoJsonRequestBadStatusRequest() throws Exception {
        expectedException.expect( CommsException.class );
        expectedException.expectMessage( "Unexpected response code " +
                HttpURLConnection.HTTP_INTERNAL_ERROR );

        GatewayComms commsSpy = Mockito.spy( comms );
        HttpsURLConnection connection = Mockito.mock( HttpsURLConnection.class );

        URL address = new URL( "https://banana.com" );
        String jsonReq = "{\"foo\": 1}";

        Mockito.doReturn( connection ).when( commsSpy ).openConnection( address );
        Mockito.doReturn( HttpURLConnection.HTTP_INTERNAL_ERROR ).when( commsSpy )
                .makeJsonRequest( connection, "POST", jsonReq );

        commsSpy.doJsonRequest( address, jsonReq, "POST", HttpURLConnection.HTTP_ACCEPTED );
    }

    @Test
    public void testDoJsonRequestIOExOpen() throws Exception {
        expectedException.expect( CommsException.class );
        expectedException.expectCause( isA( IOException.class ) );
        expectedException.expectMessage( "Couldn't open an HTTP[S] connection" );

        GatewayComms commsSpy = Mockito.spy( comms );
        URL address = new URL( "https://banana.com" );

        Mockito.doThrow( IOException.class ).when( commsSpy ).openConnection( address );

        commsSpy.doJsonRequest( address,
                "{\"foo\": 1}", "POST", HttpURLConnection.HTTP_ACCEPTED );
    }

    @Test
    public void testDoJsonRequestNoSuchAlgEx() throws Exception {
        expectedException.expect( CommsException.class );
        expectedException.expectCause( isA( NoSuchAlgorithmException.class ) );
        expectedException.expectMessage( "Couldn't initialise SSL context" );

        GatewayComms commsSpy = Mockito.spy( comms );
        Mockito.doThrow( NoSuchAlgorithmException.class ).when( commsSpy ).initialiseSslContext();

        commsSpy.doJsonRequest( new URL( "https://banana.com" ),
                "{\"foo\": 1}", "POST", HttpURLConnection.HTTP_ACCEPTED );
    }

    @Test
    public void testDoJsonRequestKeyManEx() throws Exception {
        expectedException.expect( CommsException.class );
        expectedException.expectCause( isA( KeyManagementException.class ) );
        expectedException.expectMessage( "Couldn't initialise SSL context" );

        GatewayComms commsSpy = Mockito.spy( comms );
        Mockito.doThrow( KeyManagementException.class ).when( commsSpy ).initialiseSslContext();

        commsSpy.doJsonRequest( new URL( "https://banana.com" ),
                "{\"foo\": 1}", "POST", HttpURLConnection.HTTP_ACCEPTED );
    }

    @Test
    public void testSendOnce() throws Exception {
        GatewayComms commsSpy = Mockito.spy( comms );
        URL address = new URL( "http://banana" );

        Mockito.doReturn( address ).when( commsSpy ).pathToUrl( "http://banana" );
        Mockito.doReturn( "jsonResponse" ).when( commsSpy )
                .doJsonRequest( address, "jsonRequest", "PUT", PERMITTED_RESPONSE_CODES );

        assertEquals( "jsonResponse", commsSpy.send( "PUT", "http://banana", "jsonRequest" ) );
        assertEquals( "Number of attempts", 1, commsSpy.getMetrics().getLastAttemptCount() );
    }

    @Test
    public void testSendOnceError() throws Exception {
        GatewayComms commsSpy = Mockito.spy( comms );
        URL address = new URL( "http://banana" );

        Mockito.doReturn( address ).when( commsSpy ).pathToUrl( "http://banana" );
        Mockito.doThrow( CommsException.class ).when( commsSpy )
                .doJsonRequest( address, "jsonRequest", "PUT", PERMITTED_RESPONSE_CODES );

        try {
            commsSpy.send( "PUT", "http://banana", "jsonRequest" );
            fail( "Shouldn't have succeeded" );
        }
        catch ( CommsException e ) {
            assertEquals( "Number of attempts", 1, commsSpy.getMetrics().getLastAttemptCount() );
        }
    }

    @Test
    public void testSendMultipleAttemptsSucceed() throws Exception {
        GatewayComms commsSpy = Mockito.spy( comms );
        URL address = new URL( "http://banana" );
        commsSpy.setNumberAttempts( 3 );

        Mockito.doReturn( address ).when( commsSpy ).pathToUrl( "http://banana" );
        Mockito.doThrow( CommsException.class ) // #1
                .doThrow( CommsTimeoutException.class ) // #2
                .doReturn( "jsonResponse" ) // #3
                .when( commsSpy )
                .doJsonRequest( address, "jsonRequest", "PUT", PERMITTED_RESPONSE_CODES );

        assertEquals( "jsonResponse", commsSpy.send( "PUT", "http://banana", "jsonRequest" ) );
        assertEquals( "Number of attempts", 3, commsSpy.getMetrics().getLastAttemptCount() );
    }

    @Test
    public void testSendMultipleAttemptsSucceedFirstTime() throws Exception {
        GatewayComms commsSpy = Mockito.spy( comms );
        URL address = new URL( "http://banana" );
        commsSpy.setNumberAttempts( 3 );

        Mockito.doReturn( address ).when( commsSpy ).pathToUrl( "http://banana" );
        Mockito.doReturn( "jsonResponse" ).when( commsSpy )
                .doJsonRequest( address, "jsonRequest", "PUT", PERMITTED_RESPONSE_CODES );

        assertEquals( "jsonResponse", commsSpy.send( "PUT", "http://banana", "jsonRequest" ) );
        assertEquals( "Number of attempts", 1, commsSpy.getMetrics().getLastAttemptCount() );
    }

    @Test
    public void testSendMultipleAttemptsFail() throws Exception {
        GatewayComms commsSpy = Mockito.spy( comms );
        URL address = new URL( "http://banana" );
        commsSpy.setNumberAttempts( 2 );

        Mockito.doReturn( address ).when( commsSpy ).pathToUrl( "http://banana" );
        Mockito.doThrow( CommsException.class ) // #1
                .doThrow( CommsTimeoutException.class ) // #2
                .doReturn( "jsonResponse" ) // #3 (never reached)
                .when( commsSpy )
                .doJsonRequest( address, "jsonRequest", "PUT", PERMITTED_RESPONSE_CODES );

        try {
            commsSpy.send( "PUT", "http://banana", "jsonRequest" );
            fail( "Shouldn't have succeeded" );
        }
        catch ( CommsException e ) {
            assertTrue( "Error class", e instanceof CommsTimeoutException );
            assertEquals("Number of attempts", 2, commsSpy.getMetrics().getLastAttemptCount());
        }
    }
}

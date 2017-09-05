package com.mastercard.gateway.android.sdkold;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mastercard.gateway.android.sdkold.merchant.session.UpdateSessionWithPayerDataRequest;
import com.mastercard.gateway.android.sdkold.merchant.session.UpdateSessionWithPayerDataResponse;
import com.mastercard.gateway.android.sdkold.merchant.session.WithSessionResource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class WithSessionResourceTest extends AbstractResourceTest {
    private static final String MERCH = "MERCHANT12345";
    private static final String SESS = "SESSION1231532451";

    private WithSessionResource session;
    private GatewayComms comms;
    private Gson gson;
    private UpdateSessionWithPayerDataRequest request;

    @Before
    public void setup() throws IOException {
        comms = Mockito.mock( GatewayComms.class );
        session = new WithSessionResource( comms, MERCH, SESS );
        session.setBaseURL( "http://banana" );
        gson = new GsonBuilder().create();

        String jsonRequest = slurpCondensed( "src/test/res/request.json" );

        request = gson.fromJson( jsonRequest, UpdateSessionWithPayerDataRequest.class );
    }

    @Test
    public void testSendUpdateSessionRequest() throws  Exception {
        String jsonResponse = slurpCondensed( "src/test/res/response_correct.json" );

        Mockito.when( comms.send( "PUT", "http://banana/merchant/" + MERCH + "/session/" + SESS,
                gson.toJson( request ) ) ).thenReturn( jsonResponse );

        UpdateSessionWithPayerDataResponse response = session.updateSessionWithPayerData( request );

        assertNotNull( "Null response", response );
        assertEquals( "Session ID", "SESSION00112233445566778899", response.getSession() );
        assertEquals( "Version", "444", response.getVersion() );
        assertNull( "Correlation ID", response.getCorrelationId() );
    }

    @Test
    public void testSendUpdateSessionRequestError() throws Exception {
        String jsonResponse = slurpCondensed( "src/test/res/response_error.json" );

        Mockito.when( comms.send( "PUT", "http://banana/merchant/" + MERCH + "/session/" + SESS,
                gson.toJson( request ) ) ).thenReturn( jsonResponse );

        try {
            session.updateSessionWithPayerData( request );
            fail( "Expected to fail with GatewayErrorException" );
        }
        catch ( GatewayErrorException e ) {
            assertNotNull( "Error response", e.getResponse() );
            assertEquals( "Result", "ERROR", e.getResponse().getResult().toString() );
            assertEquals( "Cause", "INVALID_REQUEST", e.getResponse().getError().getCause().toString() );
            assertEquals( "Explanation", "Value '4' is invalid.",
                    e.getResponse().getError().getExplanation() );
            assertEquals( "Field", "sourceOfFunds.provided.card.number",
                    e.getResponse().getError().getField() );
            assertEquals( "Validation type", "INVALID",
                    e.getResponse().getError().getValidationType().toString() );
            assertNull( "Support code", e.getResponse().getError().getSupportCode() );
        }
    }

    @Test
    public void testSendUpdateSessionRequestEmpty() throws Exception {
        Mockito.when( comms.send( "PUT", "http://banana/merchant/" + MERCH + "/session/" + SESS,
                gson.toJson( request ) ) ).thenReturn( "" );

        assertNull( "Expected null response", session.updateSessionWithPayerData( request ) );
    }

    @Test ( expected = JsonSyntaxException.class )
    public void testSendUpdateSessionRequestMalformed() throws Exception {
        Mockito.when( comms.send( "PUT", "http://banana/merchant/" + MERCH + "/session/" + SESS,
                gson.toJson( request ) ) ).thenReturn( "Pardon?" );

        session.updateSessionWithPayerData( request );
    }

    @Test ( expected = JsonParseException.class )
    public void testSendUpdateSessionRequestBadFormat() throws Exception {
        String jsonResponse = slurpCondensed( "src/test/res/response_badformat.json" );

        Mockito.when( comms.send( "PUT", "http://banana/merchant/" + MERCH + "/session/" + SESS,
                gson.toJson( request ) ) ).thenReturn( jsonResponse );

        session.updateSessionWithPayerData( request );
    }
}

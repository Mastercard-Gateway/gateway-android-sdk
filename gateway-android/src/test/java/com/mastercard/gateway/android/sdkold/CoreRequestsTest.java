package com.mastercard.gateway.android.sdkold;

import com.mastercard.gateway.android.sdkold.merchant.session.UpdateSessionWithPayerDataRequest;
import com.mastercard.gateway.android.sdkold.merchant.session.updatesessionwithpayerdatarequest.sourceoffunds.provided.Card;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CoreRequestsTest {
    @Test
    public void bankCardUpdateTest() throws Exception {
        UpdateSessionWithPayerDataRequest request = CoreRequests.bankCardUpdateSessionRequest(
                "Bobert Boberton", "1234000043210000", "909", "08", "19" );

        assertNotNull( "Request", request );
        assertEquals( "API Operation", "UPDATE_PAYER_DATA", request.getApiOperation() );
        assertNotNull( "Source of funds", request.getSourceOfFunds() );
        assertNotNull( "Provided source of funds", request.getSourceOfFunds().getProvided() );

        Card card = request.getSourceOfFunds().getProvided().getCard();
        assertNotNull( "Card", card );
        assertEquals( "Name on card", "Bobert Boberton", card.getNameOnCard() );
        assertEquals( "Card number", "1234000043210000", card.getNumber() );
        assertEquals( "Security code", "909", card.getSecurityCode() );
        assertNotNull( "Expiry", card.getExpiry() );
        assertEquals( "Expiry month", "08", card.getExpiry().getMonth() );
        assertEquals( "Expiry year", "19", card.getExpiry().getYear() );
    }
}
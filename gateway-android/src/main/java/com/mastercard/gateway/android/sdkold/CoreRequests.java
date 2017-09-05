package com.mastercard.gateway.android.sdkold;

import com.mastercard.gateway.android.sdkold.merchant.session.UpdateSessionWithPayerDataRequest;
import com.mastercard.gateway.android.sdkold.merchant.session.WithSessionResource;

/**
 * Convenience methods for building gateway requests
 */
public class CoreRequests {

    private CoreRequests() {
        // Cannot be instantiated
    }

    /**
     * Construct a simple session update request with bank card information
     *
     * @param nameOnCard the cardholder's name as printed on the card
     * @param cardNumber the card number (PAN)
     * @param securityCode the card verification code, as printed on the back or front of the card
     * @param expiryMM the month in numeric form; 01=Jan to 12=Dec
     * @param expiryYY the double-digit year
     * @return an update session request which can be fed into
     *    {@link WithSessionResource#updateSessionWithPayerData(UpdateSessionWithPayerDataRequest)}
     */
    public static UpdateSessionWithPayerDataRequest bankCardUpdateSessionRequest(
            String nameOnCard, String cardNumber, String securityCode,
            String expiryMM, String expiryYY ) {

        UpdateSessionWithPayerDataRequest request = new UpdateSessionWithPayerDataRequest();
        request
          .apiOperation( "UPDATE_PAYER_DATA" )
          .sourceOfFunds()
            .provided()
              .card()
                .nameOnCard( nameOnCard )
                .number( cardNumber )
                .securityCode( securityCode )
                .expiry()
                  .month( expiryMM )
                  .year( expiryYY );

        return request;
    }
}

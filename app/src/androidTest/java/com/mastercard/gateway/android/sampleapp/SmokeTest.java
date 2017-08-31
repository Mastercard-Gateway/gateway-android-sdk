package com.mastercard.gateway.android.sampleapp;

import org.junit.Before;
import org.junit.Test;

/**
 * Test the results page goes onto the main page
 */
public class SmokeTest extends TestBase {
    private static final String THIRTY_SECONDS = Integer.toString( 30 * 1000 );

    @Override
    protected long pauseMultiplier() {
        return 0;
    }

    @Before
    public void setup() {
        resetSettings();
        setStringSetting( "pref_key_timeout", THIRTY_SECONDS );
    }

    @Test
    public void fullSmokeTest() {
        new CheckoutState()
                .confirmMainState()
                .selectStandardProduct()
                .confirmCaptureState()
                .addPaymentDetails( "4012000033330026" )
                .confirmPayState()
                .checkCardNumAndPay( "************0026" )
                .confirmResultState()
                .assertSuccessResult();
    }
}

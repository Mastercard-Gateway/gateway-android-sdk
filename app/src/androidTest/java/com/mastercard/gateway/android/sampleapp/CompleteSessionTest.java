package com.mastercard.gateway.android.sampleapp;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Attempt to finalise payment for various products
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CompleteSessionTest extends TestBase {

    @Before
    public void setup() {
        resetSettings();
    }

    protected ResultState buyProduct( String product ) {
        return new CheckoutState()
                .confirmMainState()
                .selectProduct( product )
                .confirmCaptureState()
                .addStandardPaymentDetails()
                .confirmPayState()
                .checkCardNumAndPay()
                .confirmResultState();
    }

    protected ResultState buyStandardProduct() {
        return buyProduct( "Product Alpha" );
    }

    @Test
    public void testPaySuccess() {
        buyStandardProduct().assertSuccessResult();
    }

    @Test
    public void testPayDenied() {
        buyProduct( "Product Delta" ).assertFailResult( R.string.pay_denied_text, "" );
    }

    @Test
    public void testPayError() {
        buyProduct( "Product Epsilon" )
                .assertFailResult( R.string.pay_error_text, R.string.pay_error_explanation );
    }

    @Test
    public void testPay404() {
        setStringSetting( "pref_key_complete_behaviour", str( R.string.behaviour_fail ) );

        buyStandardProduct().assertFailResult( R.string.pay_comms_error_text,
                R.string.pay_comms_error_explanation );
    }

    @Test
    public void testPayTimeout() {
        setStringSetting( "pref_key_complete_behaviour", str( R.string.behaviour_timeout ) );

        buyStandardProduct().assertFailResult( R.string.pay_timeout_text,
                R.string.pay_timeout_explanation );
    }
}

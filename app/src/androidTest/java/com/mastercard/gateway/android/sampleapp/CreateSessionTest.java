package com.mastercard.gateway.android.sampleapp;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Attempt to create sessions for various products
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateSessionTest extends TestBase {
    private MainState checkoutState;

    @Before
    public void setup() {
        resetSettings();
        checkoutState = new CheckoutState().confirmMainState();
    }

    @Test
    public void testCreateDenied() {
        checkoutState.selectProduct( "Product Beta" )
                .confirmResultState()
                .assertFailResult( R.string.create_denied_text,
                        R.string.create_denied_explanation );
    }

    @Test
    public void testCreateError() {
        checkoutState.selectProduct( "Product Gamma" )
                .confirmResultState()
                .assertFailResult( R.string.create_error_text, R.string.create_error_explanation );
    }

    @Test
    public void testCreateSuccess() {
        checkoutState.selectStandardProduct()
                .confirmCaptureState();
    }

    @Test
    public void testCreateCommsError() {
        setStringSetting( "pref_key_create_behaviour", str( R.string.behaviour_fail ) );
        checkoutState.selectStandardProduct()
                .confirmResultState()
                .assertFailResult( R.string.create_failed_text,
                        R.string.create_failed_explanation );
    }

    @Test
    public void testCreateTimeout() {
        setStringSetting( "pref_key_create_behaviour", str( R.string.behaviour_timeout ) );
        checkoutState.selectStandardProduct()
                .confirmResultState()
                .assertFailResult( R.string.create_timeout_text,
                        R.string.create_timeout_explanation );
    }

    @Test
    public void testCreateAuthFail() {
        setStringSetting( "pref_key_password", "OpenSesame?" );
        checkoutState.selectStandardProduct()
                .confirmResultState()
                .assertFailResult( R.string.create_error_text, R.string.create_error_explanation );
    }
}

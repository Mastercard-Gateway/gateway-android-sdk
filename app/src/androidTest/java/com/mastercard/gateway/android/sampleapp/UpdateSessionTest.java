package com.mastercard.gateway.android.sampleapp;

import android.test.suitebuilder.annotation.LargeTest;

import com.mastercard.gateway.android.sdk.GatewayComms;

import android.support.test.runner.AndroidJUnit4;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Attempt to update session with payment details, with various results
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UpdateSessionTest extends TestBase {
    private CaptureState checkoutState;

    @Before
    public void setup() {
        resetSettings();
        checkoutState = new CheckoutState()
                .confirmMainState()
                .selectStandardProduct()
                .confirmCaptureState();
    }

    @Test
    public void testUpdateValid() {
        checkoutState.addStandardPaymentDetails()
                .confirmPayState();
    }

    @Test
    public void testUpdateError() {
        checkoutState.addPaymentDetails("4929240084496399")
                .confirmResultState()
                .assertFailResult(R.string.update_error_text,
                        "Form Session not found or expired.");
    }

    @Test
    public void testUpdateCommsError() {
        setStringSetting("pref_key_update_behaviour", str(R.string.behaviour_fail));
        checkoutState.addStandardPaymentDetails()
                .confirmResultState()
                .assertFailResult(R.string.update_commserror_text,
                        R.string.update_commserror_explanation);
    }

    @Test
    public void testUpdateTimeout() {
        setStringSetting("pref_key_update_behaviour", str(R.string.behaviour_timeout));
        checkoutState.addStandardPaymentDetails()
                .confirmResultState()
                .assertFailResult(R.string.update_timeout_text,
                        R.string.update_timeout_explanation);
    }

    /**
     * The error text we expect back is coded in the Gateway simulator, based on real Gateway
     * responses. We show that a sensible validation error is shown if our PAN is
     * invalid.
     */
    @Test
    public void testUpdateValidationErr() {
        checkoutState.addPaymentDetails("4444")
                .confirmResultState()
                .assertFailResult(R.string.update_error_text,
                        "Value '4444' is invalid. Length is 4 characters, but must be at least 9");
    }

    /**
     * Another error response with text coming back from the Gateway simulator; this time
     * simulate a server error based on magic data.
     */
    @Test
    public void testUpdateServerError() {
        checkoutState.addPaymentDetails("4539075161798115")
                .confirmResultState()
                .assertFailResult(R.string.update_error_text, "Server busy");
    }

    private int getNumRequests() {
        return GatewayComms.globalMetrics().getLastAttemptCount();
    }

    @Test
    public void testUpdateRepeatedAttempts() {
        setStringSetting("pref_key_update_behaviour", str(R.string.behaviour_fail));
        setStringSetting("pref_key_num_update_attempts", "3");

        checkoutState.addStandardPaymentDetails()
                .confirmResultState()
                .assertFailResult(R.string.update_commserror_text,
                        R.string.update_commserror_explanation);

        assertEquals("Number of requests", 3, getNumRequests());
    }

    @Test
    public void testUpdateNoRetryBecauseSuccess() {
        setStringSetting("pref_key_num_update_attempts", "3");

        checkoutState.addStandardPaymentDetails()
                .confirmPayState();

        assertEquals("Number of requests", 1, getNumRequests());
    }

    /**
     * This test shows that we only do the retry when we got a comms error, not when we got back
     * a fail response.
     */
    @Test
    public void testUpdateNoRetryBecauseFail() {
        setStringSetting("pref_key_num_update_attempts", "3");

        checkoutState.addPaymentDetails("4539075161798115")
                .confirmResultState()
                .assertFailResult(R.string.update_error_text, "Server busy");

        assertEquals("Number of requests", 1, getNumRequests());
    }
}

package com.mastercard.gateway.android.sampleapp;

import android.support.test.espresso.NoActivityResumedException;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.pressBack;

/**
 * Test the results page goes onto the main page
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ResultActivityTest extends TestBase {
    private ResultState checkoutState;

    @Before
    public void setup() {
        resetSettings();
        checkoutState = new CheckoutState()
                .confirmMainState()
                .selectProduct( "Product Beta" )
                .confirmResultState();

        checkoutState.assertFailResult( R.string.create_denied_text,
                R.string.create_denied_explanation );
    }

    @Test
    public void testResultsPage() {
        checkoutState.startOver().confirmMainState();
    }

    /**
     * After the results page has been shown, the user should no longer be able to use the back
     * button to return to other screens
     */
    @Test ( expected = NoActivityResumedException.class )
    public void testNoBackButtonAfterResults() {
        checkoutState.startOver().confirmMainState();
        pressBack();
    }
}

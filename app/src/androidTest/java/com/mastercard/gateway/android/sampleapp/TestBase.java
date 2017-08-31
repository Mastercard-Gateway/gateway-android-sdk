package com.mastercard.gateway.android.sampleapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.v4.content.ContextCompat;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * Common base for Espresso tests, with utility methods
 */
public class TestBase {
    @Rule
    public ActivityTestRule<MainActivity> mainActivityTestRule =
            new ActivityTestRule<>( MainActivity.class );

    protected static final String HASHED_CARD_NO_1111 = "************1111";

    /** Represents a generic state of the checkout process */
    public class CheckoutState {
        public MainState confirmMainState() {
            checkViewAppears( R.id.mainView );
            return new MainState();
        }

        public CaptureState confirmCaptureState() {
            checkViewAppears( R.id.capturePaymentView );
            checkTextNotBlank( R.id.sessionText );
            return new CaptureState();
        }

        public ResultState confirmResultState() {
            checkViewAppears( R.id.resultView );
            return new ResultState();
        }

        public PayState confirmPayState() {
            checkViewAppears( R.id.payView );
            return new PayState();
        }
    }

    /** Represents the state of the checkout at the MainActivity screen */
    public class MainState extends CheckoutState {
        private MainState() {}

        public CheckoutState selectStandardProduct() {
            return selectProduct( "Product Alpha" );
        }

        public CheckoutState selectProduct( String productName ) {
            chooseValue( R.id.productChooser, productName );
            clickOn( R.id.buyButton );
            return new CheckoutState();
        }
    }

    /** Represents the state of the checkout at the PaymentCaptureActivity screen */
    public class CaptureState extends CheckoutState {
        private CaptureState() {}

        public CheckoutState addPaymentDetails( String name, String pan, String expiryMM,
                                                String expiryYY, String cvv ) {

            enterText( R.id.nameOnCard, name );
            enterText( R.id.cardnumber, pan );
            enterText( R.id.expiry_month, expiryMM );
            enterText( R.id.expiry_year, expiryYY );
            enterText( R.id.cvv, cvv );
            Espresso.closeSoftKeyboard();
            clickOn( R.id.submitButton );
            return new CheckoutState();
        }

        public CheckoutState addPaymentDetails( String pan ) {
            return addPaymentDetails( "Alice Aqua", pan, "12", "20", "123" );
        }

        public CheckoutState addStandardPaymentDetails() {
            return addPaymentDetails( "Alice Aqua", "4444333322221111", "12", "20", "123" );
        }
    }

    /** Represents the state of the checkout at the PayActivity screen */
    public class PayState extends CheckoutState {
        private PayState() {}

        public CheckoutState checkCardNumAndPay(String obscuredCardNum ) {
            checkText( R.id.confirmCardNo, obscuredCardNum );
            clickOn( R.id.confirmBtn );
            return new CheckoutState();
        }

        public CheckoutState checkCardNumAndPay() {
            return checkCardNumAndPay( HASHED_CARD_NO_1111 );
        }
    }

    /** Represents the state of the checkout at the ResultActivity screen */
    public class ResultState extends CheckoutState {
        private ResultState() {}

        public void assertResult( int bgColour, int resultText, int resultExplanation ) {
            checkViewBackgroundColor( R.id.resultView, bgColour, true );
            checkText( R.id.resultText, resultText );
            checkText( R.id.resultExplanation, resultExplanation );
        }

        public void assertResult( int bgColour, int resultText, String resultExplanation ) {
            checkViewBackgroundColor( R.id.resultView, bgColour, true );
            checkText( R.id.resultText, resultText );
            checkText( R.id.resultExplanation, resultExplanation );
        }

        public void assertFailResult( int resultText, int resultExplanation ) {
            assertResult( R.color.failed_bg, resultText, resultExplanation );
        }

        public void assertFailResult( int resultText, String resultExplanation ) {
            assertResult( R.color.failed_bg, resultText, resultExplanation );
        }

        public void assertSuccessResult() {
            assertResult( R.color.success_bg, R.string.pay_successful_text, "" );
        }

        public CheckoutState startOver() {
            clickOn( R.id.continueBtn );
            return new CheckoutState();
        }
    }

    protected long pauseMultiplier() { return 0; };

    protected void doPauseMaybe( long time ) {
        long pauseMulti = pauseMultiplier();

        if ( pauseMulti > 0 ) {
            try {
                Thread.sleep( pauseMulti * time );
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }
        }
    }

    protected SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences( mainActivityTestRule.getActivity() );
    }

    protected void resetSettings() {
        getPrefs().edit().clear().commit();
    }

    protected void setBooleanSetting( String settingName, boolean enabled ) {
        getPrefs().edit().putBoolean( settingName, enabled ).commit();
    }

    protected void setStringSetting( String settingName, String value ) {
        getPrefs().edit().putString( settingName, value ).commit();
    }

    protected String str( int stringResourceId ) {
        return mainActivityTestRule.getActivity().getResources().getString( stringResourceId );
    }

    protected int colour( int colourResourceId ) {
        Context context = mainActivityTestRule.getActivity().getApplicationContext();
        return ContextCompat.getColor( context, colourResourceId );
    }

    protected void chooseValue( int dataSourceId, String value ) {
        onView( withId( dataSourceId ) ).perform( click() );
        doPauseMaybe( 5 );
        onData( allOf( instanceOf( String.class ), is( value ) ) ).perform( click() );
        doPauseMaybe( 5 );
    }

    protected void clickOn( int buttonId ) {
        onView( withId( buttonId ) ).perform( click() );
        doPauseMaybe( 5 );
    }

    protected void enterText( int fieldId, String text ) {
        onView( withId( fieldId ) ).perform( typeText( text ) );
        doPauseMaybe( 2 );
    }

    protected void checkViewAppears( int viewId ) {
        onView( withId( viewId ) ).check( matches( isDisplayed() ) );
    }

    protected void checkViewDoesNotAppear( int viewId ) {
        onView( withId( viewId ) ).check( doesNotExist() );
    }

    protected void checkText( int fieldId, String text ) {
        onView( withId( fieldId ) ).check( matches( withText( text ) ) );
    }

    protected void checkText( int fieldId, int stringResource ) {
        onView( withId( fieldId ) ).check( matches( withText( stringResource ) ) );
    }

    protected void checkTextContains( int fieldId, String substring ) {
        onView( withId( fieldId ) ).check( matches( withText( containsString( substring ) ) ) );
    }

    protected void checkTextNotBlank( int fieldId ) {
        onView( withId( fieldId ) ).check( matches( not( withText( "" ) ) ) );
    }

    /**
     * Check that the background colour of a view is as expected
     * @param viewId view to colour-match
     * @param colour expected colour
     * @param isResource if true, interpret as the resource id of a colour. Otherwise, interpret as
     *                   a literal colour value.
     */
    protected void checkViewBackgroundColor( int viewId, int colour, boolean isResource ) {
        final int actualColour = isResource ? colour( colour ): colour;
        onView( withId( viewId ) ).check( matches( withBgColour( actualColour ) ) );
    }

    public Matcher<View> withBgColour( final int expectedColour ) {
        return new TypeSafeMatcher<View>() {
            private View item;

            @Override
            public void describeTo( Description description ) {
                description.appendText( "Background colour #" )
                        .appendText( Integer.toHexString( expectedColour ) );

                // Espresso does not invoke #describeMismatchSafely, so we call it manually here
                if ( item != null ) {
                    description.appendText( "\nActual: " );
                    describeMismatchSafely( item, description );
                }
            }

            @Override
            public void describeMismatchSafely( View item, Description description ) {
                Drawable bg = item.getBackground();

                if ( bg == null ) {
                    description.appendText( "No background" );
                }
                else if ( !( bg instanceof ColorDrawable) ) {
                    description.appendText( "A non-single-colour background" );
                }
                else {
                    description.appendText( "Background colour #" )
                            .appendText( Integer.toHexString( ((ColorDrawable) bg).getColor() ) );
                }
            }

            @Override
            public boolean matchesSafely( View item ) {
                this.item = item;
                Drawable bg = item.getBackground();

                if ( ( bg == null ) || !( bg instanceof ColorDrawable ) ) {
                    return false;
                }

                return ( ((ColorDrawable) bg).getColor() == expectedColour );
            }
        };
    }

    protected void selectDropdownItem( int dataSourceId, String value ) {

        onView( withId( dataSourceId ) ).perform( click() );
        onData( hasToString( startsWith( value ) ) ).perform( click() );
    }
}

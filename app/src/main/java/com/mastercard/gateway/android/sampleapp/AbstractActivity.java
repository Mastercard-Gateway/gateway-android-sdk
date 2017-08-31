package com.mastercard.gateway.android.sampleapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import butterknife.ButterKnife;

public abstract class AbstractActivity extends AppCompatActivity {
    protected ApiController apiController = ApiController.getInstance();

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( getContentView() );
        Log.i( getClass().getSimpleName(), "Displaying" );

        ButterKnife.bind( this );
    }

    protected abstract int getContentView();

    /**
     * Start the "result" activity to show the final result of a checkout flow
     *
     * @param textResource the main heading summarising the outcome
     * @param explanationResource a longer explanation of the outcome
     * @param bgColourResource the background colour to act as a visual cue; should usually be
     *                         either success_bg or failed_bg
     */
    protected void startResultActivity( int textResource, int explanationResource,
                                        int bgColourResource ) {

        String explanation = getResources().getString( explanationResource );
        startResultActivity( textResource, explanation, bgColourResource );
    }

    /**
     * Start the "result" activity to show the final result of a checkout flow
     *
     * @param textResource the main heading summarising the outcome
     * @param explanation a longer explanation of the outcome
     * @param bgColourResource the background colour to act as a visual cue; should usually be
     *                         either success_bg or failed_bg
     */
    protected void startResultActivity( int textResource, String explanation,
                                        int bgColourResource ) {

        Intent intent = new Intent( this, ResultActivity.class );
        intent.putExtra( "TEXT", getResources().getString( textResource ) );
        intent.putExtra( "EXPLANATION", explanation );
        intent.putExtra( "COLOUR", bgColourResource );
        startActivity( intent );
    }

    /**
     * Start the "result" activity to show the final result of a checkout flow, displaying a red
     * background to indicate failure
     *
     * @param textResource the main heading summarising the outcome
     * @param explanationResource a longer explanation of the outcome; 0 to omit
     */
    protected void startResultActivity( int textResource, int explanationResource ) {
        startResultActivity( textResource, explanationResource, R.color.failed_bg );
    }

    /**
     * Start the "result" activity to show the final result of a checkout flow, displaying a red
     * background to indicate failure
     *
     * @param textResource the main heading summarising the outcome
     * @param explanation a longer explanation of the outcome
     */
    protected void startResultActivity( int textResource, String explanation ) {
        startResultActivity( textResource, explanation, R.color.failed_bg );
    }

    protected int numAttempts( SharedPreferences prefs ) {
        try {
            return Math.max( 1,
                    Integer.parseInt( prefs.getString( "pref_key_num_update_attempts", "1" ) ) );
        }
        catch ( NumberFormatException e ) {
            return 1;
        }
    }

    protected int timeout( SharedPreferences prefs ) {
        try {
            return Math.max( 0,
                    Integer.parseInt( prefs.getString( "pref_key_timeout", "4000" ) ) );
        }
        catch ( NumberFormatException e ) {
            return 4000;
        }
    }
}

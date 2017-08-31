package com.mastercard.gateway.android.sampleapp;

import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import butterknife.Bind;
import butterknife.OnClick;

public class MainActivity extends AbstractActivity {
    @Bind(R.id.productChooser)
    Spinner productField;

    @Bind(R.id.buyButton)
    Button buyButton;

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.main, menu );
        return true;
    }

    @OnClick(R.id.buyButton)
    public void buyClicked( View v ) {
        buyButton.setEnabled( false );
//        new CreateSessionTask().execute();

        apiController.createSession(new CreateSessionCallback());
    }

    @Override
    public boolean onKeyUp( int keycode, KeyEvent e ) {
        switch ( keycode ) {
            case KeyEvent.KEYCODE_MENU:
                startSettingsActivity();
                return true;
        }

        return super.onKeyUp( keycode, e );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch ( item.getItemId() ) {
            case R.id.action_settings:
                startSettingsActivity();
                break;
            default:
                Log.e( MainActivity.class.getSimpleName(), "Unknown menu item pressed" );
                break;
        }

        return super.onOptionsItemSelected( item );
    }

    private void startSettingsActivity() {
        startActivity( new Intent( this, SettingsActivity.class ) );
    }

    class CreateSessionCallback implements ApiController.CreateSessionCallback {
        @Override
        public void onSuccess(String sessionId) {
            Log.i( "CreateSessionTask", "Session established" );

            String[] productIds = getResources().getStringArray( R.array.productIds );
            String productId = productIds[ productField.getSelectedItemPosition() ];

            Intent intent = new Intent(MainActivity.this, PaymentCaptureActivity.class);;
            intent.putExtra( "PRODUCT_ID", productId );
            intent.putExtra( "PRICE", getResources().getString(
                    R.string.main_activity_price ) );
            intent.putExtra( "CURRENCY", getResources().getString(
                    R.string.main_activity_currency ) );

            startActivity( intent );

            buyButton.setEnabled( true );
        }

        @Override
        public void onError(Throwable throwable) {
            startResultActivity(R.string.create_unrecognised_text, throwable.getMessage());

            buyButton.setEnabled( true );
        }
    }

//    protected class CreateSessionTask extends AsyncTask<String, Long, MerchantSimulatorResponse> {
//        private String productId;
//
//        protected CreateSessionTask() {
//            String[] productIds = getResources().getStringArray( R.array.productIds );
//            this.productId = productIds[ productField.getSelectedItemPosition() ];
//        }
//
//        protected MerchantSimulatorResponse doInBackground( String...params ) {
//            SharedPreferences prefs =
//                    PreferenceManager.getDefaultSharedPreferences( getBaseContext() );
//
//            String authuser = prefs.getString( "pref_key_username",
//                    getResources().getString( R.string.pref_default_username ) );
//
//            String password = prefs.getString( "pref_key_password",
//                    getResources().getString( R.string.pref_default_password ) );
//
//            String behaviour = prefs.getString( "pref_key_create_behaviour",
//                    getResources().getString( R.string.behaviour_succeed ) );
//
//            Log.d( MainActivity.class.getSimpleName(), "Behaviour: " + behaviour );
//            apiController.setAuthDetails( authuser, password );
//            return apiController.createSession( this.productId, behaviour, timeout( prefs ) );
//        }
//
//        protected void onPostExecute( MerchantSimulatorResponse response ) {
//
//            if ( response == null || response.status == null) {
//                startResultActivity( R.string.create_failed_text,
//                        R.string.create_failed_explanation );
//            }
//            else {
//                switch (response.status) {
//                    case "SUCCESS":
//                        Log.i( "CreateSessionTask", "Session established" );
//
//                        Intent intent = new Intent(MainActivity.this, PaymentCaptureActivity.class);;
//                        intent.putExtra( "PRODUCT_ID", this.productId );
//                        intent.putExtra( "PRICE", getResources().getString(
//                                R.string.main_activity_price ) );
//                        intent.putExtra( "CURRENCY", getResources().getString(
//                                R.string.main_activity_currency ) );
//
//                        startActivity( intent );
//                        break;
//                    case "DENIED":
//                        startResultActivity( R.string.create_denied_text,
//                                R.string.create_denied_explanation );
//                        break;
//                    case "ERROR":
//                        startResultActivity( R.string.create_error_text,
//                                R.string.create_error_explanation );
//                        break;
//                    case "TIMEOUT":
//                        startResultActivity( R.string.create_timeout_text,
//                                R.string.create_timeout_explanation );
//                        break;
//                    default:
//                        Log.e( "CreateSessionTask", "Unknown status " + response.status );
//                        startResultActivity( R.string.create_unrecognised_text,
//                                R.string.create_unrecognised_explanation );
//                }
//            }
//
//            buyButton.setEnabled( true );
//        }
//    }
}
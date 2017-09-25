package com.mastercard.gateway.android.sampleapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public abstract class AbstractActivity extends AppCompatActivity {
    protected ApiController apiController = ApiController.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(getClass().getSimpleName(), "Displaying");

        apiController.setMerchantServerUrl(BuildConfig.MERCHANT_SERVER_URL);
    }
}

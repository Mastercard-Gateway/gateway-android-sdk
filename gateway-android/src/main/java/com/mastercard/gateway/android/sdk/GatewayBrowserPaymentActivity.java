package com.mastercard.gateway.android.sdk;

import android.net.Uri;

import androidx.annotation.NonNull;

public class GatewayBrowserPaymentActivity extends BaseGatewayPaymentActivity {

    private static final String DEFAULT_TITLE = "Payment";

    @NonNull @Override protected String gatewayHost() { return "browserpayment"; }

    @NonNull @Override protected String getDefaultTitle() { return DEFAULT_TITLE; }

    @Override
    protected void onGatewayRedirect(@NonNull Uri uri) {
        // Expected form: gatewaysdk://paymentbrowser?orderResult=...
        String result = getQueryParam(uri, "orderResult");
        complete(EXTRA_GATEWAY_RESULT, result);
    }
}
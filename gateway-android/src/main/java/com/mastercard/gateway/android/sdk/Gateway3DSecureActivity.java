package com.mastercard.gateway.android.sdk;

import android.net.Uri;

import androidx.annotation.NonNull;

public class Gateway3DSecureActivity extends BaseGatewayPaymentActivity {

    private static final String DEFAULT_TITLE = "3D Secure";

    @NonNull @Override protected String gatewayHost() { return "3dsecure"; }

    @NonNull @Override protected String getDefaultTitle() { return DEFAULT_TITLE; }

    @Override
    protected void onGatewayRedirect(@NonNull Uri uri) {
        // Expected form: gatewaysdk://3dsecure?acsResult=...
        String result = getQueryParam(uri, "acsResult");
        complete(EXTRA_GATEWAY_RESULT, result);
    }
}

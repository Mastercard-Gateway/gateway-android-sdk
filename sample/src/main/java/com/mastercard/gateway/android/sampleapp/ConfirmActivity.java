/*
 * Copyright (c) 2016 Mastercard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mastercard.gateway.android.sampleapp;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mastercard.gateway.android.sampleapp.databinding.ActivityConfirmBinding;
import com.mastercard.gateway.android.sdk.Gateway;
import com.mastercard.gateway.android.sdk.Gateway3DSCallback;
import com.mastercard.gateway.android.sdk.Gateway3DSecureActivity;
import com.mastercard.gateway.android.sdk.SummaryStatus;

import java.util.UUID;

/**
 * Display a payment confirmation screen and send the final pay request
 */
public class ConfirmActivity extends AppCompatActivity implements Gateway3DSCallback {

    static final int REQUEST_3DS = 10000;

    ActivityConfirmBinding binding;
    ApiController apiController = ApiController.getInstance();
    String sessionId;

    String orderId;
    String transactionId;
    String amount;
    String currency;

    Gateway gateway;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gateway = new Gateway();

        apiController.setMerchantServerUrl(BuildConfig.MERCHANT_SERVER_URL);
        sessionId = getIntent().getStringExtra("SESSION_ID");

        amount = "1.00";
        currency = "USD";

        // random 3ds/order/txn IDs for example purposes

        orderId = UUID.randomUUID().toString();
        orderId = orderId.substring(0, orderId.indexOf('-'));
        transactionId = UUID.randomUUID().toString();
        transactionId = transactionId.substring(0, transactionId.indexOf('-'));

        binding = DataBindingUtil.setContentView(this, R.layout.activity_confirm);
        binding.confirmCardNo.setText(getIntent().getStringExtra("PAN_MASK"));
        binding.confirmBtn.setOnClickListener(view -> doCheck3DSEnrollment());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (gateway.handle3DSecureResult(requestCode, resultCode, data, this)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void on3DSecureCancel() {
        // reset the screen to try again
        binding.confirmBtn.setEnabled(true);
    }

    @Override
    public void on3DSecureError(SummaryStatus summaryStatus) {
        // if error, show error page
        startResultActivity(false);
    }

    @Override
    public void on3DSecureSuccess(String threeDSecureId, SummaryStatus summaryStatus) {
        doConfirm(threeDSecureId);
    }

    void doCheck3DSEnrollment() {
        binding.confirmBtn.setEnabled(false);

        // generate a random 3DSecureId for testing
        String threeDSId = UUID.randomUUID().toString();
        threeDSId = threeDSId.substring(0, threeDSId.indexOf('-'));

        apiController.check3DSecureEnrollment(sessionId, amount, currency, threeDSId, new Check3DSecureEnrollmentCallback());
    }

    void doConfirm() {
        doConfirm(null);
    }

    void doConfirm(String threeDSecureId) {
        apiController.completeSession(sessionId, orderId, transactionId, amount, currency, threeDSecureId, new CompleteSessionCallback());
    }

    void startResultActivity(boolean success) {
        binding.confirmBtn.setEnabled(true);

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("SUCCESS", success);
        startActivity(intent);
    }

    class Check3DSecureEnrollmentCallback implements ApiController.Check3DSecureEnrollmentCallback {
        @Override
        public void onSuccess(boolean cardEnrolled, String html) {
            if (cardEnrolled) {
                gateway.start3DSecureActivity(ConfirmActivity.this, "3DS", html);
            } else {
                doConfirm();
            }
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
            startResultActivity(false);
        }
    }

    class CompleteSessionCallback implements ApiController.CompleteSessionCallback {
        @Override
        public void onSuccess(String result) {
            startResultActivity(true);
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
            startResultActivity(false);
        }
    }
}

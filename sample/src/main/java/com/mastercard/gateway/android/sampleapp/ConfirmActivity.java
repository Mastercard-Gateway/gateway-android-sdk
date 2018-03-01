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

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mastercard.gateway.android.sampleapp.databinding.ActivityConfirmBinding;

import java.util.UUID;

/**
 * Display a payment confirmation screen and send the final pay request
 */
public class ConfirmActivity extends AppCompatActivity {

//    static String html = "<html><body><h1>Hello World</h1><a href=\"https://www.google.com\">Google!</a><p>hi</p><p>hi</p><p>hi</p><p>hi</p><p>hi</p><p>hi</p><p>hi</p><p>hi</p><p>hi</p><p>hi</p><p>hi</p><p>hi</p><p>hi</p><p>hi</p><p>hi</p><p>hi</p><p>hi</p></body></html>";
    static int REQUEST_3DS = 10000;

    ActivityConfirmBinding binding;
    ApiController apiController = ApiController.getInstance();
    String sessionId;

    String threeDSId;
    String orderId;
    String transactionId;
    String amount;
    String currency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiController.setMerchantServerUrl(BuildConfig.MERCHANT_SERVER_URL);
        sessionId = getIntent().getStringExtra("SESSION_ID");

        amount = "1.00";
        currency = "USD";

        // random 3ds/order/txn IDs for example purposes
        threeDSId = UUID.randomUUID().toString();
        threeDSId = threeDSId.substring(0, threeDSId.indexOf('-'));
        orderId = UUID.randomUUID().toString();
        orderId = orderId.substring(0, orderId.indexOf('-'));
        transactionId = UUID.randomUUID().toString();
        transactionId = transactionId.substring(0, transactionId.indexOf('-'));

        binding = DataBindingUtil.setContentView(this, R.layout.activity_confirm);
        binding.confirmCardNo.setText(getIntent().getStringExtra("PAN_MASK"));
        binding.confirmBtn.setOnClickListener(view -> doCheck3DSEnrollment());
    }

    void doCheck3DSEnrollment() {
        binding.confirmBtn.setEnabled(false);

        apiController.check3DSecureEnrollment(sessionId, threeDSId, amount, currency, new Check3DSecureEnrollmentCallback());
    }

    void doConfirm() {
        apiController.completeSession(sessionId, orderId, transactionId, amount, currency, new CompleteSessionCallback());
    }

    void start3DSecureActivity(String html) {
        Intent intent = new Intent(this, ThreeDSecureActivity.class);
        intent.putExtra(ThreeDSecureActivity.EXTRA_HTML, html);
        startActivityForResult(intent, REQUEST_3DS);
    }

    void startResultActivity(boolean success) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("SUCCESS", success);
        startActivity(intent);
    }

    class Check3DSecureEnrollmentCallback implements ApiController.Check3DSecureEnrollmentCallback {
        @Override
        public void onSuccess(boolean cardEnrolled, String html) {
            if (cardEnrolled) {
                start3DSecureActivity(html);
            } else {
                doConfirm();
            }
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
            startResultActivity(false);
            binding.confirmBtn.setEnabled(true);
        }
    }

    class CompleteSessionCallback implements ApiController.CompleteSessionCallback {
        @Override
        public void onSuccess(String result) {
            startResultActivity(true);
            binding.confirmBtn.setEnabled(true);
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
            startResultActivity(false);
            binding.confirmBtn.setEnabled(true);
        }
    }
}

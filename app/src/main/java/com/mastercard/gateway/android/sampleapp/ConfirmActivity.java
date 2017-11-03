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

    ActivityConfirmBinding binding;
    ApiController apiController = ApiController.getInstance();
    String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiController.setMerchantServerUrl(BuildConfig.MERCHANT_SERVER_URL);
        sessionId = getIntent().getStringExtra("SESSION_ID");

        binding = DataBindingUtil.setContentView(this, R.layout.activity_confirm);
        binding.confirmCardNo.setText(getIntent().getStringExtra("PAN_MASK"));
        binding.confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doConfirm();
            }
        });
    }

    void doConfirm() {
        binding.confirmBtn.setEnabled(false);

        // random order/txn IDs for example purposes
        String orderId = UUID.randomUUID().toString();
        orderId = orderId.substring(0, orderId.indexOf('-'));
        String transactionId = UUID.randomUUID().toString();
        transactionId = transactionId.substring(0, transactionId.indexOf('-'));

        apiController.completeSession(sessionId, orderId, transactionId, "250.00", "USD", new CompleteSessionCallback());
    }

    void startResultActivity(boolean success) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("SUCCESS", success);
        startActivity(intent);
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

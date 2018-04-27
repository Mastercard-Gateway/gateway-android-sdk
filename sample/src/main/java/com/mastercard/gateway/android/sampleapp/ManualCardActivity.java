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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mastercard.gateway.android.sampleapp.databinding.ActivityManualCardBinding;
import com.mastercard.gateway.android.sdk.Gateway;

public class ManualCardActivity extends AppCompatActivity {

    ActivityManualCardBinding binding;
    ApiController apiController = ApiController.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiController.setMerchantServerUrl(BuildConfig.MERCHANT_SERVER_URL);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_manual_card);
        binding.buyButton.setOnClickListener(this::buyClicked);

        Gateway gateway = new Gateway();
        gateway.setMerchantId(BuildConfig.GATEWAY_MERCHANT_ID);

        try {
            Gateway.Region region = Gateway.Region.valueOf(BuildConfig.GATEWAY_REGION);
            gateway.setRegion(region);
        } catch (Exception e) {
            Log.e(PayActivity.class.getSimpleName(), "Invalid Gateway region value provided", e);
        }
    }

    void buyClicked(View v) {
        binding.buyButton.setEnabled(false);

        apiController.createSession(new CreateSessionCallback());
    }

    class CreateSessionCallback implements ApiController.CreateSessionCallback {
        @Override
        public void onSuccess(String sessionId, String apiVersion) {
            Log.i("CreateSessionTask", "Session established");

            Intent intent = new Intent(ManualCardActivity.this, PayActivity.class);
            intent.putExtra("SESSION_ID", sessionId);
            intent.putExtra("API_VERSION", apiVersion);

            startActivity(intent);

            binding.buyButton.setEnabled(true);
        }

        @Override
        public void onError(Throwable throwable) {
            Toast.makeText(ManualCardActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();

            binding.buyButton.setEnabled(true);
        }
    }
}
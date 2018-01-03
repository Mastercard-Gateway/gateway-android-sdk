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
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mastercard.gateway.android.sampleapp.databinding.ActivityPayBinding;
import com.mastercard.gateway.android.sdk.Gateway;
import com.mastercard.gateway.android.sdk.GatewayCallback;
import com.mastercard.gateway.android.sdk.GatewayMap;

import java.util.Arrays;

import static android.text.TextUtils.isEmpty;

public class PayActivity extends AppCompatActivity {

    ActivityPayBinding binding;

    SharedPreferences prefs = null;
    String nameOnCard, cardNumber, expiryMM, expiryYY, cvv, sessionId;
    Gateway gateway;
    TextChangeListener textChangeListener = new TextChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_pay);
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        sessionId = getIntent().getStringExtra("SESSION_ID");

        // ========================================================
        // Configure the Gateway object
        // ========================================================

        gateway = new Gateway();
        gateway.setMerchantId(BuildConfig.GATEWAY_MERCHANT_ID);

        try {
            Gateway.Region region = Gateway.Region.valueOf(BuildConfig.GATEWAY_REGION);
            gateway.setRegion(region);
        } catch (Exception e) {
            Log.e(PayActivity.class.getSimpleName(), "Invalid Gateway region value provided", e);
        }

        // ========================================================

        binding.nameOnCard.requestFocus();
        binding.nameOnCard.addTextChangedListener(textChangeListener);
        binding.cardnumber.addTextChangedListener(textChangeListener);
        binding.expiryMonth.addTextChangedListener(textChangeListener);
        binding.expiryYear.addTextChangedListener(textChangeListener);
        binding.cvv.addTextChangedListener(textChangeListener);

        binding.submitButton.setEnabled(false);
        binding.submitButton.setOnClickListener(this::buyClicked);
    }

    void buyClicked(View submitButton) {
        nameOnCard = binding.nameOnCard.getText().toString();
        cardNumber = binding.cardnumber.getText().toString();
        expiryMM = binding.expiryMonth.getText().toString();
        expiryYY = binding.expiryYear.getText().toString();
        cvv = binding.cvv.getText().toString();

        Log.i(getClass().getSimpleName(), "Making purchase");

        submitButton.setEnabled(false);

        GatewayMap request = new GatewayMap();
        request.put("sourceOfFunds.provided.card.nameOnCard", nameOnCard);
        request.put("sourceOfFunds.provided.card.number", cardNumber);
        request.put("sourceOfFunds.provided.card.securityCode", cvv);
        request.put("sourceOfFunds.provided.card.expiry.month", expiryMM);
        request.put("sourceOfFunds.provided.card.expiry.year", expiryYY);

        gateway.updateSession(sessionId, request, new UpdateSessionCallback());
    }

    String maskedCardNumber() {
        int maskLen = cardNumber.length() - 4;
        char[] mask = new char[maskLen];
        Arrays.fill(mask, '*');
        return new String(mask) + cardNumber.substring(maskLen);
    }

    void enableSubmitButton() {
        if (isEmpty(binding.nameOnCard.getText()) || isEmpty(binding.cardnumber.getText())
                || isEmpty(binding.expiryMonth.getText()) || isEmpty(binding.expiryYear.getText())
                || isEmpty(binding.cvv.getText()) || (binding.cvv.getText().toString().length() < 3)) {

            binding.submitButton.setEnabled(false);
        } else {
            binding.submitButton.setEnabled(true);
        }
    }


    class UpdateSessionCallback implements GatewayCallback {

        @Override
        public void onSuccess(GatewayMap response) {
            Log.i(PayActivity.class.getSimpleName(), "Successful pay");

            Intent intent = new Intent(PayActivity.this, ConfirmActivity.class);
            intent.putExtra("PAN_MASK", maskedCardNumber());
            intent.putExtra("SESSION_ID", sessionId);
            startActivity(intent);
        }

        @Override
        public void onError(Throwable throwable) {
            Log.e(PayActivity.class.getSimpleName(), throwable.getMessage(), throwable);

            Toast.makeText(PayActivity.this, R.string.pay_error_could_not_update_session, Toast.LENGTH_SHORT).show();
        }
    }

    class TextChangeListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            enableSubmitButton();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }
}

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

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.identity.intents.model.UserAddress;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.mastercard.gateway.android.sampleapp.databinding.ActivityPayBinding;
import com.mastercard.gateway.android.sdk.Gateway;
import com.mastercard.gateway.android.sdk.GatewayCallback;
import com.mastercard.gateway.android.sdk.GatewayGooglePayCallback;
import com.mastercard.gateway.android.sdk.GatewayMap;

import java.util.Arrays;

import static android.text.TextUtils.isEmpty;

public class PayActivity extends AppCompatActivity {

    ActivityPayBinding binding;

    SharedPreferences prefs = null;
    String nameOnCard, cardNumber, expiryMM, expiryYY, cvv, sessionId, apiVersion;
    Gateway gateway;
    TextChangeListener textChangeListener = new TextChangeListener();
    PaymentsClient mPaymentsClient;
    GooglePayCallback googlePayCallback = new GooglePayCallback();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_pay);
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        sessionId = getIntent().getStringExtra("SESSION_ID");
        apiVersion = getIntent().getStringExtra("API_VERSION");

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

        // init Google Pay client
        mPaymentsClient = Wallet.getPaymentsClient(this, new Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .build());

        // init google pay button
        binding.googlePayButton.setOnClickListener(view -> {
            PaymentDataRequest request = createPaymentDataRequest();
            if (request != null) {
                // use the Gateway convenience handler for launching the Google Pay flow
                Gateway.requestGooglePayData(mPaymentsClient, request, PayActivity.this);
            }
        });

        // check if Google Pay is available
        isReadyToPay();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle the Google Pay lifecycle
        if (Gateway.handleGooglePayResult(requestCode, resultCode, data, googlePayCallback)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void isReadyToPay() {
        IsReadyToPayRequest request = IsReadyToPayRequest.newBuilder()
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                .build();

        Task<Boolean> task = mPaymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(task1 -> {
            try {
                boolean result = task1.getResult(ApiException.class);
                if (result) {
                    // Show Google as payment option.
                    binding.googlePayButton.setVisibility(View.VISIBLE);
                } else {
                    // Hide Google as payment option.
                    binding.googlePayButton.setVisibility(View.GONE);
                }
            } catch (ApiException exception) {
            }
        });
    }

    void buyClicked(View submitButton) {
        nameOnCard = binding.nameOnCard.getText().toString();
        cardNumber = binding.cardnumber.getText().toString();
        expiryMM = binding.expiryMonth.getText().toString();
        expiryYY = binding.expiryYear.getText().toString();
        cvv = binding.cvv.getText().toString();

        Log.i(getClass().getSimpleName(), "Making purchase");

        submitButton.setEnabled(false);

        // build the gateway request
        GatewayMap request = new GatewayMap()
                .set("sourceOfFunds.provided.card.nameOnCard", nameOnCard)
                .set("sourceOfFunds.provided.card.number", cardNumber)
                .set("sourceOfFunds.provided.card.securityCode", cvv)
                .set("sourceOfFunds.provided.card.expiry.month", expiryMM)
                .set("sourceOfFunds.provided.card.expiry.year", expiryYY);

        gateway.updateSession(sessionId, apiVersion, request, new UpdateSessionCallback());
    }

    GatewayMap buildUpdateSessionRequest(PaymentData googlePayData) {
        // build the gateway request
        return new GatewayMap()
                .set("sourceOfFunds.provided.card.devicePayment.paymentToken", googlePayData.getPaymentMethodToken().getToken());
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

    PaymentDataRequest createPaymentDataRequest() {
        PaymentDataRequest.Builder request = PaymentDataRequest.newBuilder()
                .setTransactionInfo(TransactionInfo.newBuilder()
                        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                        .setTotalPrice("1.00")
                        .setCurrencyCode("USD")
                        .build())
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                .setCardRequirements(CardRequirements.newBuilder()
                        .addAllowedCardNetworks(Arrays.asList(
                                WalletConstants.CARD_NETWORK_AMEX,
                                WalletConstants.CARD_NETWORK_DISCOVER,
                                WalletConstants.CARD_NETWORK_VISA,
                                WalletConstants.CARD_NETWORK_MASTERCARD))
                        .build());

        PaymentMethodTokenizationParameters params = PaymentMethodTokenizationParameters.newBuilder()
                .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                .addParameter("gateway", "mpgs")
                .addParameter("gatewayMerchantId", BuildConfig.GATEWAY_MERCHANT_ID)
                .build();

        request.setPaymentMethodTokenizationParameters(params);
        return request.build();
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

    class GooglePayCallback implements GatewayGooglePayCallback {
        @Override
        public void onReceivedPaymentData(PaymentData paymentData) {
            Log.d(GooglePayCallback.class.getSimpleName(), "ReceivedPaymentData");

            GatewayMap request = buildUpdateSessionRequest(paymentData);

            gateway.updateSession(sessionId, apiVersion, request, new UpdateSessionCallback());
        }

        @Override
        public void onGooglePayCancelled() {
            Log.d(GooglePayCallback.class.getSimpleName(), "Cancelled");


        }

        @Override
        public void onGooglePayError(Status status) {
            Log.d(GooglePayCallback.class.getSimpleName(), "Error");
        }
    }
}

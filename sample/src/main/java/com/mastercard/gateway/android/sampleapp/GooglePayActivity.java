package com.mastercard.gateway.android.sampleapp;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.mastercard.gateway.android.sampleapp.databinding.ActivityGooglePayBinding;
import com.mastercard.gateway.android.sdk.Gateway;
import com.mastercard.gateway.android.sdk.GatewayCallback;
import com.mastercard.gateway.android.sdk.GatewayGooglePayCallback;
import com.mastercard.gateway.android.sdk.GatewayMap;

import java.util.Arrays;

public class GooglePayActivity extends AppCompatActivity {

    ActivityGooglePayBinding binding;
    Gateway gateway;
    String sessionId, apiVersion;
    PaymentsClient paymentsClient;
    PaymentData paymentData;
    ApiController apiController = ApiController.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_google_pay);

        // init google pay button
        binding.googlePayButton.setOnClickListener(v -> requestPaymentData());

        // ========================================================
        // Configure the Gateway object
        // ========================================================

        gateway = new Gateway();
        gateway.setMerchantId(Config.MERCHANT_ID.getValue(this));

        try {
            Gateway.Region region = Gateway.Region.valueOf(Config.REGION.getValue(this));
            gateway.setRegion(region);
        } catch (Exception e) {
            Log.e(PayActivity.class.getSimpleName(), "Invalid Gateway region value provided", e);
        }

        // init Google Pay client
        paymentsClient = Wallet.getPaymentsClient(this, new Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .build());

        // check if Google Pay is available
        isReadyToPay();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle the Google Pay lifecycle
        if (Gateway.handleGooglePayResult(requestCode, resultCode, data, new GooglePayCallback())) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void isReadyToPay() {
        IsReadyToPayRequest request = IsReadyToPayRequest.newBuilder()
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                .build();

        Task<Boolean> task = paymentsClient.isReadyToPay(request);
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
                .addParameter("gatewayMerchantId", Config.MERCHANT_ID.getValue(this))
                .build();

        request.setPaymentMethodTokenizationParameters(params);
        return request.build();
    }

    void requestPaymentData() {
        PaymentDataRequest request = createPaymentDataRequest();

        // use the Gateway convenience handler for launching the Google Pay flow
        Gateway.requestGooglePayData(paymentsClient, request, GooglePayActivity.this);

        binding.googlePayButton.setEnabled(false);
    }

    void createSession() {
        // create a session via merchant service
        apiController.createSession(new CreateSessionCallback());
    }

    void updateSessionWithPayerData() {
        GatewayMap request = new GatewayMap()
                .set("sourceOfFunds.provided.card.devicePayment.paymentToken", paymentData.getPaymentMethodToken().getToken());

        gateway.updateSession(sessionId, apiVersion, request, new UpdateSessionCallback());
    }

    void goToConfirmScreen() {
        Intent intent = new Intent(GooglePayActivity.this, ConfirmActivity.class);
        intent.putExtra("PAN_MASK", paymentData.getCardInfo().getCardDescription());
        intent.putExtra("SESSION_ID", sessionId);
        startActivity(intent);
    }


    class GooglePayCallback implements GatewayGooglePayCallback {
        @Override
        public void onReceivedPaymentData(PaymentData data) {
            Log.d(GooglePayCallback.class.getSimpleName(), "ReceivedPaymentData");

            paymentData = data;

            createSession();
        }

        @Override
        public void onGooglePayCancelled() {
            Log.d(GooglePayCallback.class.getSimpleName(), "Cancelled");

            binding.googlePayButton.setEnabled(true);
        }

        @Override
        public void onGooglePayError(Status status) {
            Log.d(GooglePayCallback.class.getSimpleName(), "Error");

            binding.googlePayButton.setEnabled(true);
        }
    }

    class CreateSessionCallback implements ApiController.CreateSessionCallback {

        @Override
        public void onSuccess(String session, String api) {
            Log.i("CreateSessionTask", "Session established");

            sessionId = session;
            apiVersion = api;

            updateSessionWithPayerData();
        }

        @Override
        public void onError(Throwable throwable) {
            Log.d(GooglePayCallback.class.getSimpleName(), "Error creating session");
            Toast.makeText(GooglePayActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();

            binding.googlePayButton.setEnabled(true);
        }
    }

    class UpdateSessionCallback implements GatewayCallback {

        @Override
        public void onSuccess(GatewayMap response) {
            Log.i(PayActivity.class.getSimpleName(), "Successful session update");

            goToConfirmScreen();

            binding.googlePayButton.setEnabled(true);
        }

        @Override
        public void onError(Throwable throwable) {
            Log.e(PayActivity.class.getSimpleName(), throwable.getMessage(), throwable);
            Toast.makeText(GooglePayActivity.this, R.string.pay_error_could_not_update_session, Toast.LENGTH_SHORT).show();

            binding.googlePayButton.setEnabled(true);
        }
    }
}

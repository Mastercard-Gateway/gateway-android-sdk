package com.mastercard.gateway.android.sampleapp;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mastercard.gateway.android.sampleapp.databinding.ActivityProcessPaymentBinding;
import com.mastercard.gateway.android.sdk.Gateway;
import com.mastercard.gateway.android.sdk.Gateway3DSecureCallback;
import com.mastercard.gateway.android.sdk.GatewayCallback;
import com.mastercard.gateway.android.sdk.GatewayMap;

import java.util.UUID;

public class ProcessPaymentActivity extends AppCompatActivity {

    static final int REQUEST_CARD_INFO = 100;

    // static for demo
    static final String AMOUNT = "1.00";
    static final String CURRENCY = "USD";

    ActivityProcessPaymentBinding binding;
    Gateway gateway;
    String sessionId, apiVersion, threeDSecureId, orderId, transactionId;
    boolean isGooglePay = false;
    ApiController apiController = ApiController.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_process_payment);

        // init api controller
        apiController.setMerchantServerUrl(Config.MERCHANT_URL.getValue(this));

        // init gateway
        gateway = new Gateway();
        gateway.setMerchantId(Config.MERCHANT_ID.getValue(this));
        try {
            Gateway.Region region = Gateway.Region.valueOf(Config.REGION.getValue(this));
            gateway.setRegion(region);
        } catch (Exception e) {
            Log.e(PayActivity.class.getSimpleName(), "Invalid Gateway region value provided", e);
        }

        // random order/txn IDs for example purposes
        orderId = UUID.randomUUID().toString();
        orderId = orderId.substring(0, orderId.indexOf('-'));
        transactionId = UUID.randomUUID().toString();
        transactionId = transactionId.substring(0, transactionId.indexOf('-'));

        // bind buttons
        binding.startButton.setOnClickListener(v -> createSession());
        binding.confirmButton.setOnClickListener(v -> check3dsEnrollment());
        binding.finishButton.setOnClickListener(v -> finish());

        reset();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle the 3DSecure lifecycle
        if (Gateway.handle3DSecureResult(requestCode, resultCode, data, new ThreeDSecureCallback())) {
            return;
        }

        if (requestCode == REQUEST_CARD_INFO) {
            binding.collectCardInfoProgress.setVisibility(View.GONE);

            if (resultCode == Activity.RESULT_OK) {
                binding.collectCardInfoSuccess.setVisibility(View.VISIBLE);

                String googlePayToken = data.getStringExtra(CollectCardInfoActivity.EXTRA_PAYMENT_TOKEN);
                String cardDescription = data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_DESCRIPTION);

                if (googlePayToken != null) {
                    isGooglePay = true;

                    String paymentToken = data.getStringExtra(CollectCardInfoActivity.EXTRA_PAYMENT_TOKEN);

                    updateSession(paymentToken);
                } else {
                    isGooglePay = false;

                    String cardName = data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_NAME);
                    String cardNumber = data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_NUMBER);
                    String cardExpiryMonth = data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_EXPIRY_MONTH);
                    String cardExpiryYear = data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_EXPIRY_YEAR);
                    String cardCvv = data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_CVV);

                    updateSession(cardName, cardNumber, cardExpiryMonth, cardExpiryYear, cardCvv);
                }

            } else {
                binding.collectCardInfoError.setVisibility(View.VISIBLE);

                resetButtons();
            }

            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    void reset() {
        binding.createSessionProgress.setVisibility(View.GONE);
        binding.createSessionSuccess.setVisibility(View.GONE);
        binding.createSessionError.setVisibility(View.GONE);

        binding.collectCardInfoProgress.setVisibility(View.GONE);
        binding.collectCardInfoSuccess.setVisibility(View.GONE);
        binding.collectCardInfoError.setVisibility(View.GONE);

        binding.updateSessionProgress.setVisibility(View.GONE);
        binding.updateSessionSuccess.setVisibility(View.GONE);
        binding.updateSessionError.setVisibility(View.GONE);

        binding.check3dsProgress.setVisibility(View.GONE);
        binding.check3dsSuccess.setVisibility(View.GONE);
        binding.check3dsError.setVisibility(View.GONE);

        binding.processPaymentProgress.setVisibility(View.GONE);
        binding.processPaymentSuccess.setVisibility(View.GONE);
        binding.processPaymentError.setVisibility(View.GONE);

        resetButtons();
    }

    void resetButtons() {
        binding.startButton.setEnabled(true);

        binding.startButton.setVisibility(View.VISIBLE);
        binding.confirmButton.setVisibility(View.GONE);
        binding.finishButton.setVisibility(View.GONE);
    }

    void createSession() {
        reset();

        binding.startButton.setEnabled(false);
        binding.createSessionProgress.setVisibility(View.VISIBLE);

        apiController.createSession(new CreateSessionCallback());
    }

    void collectCardInfo() {
        binding.collectCardInfoProgress.setVisibility(View.VISIBLE);

        Intent i = new Intent(this, CollectCardInfoActivity.class);
        i.putExtra(CollectCardInfoActivity.EXTRA_GOOGLE_PAY_TXN_AMOUNT, AMOUNT);
        i.putExtra(CollectCardInfoActivity.EXTRA_GOOGLE_PAY_TXN_CURRENCY, CURRENCY);

        startActivityForResult(i, REQUEST_CARD_INFO);
    }

    void updateSession(String paymentToken) {
        binding.updateSessionProgress.setVisibility(View.VISIBLE);

        GatewayMap request = new GatewayMap()
                .set("sourceOfFunds.provided.card.devicePayment.paymentToken", paymentToken);

        gateway.updateSession(sessionId, apiVersion, request, new UpdateSessionCallback());
    }

    void updateSession(String name, String number, String expiryMonth, String expiryYear, String cvv) {
        binding.updateSessionProgress.setVisibility(View.VISIBLE);

        // build the gateway request
        GatewayMap request = new GatewayMap()
                .set("sourceOfFunds.provided.card.nameOnCard", name)
                .set("sourceOfFunds.provided.card.number", number)
                .set("sourceOfFunds.provided.card.securityCode", cvv)
                .set("sourceOfFunds.provided.card.expiry.month", expiryMonth)
                .set("sourceOfFunds.provided.card.expiry.year", expiryYear);

        gateway.updateSession(sessionId, apiVersion, request, new UpdateSessionCallback());
    }

    void check3dsEnrollment() {
        binding.check3dsProgress.setVisibility(View.VISIBLE);
        binding.confirmButton.setEnabled(false);

        // generate a random 3DSecureId for testing
        String threeDSId = UUID.randomUUID().toString();
        threeDSId = threeDSId.substring(0, threeDSId.indexOf('-'));

        apiController.check3DSecureEnrollment(sessionId, AMOUNT, CURRENCY, threeDSId, new Check3DSecureEnrollmentCallback());
    }

    void processPayment() {
        binding.processPaymentProgress.setVisibility(View.VISIBLE);

        apiController.completeSession(sessionId, orderId, transactionId, AMOUNT, CURRENCY, threeDSecureId, isGooglePay, new CompleteSessionCallback());
    }


    class CreateSessionCallback implements ApiController.CreateSessionCallback {
        @Override
        public void onSuccess(String sessionId, String apiVersion) {
            Log.i("CreateSessionTask", "Session established");
            binding.createSessionProgress.setVisibility(View.GONE);
            binding.createSessionSuccess.setVisibility(View.VISIBLE);

            ProcessPaymentActivity.this.sessionId = sessionId;
            ProcessPaymentActivity.this.apiVersion = apiVersion;

            collectCardInfo();
        }

        @Override
        public void onError(Throwable throwable) {
            Toast.makeText(ProcessPaymentActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
            binding.createSessionProgress.setVisibility(View.GONE);
            binding.createSessionError.setVisibility(View.VISIBLE);

            resetButtons();
        }
    }

    class UpdateSessionCallback implements GatewayCallback {

        @Override
        public void onSuccess(GatewayMap response) {
            Log.i(PayActivity.class.getSimpleName(), "Successful pay");
            binding.updateSessionProgress.setVisibility(View.GONE);
            binding.updateSessionSuccess.setVisibility(View.VISIBLE);

            binding.startButton.setVisibility(View.GONE);
            binding.confirmButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void onError(Throwable throwable) {
            Log.e(PayActivity.class.getSimpleName(), throwable.getMessage(), throwable);
            binding.updateSessionProgress.setVisibility(View.GONE);
            binding.updateSessionError.setVisibility(View.VISIBLE);

            resetButtons();
        }
    }

    class Check3DSecureEnrollmentCallback implements ApiController.Check3DSecureEnrollmentCallback {
        @Override
        public void onSuccess(String summaryStatus, String threeDSecureId, String html) {
            if ("CARD_ENROLLED".equalsIgnoreCase(summaryStatus)) {
                Gateway.start3DSecureActivity(ProcessPaymentActivity.this, html);
                return;
            }

            binding.check3dsProgress.setVisibility(View.GONE);
            binding.check3dsSuccess.setVisibility(View.VISIBLE);
            ProcessPaymentActivity.this.threeDSecureId = null;

            // for these 2 cases, you still provide the 3DSecureId with the pay operation
            if ("CARD_NOT_ENROLLED".equalsIgnoreCase(summaryStatus) || "AUTHENTICATION_NOT_AVAILABLE".equalsIgnoreCase(summaryStatus)) {
                ProcessPaymentActivity.this.threeDSecureId = threeDSecureId;
            }

            processPayment();
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
            binding.check3dsProgress.setVisibility(View.GONE);
            binding.check3dsError.setVisibility(View.VISIBLE);

            resetButtons();
        }
    }

    class ThreeDSecureCallback implements Gateway3DSecureCallback {
        @Override
        public void on3DSecureCancel() {
            binding.check3dsProgress.setVisibility(View.GONE);
            binding.check3dsError.setVisibility(View.VISIBLE);

            resetButtons();
        }

        @Override
        public void on3DSecureError(String errorMessage) {
            binding.check3dsProgress.setVisibility(View.GONE);
            binding.check3dsError.setVisibility(View.VISIBLE);

            resetButtons();
        }

        @Override
        public void on3DSecureComplete(String summaryStatus, String threeDSecureId) {
            binding.check3dsProgress.setVisibility(View.GONE);
            binding.check3dsSuccess.setVisibility(View.VISIBLE);

            ProcessPaymentActivity.this.threeDSecureId = threeDSecureId;

            processPayment();
        }
    }

    class CompleteSessionCallback implements ApiController.CompleteSessionCallback {
        @Override
        public void onSuccess(String result) {
            binding.processPaymentProgress.setVisibility(View.GONE);
            binding.processPaymentSuccess.setVisibility(View.VISIBLE);

            binding.confirmButton.setVisibility(View.GONE);
            binding.finishButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
            binding.processPaymentProgress.setVisibility(View.GONE);
            binding.processPaymentError.setVisibility(View.VISIBLE);

            resetButtons();
        }
    }
}

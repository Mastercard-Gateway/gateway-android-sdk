package com.mastercard.gateway.android.sampleapp;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.mastercard.gateway.android.sampleapp.databinding.ActivityCollectCardInfoBinding;
import com.mastercard.gateway.android.sdk.Gateway;
import com.mastercard.gateway.android.sdk.GatewayGooglePayCallback;

import java.util.Arrays;

import static android.text.TextUtils.isEmpty;

public class CollectCardInfoActivity extends AppCompatActivity {

    private static final String EXTRA_PREFIX = "com.mastercard.gateway.sample.EXTRA_";

    // request
    public static final String EXTRA_GOOGLE_PAY_TXN_AMOUNT = EXTRA_PREFIX + "GOOGLE_PAY_TXN_AMOUNT";
    public static final String EXTRA_GOOGLE_PAY_TXN_CURRENCY = EXTRA_PREFIX + "GOOGLE_PAY_TXN_CURRENCY";

    // response
    public static final String EXTRA_CARD_DESCRIPTION = EXTRA_PREFIX + "CARD_DESCRIPTION";
    public static final String EXTRA_CARD_NAME = EXTRA_PREFIX + "CARD_NAME";
    public static final String EXTRA_CARD_NUMBER = EXTRA_PREFIX + "CARD_NUMBER";
    public static final String EXTRA_CARD_EXPIRY_MONTH = EXTRA_PREFIX + "CARD_EXPIRY_MONTH";
    public static final String EXTRA_CARD_EXPIRY_YEAR = EXTRA_PREFIX + "CARD_EXPIRY_YEAR";
    public static final String EXTRA_CARD_CVV = EXTRA_PREFIX + "CARD_CVC";
    public static final String EXTRA_PAYMENT_TOKEN = EXTRA_PREFIX + "PAYMENT_TOKEN";


    ActivityCollectCardInfoBinding binding;
    String googlePayTxnAmount;
    String googlePayTxnCurrency;
    PaymentsClient paymentsClient;
    TextChangeListener textChangeListener = new TextChangeListener();
    GooglePayCallback googlePayCallback = new GooglePayCallback();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_collect_card_info);

        // get bundle extras and set txn amount and currency for google pay
        Intent i = getIntent();
        googlePayTxnAmount = i.getStringExtra(EXTRA_GOOGLE_PAY_TXN_AMOUNT);
        googlePayTxnCurrency = i.getStringExtra(EXTRA_GOOGLE_PAY_TXN_CURRENCY);

        // init manual text field listeners
        binding.nameOnCard.requestFocus();
        binding.nameOnCard.addTextChangedListener(textChangeListener);
        binding.cardnumber.addTextChangedListener(textChangeListener);
        binding.expiryMonth.addTextChangedListener(textChangeListener);
        binding.expiryYear.addTextChangedListener(textChangeListener);
        binding.cvv.addTextChangedListener(textChangeListener);

        binding.submitButton.setEnabled(false);
        binding.submitButton.setOnClickListener(v -> continueButtonClicked());


        // init Google Pay client
        paymentsClient = Wallet.getPaymentsClient(this, new Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .build());

        // init google pay button
        binding.googlePayButton.setOnClickListener(v -> googlePayButtonClicked());

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

    void enableContinueButton() {
        if (isEmpty(binding.nameOnCard.getText()) || isEmpty(binding.cardnumber.getText())
                || isEmpty(binding.expiryMonth.getText()) || isEmpty(binding.expiryYear.getText())
                || isEmpty(binding.cvv.getText()) || (binding.cvv.getText().toString().length() < 3)) {

            binding.submitButton.setEnabled(false);
        } else {
            binding.submitButton.setEnabled(true);
        }
    }

    void continueButtonClicked() {
        String nameOnCard = binding.nameOnCard.getText().toString();
        String cardNumber = binding.cardnumber.getText().toString();
        String expiryMM = binding.expiryMonth.getText().toString();
        String expiryYY = binding.expiryYear.getText().toString();
        String cvv = binding.cvv.getText().toString();

        Intent i = new Intent();
        i.putExtra(EXTRA_CARD_DESCRIPTION, maskCardNumber(cardNumber));
        i.putExtra(EXTRA_CARD_NAME, nameOnCard);
        i.putExtra(EXTRA_CARD_NUMBER, cardNumber);
        i.putExtra(EXTRA_CARD_EXPIRY_MONTH, expiryMM);
        i.putExtra(EXTRA_CARD_EXPIRY_YEAR, expiryYY);
        i.putExtra(EXTRA_CARD_CVV, cvv);

        setResult(Activity.RESULT_OK, i);
        finish();
    }

    void googlePayButtonClicked() {
        PaymentDataRequest request = createPaymentDataRequest();

        // use the Gateway convenience handler for launching the Google Pay flow
        Gateway.requestGooglePayData(paymentsClient, request, CollectCardInfoActivity.this);
    }

    void returnCardInfo(PaymentData paymentData) {
        Intent i = new Intent();
        i.putExtra(EXTRA_CARD_DESCRIPTION, paymentData.getCardInfo().getCardDescription());
        i.putExtra(EXTRA_PAYMENT_TOKEN, paymentData.getPaymentMethodToken().getToken());

        setResult(Activity.RESULT_OK, i);
        finish();
    }

    String maskCardNumber(String number) {
        int maskLen = number.length() - 4;
        char[] mask = new char[maskLen];
        Arrays.fill(mask, '*');
        return new String(mask) + number.substring(maskLen);
    }

    void isReadyToPay() {
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
                    binding.orSeparator.setVisibility(View.VISIBLE);
                    binding.googlePayButton.setVisibility(View.VISIBLE);
                } else {
                    // Hide Google as payment option.
                    binding.orSeparator.setVisibility(View.GONE);
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
                        .setTotalPrice(googlePayTxnAmount)
                        .setCurrencyCode(googlePayTxnCurrency)
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

    class TextChangeListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            enableContinueButton();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    class GooglePayCallback implements GatewayGooglePayCallback {
        @Override
        public void onReceivedPaymentData(PaymentData paymentData) {
            Log.d(GooglePayCallback.class.getSimpleName(), "ReceivedPaymentData: " + paymentData.getCardInfo().getCardDescription());

            returnCardInfo(paymentData);
        }

        @Override
        public void onGooglePayCancelled() {
            Log.d(GooglePayCallback.class.getSimpleName(), "Cancelled");
        }

        @Override
        public void onGooglePayError(Status status) {
            Log.d(GooglePayCallback.class.getSimpleName(), "Error");
            Toast.makeText(CollectCardInfoActivity.this, "Google Pay Error", Toast.LENGTH_SHORT).show();
        }
    }
}

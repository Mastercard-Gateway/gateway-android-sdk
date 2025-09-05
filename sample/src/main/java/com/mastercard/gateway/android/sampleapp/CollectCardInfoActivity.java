package com.mastercard.gateway.android.sampleapp;

import android.app.Activity;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.api.Status;
import com.mastercard.gateway.android.sampleapp.databinding.ActivityCollectCardInfoBinding;
import com.mastercard.gateway.android.sampleapp.utils.PaymentsClientWrapper;
import com.mastercard.gateway.android.sampleapp.utils.SimpleTextChangedWatcher;
import com.mastercard.gateway.android.sampleapp.viewmodel.CollectCardInfoViewModel;
import com.mastercard.gateway.android.sdk.Gateway;
import com.mastercard.gateway.android.sdk.GatewayGooglePayCallback;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.text.TextUtils.isEmpty;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CollectCardInfoActivity extends AppCompatActivity {

    private CollectCardInfoViewModel viewModel;

    @Inject
    PaymentsClientWrapper paymentsClientWrapper;

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
    SimpleTextChangedWatcher textChangeListener = new SimpleTextChangedWatcher(this::enableContinueButton);

    GooglePayCallback googlePayCallback = new GooglePayCallback();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_collect_card_info);

        viewModel = new ViewModelProvider(this).get(CollectCardInfoViewModel.class);

        viewModel.isGooglePayReady().observe(this, result -> {
            if ((result) != null) {
                if (((CollectCardInfoViewModel.GoogleCheckResult.Success) result).getResult()) {
                    // Show Google as payment option.
                    binding.orSeparator.setVisibility(VISIBLE);
                    binding.googlePayButton.setVisibility(VISIBLE);
                } else {
                    // Hide Google as payment option.
                    binding.orSeparator.setVisibility(GONE);
                    binding.googlePayButton.setVisibility(GONE);
                }
            }

        });


        viewModel.getLaunchGooglePay().observe(this, request -> {
            if (request != null) {
                Gateway.requestGooglePayData(
                        paymentsClientWrapper.getClient(),
                        request,
                        this
                );
            }
        });

        viewModel.checkGooglePay(paymentsClientWrapper);

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

        binding.submitButton.setOnClickListener(v -> continueButtonClicked());

        // init google pay button
        binding.googlePayButton.setOnClickListener(v -> viewModel.onGooglePayButtonClicked());
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
        String nameOnCard = Objects.requireNonNull(binding.nameOnCard.getText()).toString();
        String cardNumber = Objects.requireNonNull(binding.cardnumber.getText()).toString();
        String expiryMM = Objects.requireNonNull(binding.expiryMonth.getText()).toString();
        String expiryYY = Objects.requireNonNull(binding.expiryYear.getText()).toString();
        String cvv = Objects.requireNonNull(binding.cvv.getText()).toString();

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

    void returnCardInfo(JSONObject paymentData) {
        Intent i = new Intent();

        try {
            JSONObject paymentMethodData = paymentData.getJSONObject("paymentMethodData");
            String description = paymentMethodData.getString("description");
            String token = paymentMethodData.getJSONObject("tokenizationData")
                    .getString("token");

            i.putExtra(EXTRA_CARD_DESCRIPTION, description);
            i.putExtra(EXTRA_PAYMENT_TOKEN, token);

            setResult(Activity.RESULT_OK, i);
        } catch (Exception e) {
            setResult(Activity.RESULT_CANCELED, i);
        }

        finish();
    }

    String maskCardNumber(String number) {
        int maskLen = number.length() - 4;
        char[] mask = new char[maskLen];
        Arrays.fill(mask, '*');
        return new String(mask) + number.substring(maskLen);
    }

    class GooglePayCallback implements GatewayGooglePayCallback {
        @Override
        public void onReceivedPaymentData(JSONObject paymentData) {
            try {
                String description = paymentData.getJSONObject("paymentMethodData")
                        .getString("description");

            } catch (Exception e) {

            }

            returnCardInfo(paymentData);
        }

        @Override
        public void onGooglePayCancelled() {}

        @Override
        public void onGooglePayError(Status status) {
            Toast.makeText(CollectCardInfoActivity.this, "Google Pay Error", Toast.LENGTH_SHORT).show();
        }
    }
}

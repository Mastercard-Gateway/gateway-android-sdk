package com.mastercard.gateway.android.sampleapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonParseException;
import com.mastercard.gateway.android.sampleapp.databinding.ActivityCapturePaymentDetailsBinding;
import com.mastercard.gateway.android.sdk.Gateway;
import com.mastercard.gateway.android.sdk.api.GatewayCallback;
import com.mastercard.gateway.android.sdk.api.UpdateSessionResponse;

import java.net.MalformedURLException;
import java.util.Arrays;

import static android.text.TextUtils.isEmpty;

public class PaymentCaptureActivity extends AbstractActivity {

    ActivityCapturePaymentDetailsBinding binding;

    private SharedPreferences prefs = null;

    protected String nameOnCard, cardNumber, expiryMM, expiryYY, cvv, sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_capture_payment_details);

        sessionId = getIntent().getStringExtra("SESSION_ID");

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        binding.nameOnCard.requestFocus();
        binding.nameOnCard.addTextChangedListener(new TextChangeListener());
        binding.cardnumber.addTextChangedListener(new TextChangeListener());
        binding.expiryMonth.addTextChangedListener(new TextChangeListener());
        binding.expiryYear.addTextChangedListener(new TextChangeListener());
        binding.cvv.addTextChangedListener(new TextChangeListener());

        binding.submitButton.setEnabled(false);
        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyClicked(view);
            }
        });
    }

    public void buyClicked(View submitButton) {
        nameOnCard = binding.nameOnCard.getText().toString();
        cardNumber = binding.cardnumber.getText().toString();
        expiryMM = binding.expiryMonth.getText().toString();
        expiryYY = binding.expiryYear.getText().toString();
        cvv = binding.cvv.getText().toString();

        Log.i(getClass().getSimpleName(), "Making purchase");

        submitButton.setEnabled(false);

        Gateway gateway = new Gateway()
                .setMerchantId(BuildConfig.GATEWAY_MERCHANT_ID)
                .setBaseUrl(BuildConfig.GATEWAY_BASE_URL);

        gateway.updateSessionWithCardInfo(sessionId, nameOnCard, cardNumber, cvv, expiryMM, expiryYY, new UpdateSessionCallback());
    }

    private String maskedCardNumber() {
        int maskLen = cardNumber.length() - 4;
        char[] mask = new char[maskLen];
        Arrays.fill(mask, '*');
        return new String(mask) + cardNumber.substring(maskLen);
    }


    class UpdateSessionCallback implements GatewayCallback<UpdateSessionResponse> {

        @Override
        public void onSuccess(UpdateSessionResponse updateSessionResponse) {
            Intent intent = new Intent(PaymentCaptureActivity.this, PayActivity.class);
            intent.putExtra("PAN_MASK", maskedCardNumber());
            intent.putExtra("SESSION_ID", sessionId);
            Log.i(PaymentCaptureActivity.class.getSimpleName(), "Successful pay");

            startActivity(intent);
        }

        @Override
        public void onError(Throwable throwable) {
            Log.e(PaymentCaptureActivity.class.getSimpleName(), throwable.getMessage(), throwable);



            if (throwable instanceof MalformedURLException) {
                Toast.makeText(PaymentCaptureActivity.this, R.string.update_commserror_explanation_badurl, Toast.LENGTH_SHORT).show();
            } else if (throwable instanceof JsonParseException) {
                Toast.makeText(PaymentCaptureActivity.this, R.string.update_malformed_explanation_parse, Toast.LENGTH_SHORT).show();
            } else {
                Log.e(PaymentCaptureActivity.class.getSimpleName(),
                        "Unexpected error type " + throwable.getClass().getName());
                Toast.makeText(PaymentCaptureActivity.this, R.string.update_unknown_error_explanation, Toast.LENGTH_SHORT).show();
            }
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

    public void enableSubmitButton() {
        if (isEmpty(binding.nameOnCard.getText()) || isEmpty(binding.cardnumber.getText())
                || isEmpty(binding.expiryMonth.getText()) || isEmpty(binding.expiryYear.getText())
                || isEmpty(binding.cvv.getText()) || (binding.cvv.getText().toString().length() < 3)) {

            binding.submitButton.setEnabled(false);
        } else {
            binding.submitButton.setEnabled(true);
        }
    }
}

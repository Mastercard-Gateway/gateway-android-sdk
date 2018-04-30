package com.mastercard.gateway.android.sampleapp;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import com.mastercard.gateway.android.sampleapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    TextChangeListener textChangeListener = new TextChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.merchantId.setText(Config.MERCHANT_ID.getValue(this));
        binding.merchantId.addTextChangedListener(textChangeListener);

        binding.region.setText(Config.REGION.getValue(this));
        binding.region.addTextChangedListener(textChangeListener);

        binding.merchantUrl.setText(Config.MERCHANT_URL.getValue(this));
        binding.merchantUrl.addTextChangedListener(textChangeListener);

        binding.manualCardButton.setOnClickListener(v -> goTo(ProcessPaymentActivity.class));

        binding.googlePayButton.setOnClickListener(v -> goTo(GooglePayActivity.class));

        enableButtons();
    }

    void goTo(Class klass) {
        persistConfig();

        Intent i = new Intent(this, klass);
        startActivity(i);
    }

    void persistConfig() {
        Config.MERCHANT_ID.setValue(this, binding.merchantId.getText().toString());
        Config.REGION.setValue(this, binding.region.getText().toString());
        Config.MERCHANT_URL.setValue(this, binding.merchantUrl.getText().toString());

        // update api controller url
        ApiController.getInstance().setMerchantServerUrl(Config.MERCHANT_URL.getValue(this));
    }

    void enableButtons() {
        boolean enabled = !TextUtils.isEmpty(binding.merchantId.getText())
                && !TextUtils.isEmpty(binding.region.getText())
                && !TextUtils.isEmpty(binding.merchantUrl.getText());

        binding.manualCardButton.setEnabled(enabled);
        binding.googlePayButton.setEnabled(enabled);
    }

    class TextChangeListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            enableButtons();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }
}

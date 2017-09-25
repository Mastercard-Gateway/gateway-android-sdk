package com.mastercard.gateway.android.sampleapp;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mastercard.gateway.android.sampleapp.databinding.ActivityResultBinding;

/**
 * Show a screen which displays a payment status
 */
public class ResultActivity extends AppCompatActivity {

    ActivityResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_result);

        boolean success = getIntent().getBooleanExtra("SUCCESS", false);

        binding.resultText.setText(getString(success ? R.string.result_you_payment_was_successful : R.string.result_error_processing_your_payment));
        binding.resultIcon.setImageDrawable(ContextCompat.getDrawable(this, success ? R.drawable.success : R.drawable.failed));
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doContinue();
            }
        });
    }

    protected void doContinue() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}

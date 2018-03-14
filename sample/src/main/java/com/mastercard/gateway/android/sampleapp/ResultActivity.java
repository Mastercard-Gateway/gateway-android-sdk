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
        binding.continueBtn.setOnClickListener(view -> doContinue());
    }

    @Override
    public void onBackPressed() {
        doContinue();
    }

    void doContinue() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}

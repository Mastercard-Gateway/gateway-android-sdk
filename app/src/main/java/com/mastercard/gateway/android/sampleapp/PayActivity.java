package com.mastercard.gateway.android.sampleapp;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.mastercard.gateway.android.sampleapp.databinding.ActivityPayBinding;

import java.util.UUID;

/**
 * Display a payment confirmation screen and send the final pay request
 */
public class PayActivity extends AbstractActivity {

    ActivityPayBinding binding;

    String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_pay);

        binding.confirmCardNo.setText(getIntent().getStringExtra("PAN_MASK"));
        sessionId = getIntent().getStringExtra("SESSION_ID");

        binding.confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doConfirm();
            }
        });
    }

    protected void doConfirm() {
        binding.confirmBtn.setEnabled(false);

        // random order/txn IDs for example purposes
        String orderId = UUID.randomUUID().toString();
        orderId = orderId.substring(0, orderId.indexOf('-'));
        String transactionId = UUID.randomUUID().toString();
        transactionId = transactionId.substring(0, transactionId.indexOf('-'));

        apiController.completeSession(sessionId, orderId, transactionId, "250.00", "USD", new CompleteSessionCallback());
    }

    void startResultActivity(boolean success) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("SUCCESS", success);
        startActivity(intent);
    }

    class CompleteSessionCallback implements ApiController.CompleteSessionCallback {
        @Override
        public void onSuccess(String result) {
            startResultActivity(true);
            binding.confirmBtn.setEnabled(true);
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
            startResultActivity(false);
            binding.confirmBtn.setEnabled(true);
        }
    }
}

package com.mastercard.gateway.android.sampleapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.UUID;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Display a payment confirmation screen and send the final pay request
 */
public class PayActivity extends AbstractActivity {
    @Bind(R.id.confirmCardNo)
    TextView cardNumber;

    @Bind(R.id.confirmBtn)
    Button confirmBtn;

    String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cardNumber.setText(getIntent().getStringExtra("PAN_MASK"));
        sessionId = getIntent().getStringExtra("SESSION_ID");
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_pay;
    }

    @OnClick(R.id.confirmBtn)
    protected void doConfirm() {
        confirmBtn.setEnabled(false);

        // random order/txn IDs for example purposes
        String orderId = UUID.randomUUID().toString();
        orderId = orderId.substring(0, orderId.indexOf('-'));
        String transactionId = UUID.randomUUID().toString();
        transactionId = transactionId.substring(0, transactionId.indexOf('-'));

        apiController.completeSession(sessionId, orderId, transactionId, getResources().getString(R.string.main_activity_price), "USD", new CompleteSessionCallback());
    }

    class CompleteSessionCallback implements ApiController.CompleteSessionCallback {
        @Override
        public void onSuccess(String result) {
            startResultActivity(R.string.pay_successful_text, "", R.color.success_bg);
            confirmBtn.setEnabled(true);
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
            startResultActivity(R.string.pay_error_text, R.string.pay_error_explanation);
            confirmBtn.setEnabled(true);
        }
    }
}

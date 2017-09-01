package com.mastercard.gateway.android.sampleapp;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import butterknife.Bind;
import butterknife.OnClick;

public class MainActivity extends AbstractActivity {
    @Bind(R.id.productChooser)
    Spinner productField;

    @Bind(R.id.buyButton)
    Button buyButton;

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @OnClick(R.id.buyButton)
    public void buyClicked(View v) {
        buyButton.setEnabled(false);

        apiController.createSession(new CreateSessionCallback());
    }

    class CreateSessionCallback implements ApiController.CreateSessionCallback {
        @Override
        public void onSuccess(String sessionId) {
            Log.i("CreateSessionTask", "Session established");

            String[] productIds = getResources().getStringArray(R.array.productIds);
            String productId = productIds[productField.getSelectedItemPosition()];

            Intent intent = new Intent(MainActivity.this, PaymentCaptureActivity.class);
            intent.putExtra("PRODUCT_ID", productId);
            intent.putExtra("PRICE", getResources().getString(R.string.main_activity_price));
            intent.putExtra("CURRENCY", getResources().getString(R.string.main_activity_currency));
            intent.putExtra("SESSION_ID", sessionId);

            startActivity(intent);

            buyButton.setEnabled(true);
        }

        @Override
        public void onError(Throwable throwable) {
            startResultActivity(R.string.create_unrecognised_text, throwable.getMessage());

            buyButton.setEnabled(true);
        }
    }
}
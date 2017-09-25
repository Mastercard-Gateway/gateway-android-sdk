package com.mastercard.gateway.android.sampleapp;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mastercard.gateway.android.sampleapp.databinding.ActivityMainBinding;

public class MainActivity extends AbstractActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyClicked(view);
            }
        });
    }

    public void buyClicked(View v) {
        binding.buyButton.setEnabled(false);

        apiController.createSession(new CreateSessionCallback());
    }

    class CreateSessionCallback implements ApiController.CreateSessionCallback {
        @Override
        public void onSuccess(String sessionId) {
            Log.i("CreateSessionTask", "Session established");

            Intent intent = new Intent(MainActivity.this, PaymentCaptureActivity.class);
            intent.putExtra("SESSION_ID", sessionId);

            startActivity(intent);

            binding.buyButton.setEnabled(true);
        }

        @Override
        public void onError(Throwable throwable) {
            Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();

            binding.buyButton.setEnabled(true);
        }
    }
}
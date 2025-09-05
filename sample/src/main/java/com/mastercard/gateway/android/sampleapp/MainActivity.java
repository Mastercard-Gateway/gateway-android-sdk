package com.mastercard.gateway.android.sampleapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.mastercard.gateway.android.sampleapp.databinding.ActivityMainBinding;
import com.mastercard.gateway.android.sampleapp.utils.RegionInfo;
import com.mastercard.gateway.android.sampleapp.utils.SimpleTextChangedWatcher;
import com.mastercard.gateway.android.sampleapp.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Watcher that triggers enableButtons + saveSessionData
        SimpleTextChangedWatcher watcher = new SimpleTextChangedWatcher(() -> {
            enableButtons();
            viewModel.saveSessionData(
                    Objects.requireNonNull(binding.merchantId.getText()).toString(),
                    Objects.requireNonNull(binding.region.getText()).toString(),
                    Objects.requireNonNull(binding.merchantServerLink.getText()).toString()
            );
        });

        binding.merchantId.setText(viewModel.getMerchantId());
        binding.merchantId.addTextChangedListener(watcher);

        binding.region.setText(viewModel.getRegion());
        binding.region.addTextChangedListener(watcher);
        binding.region.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.region.clearFocus();
                showRegionPicker();
            }
        });

        binding.merchantServerLink.setText(viewModel.getMerchantServerLink());
        binding.merchantServerLink.addTextChangedListener(watcher);

        binding.processPaymentButton.setOnClickListener(v ->
                goTo(ProcessPaymentActivity.class)
        );

        enableButtons();
    }

    void goTo(Class<?> klass) {
        Intent i = new Intent(this, klass);
        startActivity(i);
    }

    void enableButtons() {
        boolean enabled = !TextUtils.isEmpty(binding.merchantId.getText())
                && !TextUtils.isEmpty(binding.region.getText());

        binding.processPaymentButton.setEnabled(enabled);
    }

    void showRegionPicker() {
        List<RegionInfo> regionsWithExtra = new ArrayList<>();
        regionsWithExtra.add(new RegionInfo(getString(R.string.none), ""));
        regionsWithExtra.addAll(viewModel.getRegions());

        String[] items;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            items = regionsWithExtra.stream()
                    .map(RegionInfo::getName)
                    .toArray(String[]::new);
        } else {
            items = null;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.main_select_region)
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        binding.region.setText("");
                    } else {
                        assert items != null;
                        binding.region.setText(items[which]);
                    }
                    dialog.cancel();
                })
                .show();
    }
}

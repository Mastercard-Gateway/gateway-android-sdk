package com.mastercard.gateway.android.sampleapp;

import static com.mastercard.gateway.android.sampleapp.utils.Ui.hide;
import static com.mastercard.gateway.android.sampleapp.utils.Ui.show;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.mastercard.gateway.android.sampleapp.databinding.ActivityProcessPaymentBinding;
import com.mastercard.gateway.android.sampleapp.utils.AuthAndBrowserHandler;
import com.mastercard.gateway.android.sampleapp.utils.PaymentOptionsParser;
import com.mastercard.gateway.android.sampleapp.utils.PaymentOptionsSheet;
import com.mastercard.gateway.android.sampleapp.viewmodel.GatewayResult;
import com.mastercard.gateway.android.sampleapp.viewmodel.ProcessPaymentViewModel;
import com.mastercard.gateway.android.sdk.Gateway;
import com.mastercard.gateway.android.sdk.GatewayMap;

import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProcessPaymentActivity extends AppCompatActivity implements AuthAndBrowserHandler.ResultUI {

    private ProcessPaymentViewModel viewModel;
    ActivityProcessPaymentBinding binding;

    boolean isGooglePay = false;

    PaymentOptionsSheet optionsSheet;
    AuthAndBrowserHandler handler;

    static final int REQUEST_CARD_INFO = 100;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_process_payment);
        viewModel = new ViewModelProvider(this).get(ProcessPaymentViewModel.class);

        optionsSheet = new PaymentOptionsSheet(this,
                LayoutInflater.from(this).inflate(R.layout.bottom_sheet_layout, null));
        handler = new AuthAndBrowserHandler(binding, this);

        initUI();
        setupObservers();

        binding.startButton.setOnClickListener(v -> createSession());
        binding.confirmButton.setOnClickListener(v -> processPayment(handler.isSkip3ds(), 7));
        binding.doneButton.setOnClickListener(v -> finish());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupObservers() {
        viewModel.getSessionResult().observe(this, result -> {
            hide(binding.createSessionProgress);
            if (result instanceof GatewayResult.Success) {
                show(binding.createSessionSuccess);
                executePaymentOptionEnquiry();
            } else {
                show(binding.createSessionError);
                showResult(R.drawable.failed, R.string.pay_error_unable_to_create_session);
            }
        });

        viewModel.getPaymentOptionsInquiryResult().observe(this, result -> {
            hide(binding.paymentOptionEnquiryProgress);
            if (result instanceof GatewayResult.Success) {
                GatewayMap response = ((GatewayResult.Success<GatewayMap>) result).getResponse();
                List<String> types = PaymentOptionsParser.extractTypes(response);
                if (types.isEmpty()) {
                    show(binding.paymentOptionEnquiryError);
                } else {
                    show(binding.paymentOptionEnquirySuccess);
                    show(binding.selectPaymentOptionLabel);
                    show(binding.selectPaymentOptionProgress);
                    optionsSheet.show(types, this::onPaymentOptionChosen);
                }
            } else {
                show(binding.paymentOptionEnquiryError);
            }
        });

        viewModel.getCompleteSessionResult().observe(this, result -> {
            hide(binding.processPaymentProgress);
            if (result instanceof GatewayResult.Success) {
                show(binding.processPaymentSuccess);
                showResult(R.drawable.success, R.string.pay_you_payment_was_successful);
            } else {
                hide(binding.processPaymentSuccess);
                show(binding.processPaymentError);
                showResult(R.drawable.failed, R.string.pay_error_unable_to_complete_payment);
            }
        });

        viewModel.getTransactionResult().observe(this, result -> {
            if (result instanceof GatewayResult.Success) {
                GatewayMap response = ((GatewayResult.Success<GatewayMap>) result).getResponse();
                String txnResult = safeString(response.get("result"));
                if ("NOT_SUPPORTED".equals(txnResult)) handler.setSkip3ds(true);
                else viewModel.initiateAuthentication();
            } else {
                hide(binding.check3dsProgress);
                show(binding.check3dsError);
                showResult(R.drawable.failed, R.string.pay_error_3ds_authentication_failed);
            }
        });

        viewModel.getStartAuthenticationResult().observe(this, result -> {
            hide(binding.initiateAuthenticationProgress);
            if (result instanceof GatewayResult.Success) {
                handler.handleAuthentication(((GatewayResult.Success<GatewayMap>) result).getResponse());
            } else {
                show(binding.initiateAuthenticationError);
                showResult(R.drawable.failed, R.string.pay_error_3ds_authentication_failed);
            }
        });

        viewModel.getUpdateSessionResult().observe(this, result -> {
            hide(binding.updateSessionProgress);
            if (result instanceof GatewayResult.Success) {
                show(binding.updateSessionSuccess);
                hide(binding.startButton);
                if (isGooglePay) {
                    show(binding.groupConfirm);
                    //processPayment(true, 6);
                } else {
                    initiateAuthenticate();
                }
            } else {
                show(binding.updateSessionError);
                showResult(R.drawable.failed, R.string.pay_error_unable_to_update_session);
            }
        });

        viewModel.getBrowserPaymentResult().observe(this, result -> {
            hide(binding.check3dsProgress);
            if (result instanceof GatewayResult.Success) {
                show(binding.check3dsSuccess);
                String html = safeString(((GatewayResult.Success<GatewayMap>) result)
                        .getResponse().get("browserPayment.redirectHtml"));
                if (html != null) {
                    show(binding.authenticateBrowserPaymentLabel);
                    show(binding.authenticateBrowserPaymentProgress);
                    startBrowserPayment(html);
                }
            } else {
                show(binding.check3dsError);
                showResult(R.drawable.failed, R.string.pay_error_3ds_authentication_failed);
            }
        });
    }

    private void onPaymentOptionChosen(String option) {
        hide(binding.selectPaymentOptionProgress);
        show(binding.selectPaymentOptionSuccess);
        viewModel.saveCurrencyForSelectedFlow(option);

        switch (option) {
            case "CARD":
                collectCardInfo();
                break;
            case "KNET":
            case "BENEFIT":
            case "QPAY":
            case "OMAN":
                initiateBrowserPayment();
                break;
            default:
        }
    }

    void initUI() {
        binding.startButton.setEnabled(true);
        binding.confirmButton.setEnabled(true);
        show(binding.startButton);
        hide(binding.groupConfirm);
        hide(binding.groupResult);
    }

    void createSession() {
        binding.startButton.setEnabled(false);
        show(binding.createSessionProgress);
        show(binding.createSessionLabel);
        GatewayMap payload = new GatewayMap().set("session.authenticationLimit", 25);
        viewModel.createSession(payload);
    }

    void executePaymentOptionEnquiry() {
        show(binding.paymentOptionEnquiryProgress);
        show(binding.paymentOptionEnquiryLabel);
        viewModel.inquirePaymentOptions();
    }

    void collectCardInfo() {
        show(binding.collectCardInfoLabel);
        show(binding.collectCardInfoProgress);
        Intent i = new Intent(this, CollectCardInfoActivity.class);
        i.putExtra(CollectCardInfoActivity.EXTRA_GOOGLE_PAY_TXN_AMOUNT, ProcessPaymentViewModel.AMOUNT);
        i.putExtra(CollectCardInfoActivity.EXTRA_GOOGLE_PAY_TXN_CURRENCY, viewModel.getCURRENCY());
        startActivityForResult(i, REQUEST_CARD_INFO);
    }

    void updateSession(String paymentToken) {
        show(binding.updateSessionLabel);
        show(binding.updateSessionProgress);
        viewModel.updateSession(paymentToken, "", "", "", "");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void initiateAuthenticate() {
        show(binding.initiateAuthenticationLabel);
        show(binding.initiateAuthenticationProgress);
        viewModel.initiateAuthentication();
    }

    void initiateBrowserPayment() {
        show(binding.check3dsLabel);
        show(binding.check3dsProgress);
        binding.confirmButton.setEnabled(false);
        viewModel.initiateBrowserPayment();
    }

    @SuppressLint("SetTextI18n")
    void processPayment(boolean skip3ds, int step) {
        show(binding.processPaymentLabel);
        show(binding.processPaymentProgress);
        if (skip3ds) {
            binding.processPaymentLabel.setText(step + "." + getString(R.string.pay_process_payment));
        }
        viewModel.submitTransaction(isGooglePay);
    }


    @Override public void showResult(@DrawableRes int iconRes, @StringRes int msgRes) {
        binding.resultIcon.setImageResource(iconRes);
        binding.resultText.setText(msgRes);
        hide(binding.groupConfirm);
        show(binding.groupResult);
    }

    @Override public void showResult(@DrawableRes int iconRes, @NonNull String message) {
        binding.resultIcon.setImageResource(iconRes);
        binding.resultText.setText(message);
        hide(binding.groupConfirm);
        show(binding.groupResult);
    }

    @Override public void start3DS(String redirectHtml) {
        Gateway.start3DSecureActivity(this, redirectHtml);
    }

    @Override public void startBrowserPayment(String redirectHtml) {
        Gateway.startGatewayBrowserPaymentActivity(this, redirectHtml);
    }

    @Override public void onReadyToConfirm() {
        show(binding.groupConfirm);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Gateway.handleGatewayResult(requestCode, resultCode, data, handler.makeGatewayCallback())) return;

        if (requestCode == REQUEST_CARD_INFO) {
            hide(binding.collectCardInfoProgress);
            if (resultCode == Activity.RESULT_OK) {
                show(binding.collectCardInfoSuccess);
                show(binding.updateSessionLabel);
                show(binding.updateSessionProgress);
                binding.confirmCardDescription.setText(data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_DESCRIPTION));

                String googlePayToken = data.getStringExtra(CollectCardInfoActivity.EXTRA_PAYMENT_TOKEN);
                if (googlePayToken != null) {
                    isGooglePay = true;
                    binding.check3dsLabel.setPaintFlags(binding.check3dsLabel.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    updateSession(googlePayToken);
                } else {
                    isGooglePay = false;
                    viewModel.updateSession(
                            null,
                            Objects.requireNonNull(data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_NUMBER)),
                            Objects.requireNonNull(data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_EXPIRY_MONTH)),
                            Objects.requireNonNull(data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_EXPIRY_YEAR)),
                            Objects.requireNonNull(data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_CVV))
                    );
                }
            } else {
                show(binding.collectCardInfoError);
                showResult(R.drawable.failed, R.string.pay_error_card_info_not_collected);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static String safeString(Object o) { return (o instanceof String) ? (String) o : null; }
}
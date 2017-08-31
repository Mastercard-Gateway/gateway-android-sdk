package com.mastercard.gateway.android.sampleapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

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
//        new CompleteSessionTask().execute();

        apiController.completeSession(sessionId, getResources().getString(R.string.main_activity_price), "USD", new CompleteSessionCallback());
    }

    class CompleteSessionCallback implements ApiController.CompleteSessionCallback {
        @Override
        public void onSuccess(String result) {
            startResultActivity(R.string.pay_successful_text, "", R.color.success_bg);
            confirmBtn.setEnabled(true);
        }

        @Override
        public void onError(Throwable throwable) {
            startResultActivity(R.string.pay_error_text, R.string.pay_error_explanation);
            confirmBtn.setEnabled(true);
        }
    }

//    protected class CompleteSessionTask extends AsyncTask<String, Long, MerchantSimulatorResponse> {
//        protected MerchantSimulatorResponse doInBackground(String... params) {
//            SharedPreferences prefs =
//                    PreferenceManager.getDefaultSharedPreferences(getBaseContext());
//
//            String behaviour = prefs.getString("pref_key_complete_behaviour",
//                    getResources().getString(R.string.behaviour_succeed));
//
//            return apiController.completeSession(getIntent().getStringExtra("PRODUCT_ID"),
//                    getResources().getString(R.string.main_activity_price), behaviour,
//                    timeout(prefs));
//        }
//
//        protected void onPostExecute(MerchantSimulatorResponse response) {
//            if (response == null || response.status == null) {
//                startResultActivity(R.string.pay_comms_error_text,
//                        R.string.pay_comms_error_explanation);
//            } else {
//                switch (response.status) {
//                    case "SUCCESS":
//                        startResultActivity(R.string.pay_successful_text, "", R.color.success_bg);
//                        break;
//                    case "TIMEOUT":
//                        startResultActivity(R.string.pay_timeout_text,
//                                R.string.pay_timeout_explanation);
//                        break;
//                    case "DENIED":
//                        startResultActivity(R.string.pay_denied_text, "");
//                        break;
//                    default:
//                        startResultActivity(R.string.pay_error_text,
//                                R.string.pay_error_explanation);
//                }
//            }
//
//            confirmBtn.setEnabled(true);
//        }
//    }
}

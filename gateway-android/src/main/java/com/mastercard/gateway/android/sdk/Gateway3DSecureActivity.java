package com.mastercard.gateway.android.sdk;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class Gateway3DSecureActivity extends AppCompatActivity {

    public static final int REQUEST_3DS = 14137;

    public static final String EXTRA_TITLE = "com.mastercard.gateway.android.TITLE";
    public static final String EXTRA_REQUEST_HTML = "com.mastercard.gateway.android.HTML";
    public static final String EXTRA_RESPONSE_3DSECURE_ID = "com.mastercard.gateway.android.3DSECURE_ID";
    public static final String EXTRA_RESPONSE_SUMMARY_STATUS = "com.mastercard.gateway.android.SUMMARY_STATUS";
    public static final String EXTRA_RESPONSE_ERROR = "com.mastercard.gateway.android.ERROR";

    private static final String REDIRECT_SCHEME = "gatewaysdk:";
    private static final String QUERY_3DSECURE_ID = "3DSecureId";
    private static final String QUERY_SUMMARY_STATUS = "summaryStatus";


    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3dsecure);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        WebView webView = findViewById(R.id.webview);
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String title = extras.getString(EXTRA_TITLE, getString(R.string.gateway_3dsecure_card_authentication));
            toolbar.setTitle(title);

            String html = extras.getString(EXTRA_REQUEST_HTML);
            if (html == null) {
                onBackPressed();
            } else {
                webView.loadData(html, "text/html", "utf-8");
            }
        } else {
            onBackPressed();
        }
    }

    void handle3DSecureResult(String url) {
        String threeDSecureId = null;
        SummaryStatus summaryStatus = null;
        boolean error = true;

        try {
            Uri uri = Uri.parse(url);
            threeDSecureId = uri.getQueryParameter(QUERY_3DSECURE_ID);
            summaryStatus = SummaryStatus.valueOf(uri.getQueryParameter(QUERY_SUMMARY_STATUS).toUpperCase());
            error = summaryStatus == SummaryStatus.AUTHENTICATION_FAILED;
        } catch (Exception e) {
            // unable to parse or find result data
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_RESPONSE_3DSECURE_ID, threeDSecureId);
        data.putExtra(EXTRA_RESPONSE_SUMMARY_STATUS, summaryStatus);
        data.putExtra(EXTRA_RESPONSE_ERROR, error);

        setResult(Activity.RESULT_OK, data);
        finish();
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(REDIRECT_SCHEME)) {
                handle3DSecureResult(url);
            } else if (url.startsWith("mailto:")) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse(url));
                emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(emailIntent);
            } else {
                view.loadUrl(url);
            }

            return true;
        }
    }
}

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

    /**
     * The HTML used to initialize the WebView. SHould be the HTML content returned from the Gateway
     * during the Check 3DS Enrollment call
     */
    public static final String EXTRA_HTML = "com.mastercard.gateway.android.HTML";

    /**
     * An OPTIONAL title to display in the toolbar for this activity
     */
    public static final String EXTRA_TITLE = "com.mastercard.gateway.android.TITLE";

    /**
     * The 3-D Secure Id returned from the Gateway on the Process ACS Result call
     * Will not be set on error
     */
    public static final String EXTRA_3D_SECURE_ID = "com.mastercard.gateway.android.3D_SECURE_ID";

    /**
     * The summary status returned from the Gateway on the Process ACS Result call
     * Will not be set on error
     */
    public static final String EXTRA_SUMMARY_STATUS = "com.mastercard.gateway.android.SUMMARY_STATUS";

    /**
     * A message indicating there was an error reading response data.
     * Will not be set on success
     */
    public static final String EXTRA_ERROR = "com.mastercard.gateway.android.ERROR";


    static final String REDIRECT_SCHEME = "gatewaysdk:";
    static final String QUERY_3DSECURE_ID = "3DSecureId";
    static final String QUERY_SUMMARY_STATUS = "summaryStatus";


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
            String title = extras.getString(EXTRA_TITLE, getString(R.string.gateway_3d_secure_authentication));
            toolbar.setTitle(title);

            String html = extras.getString(EXTRA_HTML);
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
        String summaryStatus = null;
        String threeDSecureId = null;

        // parse response info from url redirect
        try {
            Uri uri = Uri.parse(url);
            summaryStatus = uri.getQueryParameter(QUERY_SUMMARY_STATUS);
            threeDSecureId = uri.getQueryParameter(QUERY_3DSECURE_ID);
        } catch (Exception e) {
            // unable to parse or find result data
        }

        // check that we got the correct data back
        String error = null;
        if (summaryStatus == null) {
            error = getString(R.string.gateway_error_missing_summary_status);
        } else if (threeDSecureId == null) {
            error = getString(R.string.gateway_error_missing_3d_secure_id);
        }

        // build result data
        Intent data = new Intent();
        if (error != null) {
            data.putExtra(EXTRA_ERROR, error);
        } else {
            data.putExtra(EXTRA_SUMMARY_STATUS, summaryStatus);
            data.putExtra(EXTRA_3D_SECURE_ID, threeDSecureId);
        }

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

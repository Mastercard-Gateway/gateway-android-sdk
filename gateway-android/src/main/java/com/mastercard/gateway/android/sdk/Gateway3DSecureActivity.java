package com.mastercard.gateway.android.sdk;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Base64;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Set;


public class Gateway3DSecureActivity extends AppCompatActivity {

    /**
     * The HTML used to initialize the WebView. Should be the HTML content returned from the Gateway
     * during the Check 3DS Enrollment call
     */
    public static final String EXTRA_HTML = "com.mastercard.gateway.android.HTML";

    /**
     * An OPTIONAL title to display in the toolbar for this activity
     */
    public static final String EXTRA_TITLE = "com.mastercard.gateway.android.TITLE";

    /**
     * The ACS Result data after performing 3DS
     */
    public static final String EXTRA_ACS_RESULT = "com.mastercard.gateway.android.ACS_RESULT";


    static final String REDIRECT_SCHEME = "gatewaysdk";

    Toolbar toolbar;
    WebView webView;


    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3dsecure);

        // init toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        // init web view
        webView = findViewById(R.id.webview);
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(buildWebViewClient());

        init();
    }

    void init() {
        // init html
        String extraHtml = getExtraHtml();
        if (extraHtml == null) {
            onBackPressed();
            return;
        } else {
            setWebViewHtml(extraHtml);
        }

        // init title
        String defaultTitle = getDefaultTitle();
        String extraTitle = getExtraTitle();
        setToolbarTitle(extraTitle != null ? extraTitle : defaultTitle);
    }

    String getDefaultTitle() {
        return getString(R.string.gateway_3d_secure_authentication);
    }

    String getExtraTitle() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            return extras.getString(EXTRA_TITLE);
        }

        return null;
    }

    String getExtraHtml() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            return extras.getString(EXTRA_HTML);
        }

        return null;
    }

    void setToolbarTitle(String title) {
        toolbar.setTitle(title);
    }

    void setWebViewHtml(String html) {
        String encoded = Base64.encodeToString(html.getBytes(), Base64.NO_PADDING | Base64.NO_WRAP);
        webView.loadData(encoded, "text/html", "base64");
    }

    void webViewUrlChanges(Uri uri) {
        String scheme = uri.getScheme();
        if (REDIRECT_SCHEME.equalsIgnoreCase(scheme)) {
            complete(getACSResultFromUri(uri));
        } else if ("mailto".equalsIgnoreCase(scheme)) {
            intentToEmail(uri);
        } else {
            loadWebViewUrl(uri);
        }
    }

    void complete(String acsResult) {
        Intent intent = new Intent();
        complete(acsResult, intent);
    }

    // separate for testability
    void complete(String acsResult, Intent intent) {
        intent.putExtra(EXTRA_ACS_RESULT, acsResult);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    void loadWebViewUrl(Uri uri) {
        webView.loadUrl(uri.toString());
    }

    void intentToEmail(Uri uri) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        intentToEmail(uri, emailIntent);
    }

    // separate for testability
    void intentToEmail(Uri uri, Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(uri);

        startActivity(intent);
    }

    String getACSResultFromUri(Uri uri) {
        String result = null;

        Set<String> params = uri.getQueryParameterNames();
        for (String param : params) {
            if ("acsResult".equalsIgnoreCase(param)) {
                result = uri.getQueryParameter(param);
            }
        }

        return result;
    }

    WebViewClient buildWebViewClient() {
        return new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webViewUrlChanges(Uri.parse(url));
                return true;
            }
        };
    }
}

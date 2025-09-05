package com.mastercard.gateway.android.sdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mastercard.gateway.android.R;

import java.util.Set;

abstract public class BaseGatewayPaymentActivity extends AppCompatActivity {

    /** Common extras */
    public static final String EXTRA_HTML  = "com.mastercard.gateway.android.HTML";
    public static final String EXTRA_TITLE = "com.mastercard.gateway.android.TITLE";

    static final String EXTRA_GATEWAY_RESULT = "com.mastercard.gateway.android.GATEWAY_RESULT";
    public static final String REDIRECT_SCHEME = "gatewaysdk";

    protected Toolbar toolbar;
    protected WebView webView;
    private ProgressBar progressBar;

    @LayoutRes protected int contentLayoutResId() { return R.layout.activity_3dsecure; }
    protected int toolbarId() { return R.id.toolbar; }
    protected int webViewId() { return R.id.webview; }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(contentLayoutResId());

        toolbar = findViewById(toolbarId());
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        webView = findViewById(webViewId());
        progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.bringToFront();
            progressBar.setVisibility(View.VISIBLE);
        }

        WebSettings settings = webView.getSettings();
        configureWebView(settings);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(buildWebViewClient());

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            init(); // may trigger first load safely (progressBar already set)
        }
    }

    /** Initialization: title + initial content */
    protected void init() {
        setToolbarTitle(getTitleFromIntentOrDefault());
        final String html = getExtraHtml();
        if (html != null) {
            setWebViewHtml(html);
        } else {
            onBackPressed();
        }
    }

    protected @Nullable String getExtraHtml() {
        final Bundle extras = getIntent() != null ? getIntent().getExtras() : null;
        return extras != null ? extras.getString(EXTRA_HTML) : null;
    }

    protected @Nullable String getExtraTitle() {
        final Bundle extras = getIntent() != null ? getIntent().getExtras() : null;
        return extras != null ? extras.getString(EXTRA_TITLE) : null;
    }

    /** Baseline WebView settings; override for per-activity tweaks */
    protected void configureWebView(@NonNull WebSettings s) {
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
    }

    /** Title: intent extra wins; otherwise child provides a default */
    protected String getTitleFromIntentOrDefault() {
        final String extraTitle = getExtraTitle();
        return (extraTitle != null && !extraTitle.isEmpty()) ? extraTitle : getDefaultTitle();
    }

    protected void setToolbarTitle(@NonNull String title) {
        if (toolbar != null) toolbar.setTitle(title);
    }

    @NonNull protected abstract String gatewayHost();
    @NonNull protected abstract String getDefaultTitle();
    protected abstract void onGatewayRedirect(@NonNull Uri uri);
    @NonNull protected String getRedirectScheme() { return REDIRECT_SCHEME; }

    @NonNull protected WebViewClient buildWebViewClient() {
        return new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(Uri.parse(url));
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request != null ? request.getUrl() : null);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }

            private boolean handleUrl(@Nullable Uri uri) {
                if (uri == null) return false;

                final String scheme = uri.getScheme() != null ? uri.getScheme() : "";
                if (getRedirectScheme().equalsIgnoreCase(scheme)) {
                    final String host = uri.getHost() != null ? uri.getHost() : "";
                    if (host.equalsIgnoreCase(gatewayHost())) {
                        webViewUrlChanges(uri);
                        return true;
                    }
                    return false;
                }

                return false;
            }
        };
    }

    protected void webViewUrlChanges(@Nullable Uri uri) {
        if (uri == null) return;

        final String scheme = uri.getScheme() != null ? uri.getScheme() : "";
        if (getRedirectScheme().equalsIgnoreCase(scheme)) {
            final String host = uri.getHost() != null ? uri.getHost() : "";
            if (host.equalsIgnoreCase(gatewayHost())) {
                onGatewayRedirect(uri);
                return;
            }
        }

        if ("mailto".equalsIgnoreCase(scheme)) {
            intentToEmail(uri);
        } else {
            loadWebViewUrl(uri);
        }
    }

    protected @Nullable String getQueryParam(@NonNull Uri uri, @NonNull String key) {
        Set<String> params = uri.getQueryParameterNames();
        for (String p : params) {
            if (key.equalsIgnoreCase(p)) return uri.getQueryParameter(p);
        }
        return null;
    }

    protected void setWebViewHtml(@NonNull String html) {
        String encoded = Base64.encodeToString(html.getBytes(), Base64.NO_PADDING | Base64.NO_WRAP);
        webView.loadData(encoded, "text/html", "base64");
    }

    protected void loadWebViewUrl(@NonNull Uri uri) {
        webView.loadUrl(uri.toString());
    }

    protected void intentToEmail(@NonNull Uri uri) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setData(uri);
        startActivity(emailIntent);
    }

    protected void complete(@NonNull String extraKey, @Nullable String value) {
        Intent data = new Intent();
        data.putExtra(extraKey, value);
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
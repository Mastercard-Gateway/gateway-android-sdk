package com.mastercard.gateway.android.sampleapp;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mastercard.gateway.android.sampleapp.databinding.Activity3DSecureBinding;

public class ThreeDSecureActivity extends AppCompatActivity {

    public static String EXTRA_HTML = "com.mastercard.gateway.android.HTML";

    Activity3DSecureBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_3_d_secure);

        binding.toolbar.setNavigationOnClickListener(view -> onBackPressed());

        binding.webview.setWebViewClient(new WebViewClient() {
            // shouldOverrideUrlLoading makes this `WebView` the default handler for URLs inside the app, so that links are not kicked out to other apps.
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Use an external email program if the link begins with "mailto:".
                if (url.startsWith("mailto:")) {
                    // We use `ACTION_SENDTO` instead of `ACTION_SEND` so that only email programs are launched.
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

                    // Parse the url and set it as the data for the `Intent`.
                    emailIntent.setData(Uri.parse(url));

                    // `FLAG_ACTIVITY_NEW_TASK` opens the email program in a new task instead as part of this application.
                    emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    // Make it so.
                    startActivity(emailIntent);
                    return true;
                } else {  // Load the URL in `webView`.
                    view.loadUrl(url);
                    return true;
                }
            }
        });

        String html = getIntent().getStringExtra(EXTRA_HTML);
        if (html != null) {
            binding.webview.loadData(html, "text/html", "utf-8");
        }
    }
}

package com.happydental.kidsadventure;

import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        // Inner assets handler that delegates to AssetsPathHandler with "public/" prefix
        final WebViewAssetLoader.AssetsPathHandler innerHandler =
                new WebViewAssetLoader.AssetsPathHandler(this);

        // Custom PathHandler wrapping innerHandler, prepending "public/" to every path
        WebViewAssetLoader.PathHandler publicPathHandler = path -> innerHandler.handle("public/" + path);

        // Map root "/" -> assets/public/ so that:
        //   https://appassets.android.com/index.html       -> assets/public/index.html
        //   https://appassets.android.com/assets/xxx.js    -> assets/public/assets/xxx.js
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .setDomain("appassets.android.com")
                .addPathHandler("/", publicPathHandler)
                .build();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        // Load the React app via a virtual HTTPS origin (required for ES modules)
        webView.loadUrl("https://appassets.android.com/index.html");
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

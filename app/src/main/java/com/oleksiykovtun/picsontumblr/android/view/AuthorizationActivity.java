package com.oleksiykovtun.picsontumblr.android.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.Toolbar;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.oleksiykovtun.picsontumblr.android.R;
import com.oleksiykovtun.picsontumblr.android.manager.AccountManager;
import com.pnikosis.materialishprogress.ProgressWheel;

/**
 * The Tumblr authorization activity
 */
public class AuthorizationActivity extends Activity {

    public static final int CODE = 1;

    private static String authorizationUrl = "";

    private WebView webView;
    private ProgressWheel progressWheel;

    private boolean loadingFinished = true;
    private boolean redirect = false;

    public static void setAuthorizationUrl(String authorizationUrl) {
        AuthorizationActivity.authorizationUrl = authorizationUrl;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        progressWheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        progressWheel.setProgress(0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Signing in Tumblr");

        webView = setupWebView();
        webView.loadUrl(authorizationUrl);

        pollResultPeriodically();
    }

    private WebView setupWebView() {
        webView = (WebView) findViewById(R.id.web_view);
        CookieManager.getInstance().removeAllCookie();
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSaveFormData(false);
        webView.setBackgroundColor(getResources().getColor(R.color.background_material_dark));
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                if (!loadingFinished) {
                    redirect = true;
                }
                loadingFinished = false;
                webView.loadUrl(urlNewString);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loadingFinished = false;
                progressWheel.spin();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!redirect) {
                    loadingFinished = true;
                }
                if (loadingFinished && !redirect) {
                    progressWheel.setProgress(0);
                } else {
                    redirect = false;
                }
            }
        });
        return webView;
    }

    private void pollResultPeriodically() {
        new CountDownTimer(Integer.MAX_VALUE, 1000) {

            public void onTick(long millisUntilFinished) {
                String url = webView.getUrl();
                if (url != null && url.contains("oauth_verifier=")) {
                    cancel();
                    CookieManager.getInstance().removeAllCookie();
                    Bundle conData = new Bundle();
                    String verifierString = url.substring(url.lastIndexOf("=") + 1);
                    conData.putString(AccountManager.TAG_VERIFIER, verifierString);
                    Intent intent = new Intent();
                    intent.putExtras(conData);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

            public void onFinish() { }

        }.start();
    }

}

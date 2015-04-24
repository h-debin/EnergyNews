package com.energynews.app.activity;

import com.energynews.app.R;
import com.energynews.app.util.LogUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class NewsContentActivity extends Activity {

	
	private final static String DEBUG_TAG = "NewsContentActivity";
	
	public static void actionStart(Context context, String url) {
		LogUtil.d(DEBUG_TAG,"actionStart");
		Intent intent = new Intent(context, NewsContentActivity.class);
		intent.putExtra("url", url);
		context.startActivity(intent);
	}

    private CircularProgressBar circularProgressBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.d(DEBUG_TAG,"onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.news_contant);

        circularProgressBar = (CircularProgressBar) findViewById(R.id.circular_progressbar);
		WebView webView = (WebView) findViewById(R.id.new_content_web_view);
		webView.getSettings().setJavaScriptEnabled(true);		
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                LogUtil.d(DEBUG_TAG,"onPageStarted");
                startProgress();
                super.onPageStarted(view, url, favicon);
            }
            @Override
            public void onPageFinished (WebView view, String url) {
                LogUtil.d(DEBUG_TAG,"onPageFinished");
                finishProgress();
                super.onPageFinished(view, url);
            }
            @Override
            public void onReceivedError (WebView view, int errorCode, String description, String failingUrl) {
                LogUtil.d(DEBUG_TAG,"onReceivedError");
                finishProgress();
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
		});
		Intent intent = getIntent();
		String url = intent.getStringExtra("url");
		webView.loadUrl(url);
	}

    private void startProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                circularProgressBar.setVisibility(View.VISIBLE);
            }
        });
    }
    private void finishProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                circularProgressBar.setVisibility(View.GONE);
            }
        });
    }
	
}

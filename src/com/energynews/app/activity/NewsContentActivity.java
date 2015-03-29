package com.energynews.app.activity;

import com.energynews.app.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NewsContentActivity extends BaseActivity {

	private WebView webView;
	
	public static void actionStart(Context context, String url) {
		Intent intent = new Intent(context, NewsContentActivity.class);
		intent.putExtra("url", url);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.news_contant);
		webView = (WebView) findViewById(R.id.new_content_web_view);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		Intent intent = getIntent();
		String url = intent.getStringExtra("url");
		webView.loadUrl(url);
	}
}

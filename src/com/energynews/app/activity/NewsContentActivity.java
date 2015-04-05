package com.energynews.app.activity;

import com.energynews.app.R;
import com.energynews.app.util.LogUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class NewsContentActivity extends BaseActivity {

	private WebView webView;
	private TextView titleView;
	
	private final static String DEBUG_TAG = "NewsContentActivity";
	
	public static void actionStart(Context context, String url, int color) {
		LogUtil.d(DEBUG_TAG,"actionStart");
		Intent intent = new Intent(context, NewsContentActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("color", color);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LogUtil.d(DEBUG_TAG,"onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.news_contant);
		titleView = (TextView) findViewById(R.id.content_title);
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
		int color = intent.getIntExtra("color", 0xff051409);
		titleView.setBackgroundColor(color);
		String url = intent.getStringExtra("url");
		webView.loadUrl(url);
		overridePendingTransition(R.anim.center_in, R.anim.center_out);
	}
	
}

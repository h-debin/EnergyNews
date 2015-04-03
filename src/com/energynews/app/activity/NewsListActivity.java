package com.energynews.app.activity;

import com.energynews.app.R;
import com.energynews.app.data.NewsManager;
import com.energynews.app.fragment.NewsTitleFragment;
import com.energynews.app.util.LogUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class NewsListActivity extends BaseActivity {
	
	private final static String DEBUG_TAG = "NewsListActivity";
	
	private TextView homeTitleTextView;
	
	private NewsTitleFragment titleFragment = null;
	
	private static final int[] BACK_COLOR = {0xff051409, 0xff4F4F4F, 0xff8B1A1A, 0xff8B3A62,
		0xff330000, 0xff333300, 0xff330033, 0xff003333, 0xff660033, 0xff660066, 0xff000066,
		0xff336699};
	private static int colorId = 0;
	
	public static void actionStart(Context context) {
		LogUtil.d(DEBUG_TAG,"actionStart");
		Intent intent = new Intent(context, NewsListActivity.class);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LogUtil.d(DEBUG_TAG,"onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.news_list);
        homeTitleTextView = (TextView) findViewById(R.id.home_title_text);
        titleFragment = (NewsTitleFragment) getFragmentManager().
        		findFragmentById(R.id.news_title_fragment);
		overridePendingTransition(R.anim.center_in, R.anim.center_out);
	}
	
	public void changeEmotionTitleText() {
		LogUtil.d(DEBUG_TAG,"changeEmotionTitleText");
		if (homeTitleTextView != null) {
			String emotion = NewsManager.getInstance(this).getCurrentText();
			homeTitleTextView.setText(emotion);
		}
	}
	
	@Override
	protected void onRestart() {
		LogUtil.d(DEBUG_TAG,"onRestart");
		//AutoUpdateService.actionStop(this);
		super.onDestroy();
		overridePendingTransition(R.anim.center_in, R.anim.center_out);
	}
	
	@Override
	protected void onDestroy() {
		LogUtil.d(DEBUG_TAG,"onDestroy");
		//AutoUpdateService.actionStop(this);
		super.onDestroy();
	}
	
	/**
	 * 单击标题栏,更新当前新闻
	 */
	@Override
	protected void singleTapConfirmed() {
		LogUtil.d(DEBUG_TAG,"singleTapConfirmed");
		titleFragment.refreshFromServer();
	}
	
	/**
	 * 滑动事件
	 */
	@Override
	protected void onFlingEvent(float xdis, float ydis) {
		LogUtil.d(DEBUG_TAG,"onFlingEvent");
		int idOrien = (int)(xdis/Math.abs(xdis));//移动方向+-1
		colorId = (colorId + idOrien + BACK_COLOR.length) % BACK_COLOR.length;
		int color = BACK_COLOR[colorId];
		homeTitleTextView.setBackgroundColor(color);
		titleFragment.changeBackgroundColor(color);
	}
	
	/**
	 * 滑动事件过程
	 */
	@Override
	protected void onScrollEvent(float xdis, float ydis) {
		LogUtil.d(DEBUG_TAG,"onFlingEvent");
	}
	
	public int getBackColor() {
		return BACK_COLOR[colorId];
	}
	
}

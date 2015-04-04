package com.energynews.app.activity;

import net.youmi.android.AdManager;
import net.youmi.android.spot.SpotDialogListener;
import net.youmi.android.spot.SpotManager;

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
	
	private static int animCount = 0;
	private static int adCount = 0;
	private static int adFrequency = 10;//动画启动多少次后出一条广告,当前频率+广告数
	private static boolean bAdShow = false;//广告正在显示
	
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
		
		AdManager.getInstance(this).init("fa1dbd432d37f9f2", "96b8d76780e66416", true);//测试
		SpotManager.getInstance(this).loadSpotAds();//初始化
		//SpotManager.getInstance(this).setSpotOrientation(SpotManager.ORIENTATION_LANDSCAPE);//横屏
		SpotManager.getInstance(this).setSpotOrientation(SpotManager.ORIENTATION_PORTRAIT);//竖屏
		SpotManager.getInstance(this).setAnimationType(SpotManager.ANIM_ADVANCE);
	}
	
	public void showAds() {
		LogUtil.d(DEBUG_TAG,"showAds");
		//LogUtil.e(DEBUG_TAG,"showAds frequency,animCount,adCount,bAdShow");
		//LogUtil.e(DEBUG_TAG,adFrequency+","+animCount+","+adCount+","+bAdShow);
		animCount += 1;
		if (animCount > adFrequency && !bAdShow) {
			bAdShow = true;
			SpotManager.getInstance(this).showSpotAds(this, new SpotDialogListener() {
			    @Override
			    public void onShowSuccess() {
			        LogUtil.d("Youmi", "onShowSuccess");
			    }
			    @Override
			    public void onShowFailed() {
			    	LogUtil.d("Youmi", "onShowFailed");
					bAdShow = false;
			    }
			    @Override
			    public void onSpotClosed() {
			    	LogUtil.d("sdkDemo", "closed");
			    	bAdShow = false;
			    }
			});//显示广告
			animCount = 0;
			adCount += 1;
			adFrequency += adCount;
		}
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
		SpotManager.getInstance(this).onDestroy();
		super.onDestroy();
	}
	
	public void onBackPressed() {
		LogUtil.d(DEBUG_TAG,"onBackPressed");
	    // 如果有需要，可以点击后退关闭插播广告。
	    if (!SpotManager.getInstance(this).disMiss()) {
	        // 弹出退出窗口，可以使用自定义退屏弹出和回退动画,参照demo,若不使用动画，传入-1
	        super.onBackPressed();
	    }
	}

	@Override
	protected void onStop() {
		LogUtil.d(DEBUG_TAG,"onStop");
	    // 如果不调用此方法，则按home键的时候会出现图标无法显示的情况。
	    SpotManager.getInstance(this).onStop();
	    super.onStop();
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

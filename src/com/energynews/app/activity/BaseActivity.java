package com.energynews.app.activity;

import com.energynews.app.util.ActivityCollector;
import com.energynews.app.util.LogUtil;

import android.app.Activity;
import android.os.Bundle;

import com.umeng.analytics.MobclickAgent;

public class BaseActivity extends Activity {

	private final static String DEBUG_TAG = "BaseActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.d(DEBUG_TAG,"onCreate");
		//Log.e("BaseActivity", getClass().getSimpleName());
		ActivityCollector.addActivity(this);
	}
	
	@Override
	protected void onDestroy() {
		LogUtil.d(DEBUG_TAG,"onDestroy");
		super.onDestroy();
		ActivityCollector.removeActivity(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}

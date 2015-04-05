package com.energynews.app.activity;

import com.energynews.app.util.ActivityCollector;
import com.energynews.app.util.LogUtil;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

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

}

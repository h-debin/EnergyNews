package com.energynews.app.activity;

import com.energynews.app.R;
import com.energynews.app.data.NewsManager;
import com.energynews.app.fragment.NewsTitleFragment;
import com.energynews.app.model.News;
import com.energynews.app.service.AutoUpdateService;
import com.energynews.app.util.LogUtil;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class NewsListActivity extends BaseActivity {
	
	private TextView homeTitleTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.news_list);
        homeTitleTextView = (TextView) findViewById(R.id.title_text);
	}
	
	public void changeEmotionTitleText() {
		if (homeTitleTextView != null) {
			String emotion = NewsManager.getInstance(this).getCurrentEmotionType();
			int color = NewsManager.getInstance(this).getCurrentEmotionColor();
			homeTitleTextView.setText("今日" + emotion + "新闻");
			homeTitleTextView.setBackgroundColor(color);
		}
	}
	
	@Override
	protected void onDestroy() {
		//AutoUpdateService.actionStop(this);
		super.onDestroy();
	}
	
	/**
	 * 单击标题栏,更新当前新闻
	 */
	@Override
	protected void singleTapConfirmed() {
		NewsTitleFragment frag = (NewsTitleFragment) getFragmentManager().
				findFragmentById(R.id.news_title_fragment);
		frag.refreshFromServer();
	}
}

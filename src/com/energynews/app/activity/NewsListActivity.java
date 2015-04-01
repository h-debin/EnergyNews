package com.energynews.app.activity;

import com.energynews.app.R;
import com.energynews.app.data.NewsManager;
import com.energynews.app.fragment.NewsTitleFragment;
import com.energynews.app.util.LogUtil;

import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class NewsListActivity extends BaseActivity {
	
	private TextView homeTitleTextView;
	
	private final static String DEBUG_TAG = "NewsListActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LogUtil.d(DEBUG_TAG,"onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.news_list);
        homeTitleTextView = (TextView) findViewById(R.id.title_text);
	}
	
	public void changeEmotionTitleText() {
		LogUtil.d(DEBUG_TAG,"changeEmotionTitleText");
		if (homeTitleTextView != null) {
			String emotion = NewsManager.getInstance(this).getCurrentEmotionType();
			int colorHome = NewsManager.getInstance(this).getCurrentEmotionColor();
			int colorText = NewsManager.getInstance(this).getCurrentEmotionColorText();
			int colorBg = NewsManager.getInstance(this).getCurrentEmotionColorBg();
			homeTitleTextView.setText("今日" + emotion + "新闻");
			homeTitleTextView.setBackgroundColor(colorHome);
			homeTitleTextView.setTextColor(colorText);
			getFragmentManager().findFragmentById(R.id.news_title_fragment)
			.getView().setBackgroundColor(colorBg);
		}
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
		NewsTitleFragment frag = (NewsTitleFragment) getFragmentManager().
				findFragmentById(R.id.news_title_fragment);
		frag.refreshFromServer();
	}
}

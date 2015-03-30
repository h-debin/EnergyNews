package com.energynews.app.activity;

import com.energynews.app.R;
import com.energynews.app.data.NewsManager;
import com.energynews.app.fragment.NewsTitleFragment;
import com.energynews.app.model.News;
import com.energynews.app.service.AutoUpdateService;
import com.energynews.app.util.LogUtil;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.Window;

public class NewsListActivity extends BaseActivity {
	
	private GestureDetectorCompat mDetector; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.news_list);
        mDetector = new GestureDetectorCompat(this, new MyGestureListener(this));
	}
	
	@Override
	protected void onDestroy() {
		AutoUpdateService.actionStop(this);
		super.onDestroy();
	}

    @Override 
    public boolean onTouchEvent(MotionEvent event){ 
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    
    class MyGestureListener extends SimpleOnGestureListener {
        private static final String TAG = "MyGestureListener";
        private Context myContext;
        
        public MyGestureListener(Context context) {
        	myContext = context;
        }
        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, 
                float velocityX, float velocityY) {
        	//x 左>右, y 上>下
        	float xdis = event1.getX() - event2.getX();
        	float ydis = event1.getY() - event2.getY();
        	//LogUtil.e(TAG, "xdis, ydis:" + xdis + "," + ydis);
        	if (Math.abs(xdis) > 15 || Math.abs(ydis) > 15) {
    			NewsTitleFragment newsTitleFragMent = (NewsTitleFragment) 
    					getFragmentManager().findFragmentById(R.id.news_title_fragment);
        		if (Math.abs(xdis) > Math.abs(ydis)) {//横向,更新新闻
        			newsTitleFragMent.changeNews((int)xdis);
        		} else {//竖向,改变情绪
        			newsTitleFragMent.changeEmotion((int)ydis);
        		}
        	}
            return true;
        }
        
        @Override
        public boolean onDoubleTapEvent(MotionEvent event) {
        	//LogUtil.e(TAG, "onDoubleTapEvent");
        	NewsTitleFragment newsTitleFragMent = (NewsTitleFragment) 
					getFragmentManager().findFragmentById(R.id.news_title_fragment);
			newsTitleFragMent.showNewsContent();
            return true;
        }
        
    }
}

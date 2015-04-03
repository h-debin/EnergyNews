package com.energynews.app.activity;

import com.energynews.app.util.ActivityCollector;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

public class BaseActivity extends Activity {
	
	private GestureDetectorCompat mDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Log.e("BaseActivity", getClass().getSimpleName());
		ActivityCollector.addActivity(this);
		mDetector = new GestureDetectorCompat(this, new MyGestureListener(this));
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityCollector.removeActivity(this);
	}
	
	protected void singleTapConfirmed() {
		
	}
	
	protected void onFlingEvent(float xdis, float ydis) {
		
	}
	
	protected void onScrollEvent(float xdis, float ydis) {
		
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
        	onFlingEvent(xdis, ydis);
            return true;
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
        	onScrollEvent(distanceX, distanceY);
            return false;
        }

        @Override//单击刷新新闻
        public boolean onSingleTapConfirmed(MotionEvent event) {
        	singleTapConfirmed();
            return true;
        }
        
        @Override//双击关闭程序
        public boolean onDoubleTapEvent(MotionEvent event) {
        	ActivityCollector.finishAll();
            return true;
        }
    }

}

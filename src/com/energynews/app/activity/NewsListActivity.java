package com.energynews.app.activity;

import net.youmi.android.AdManager;
import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import net.youmi.android.spot.SpotManager;

import com.energynews.app.R;
import com.energynews.app.data.NewsManager;
import com.energynews.app.db.EnergyNewsDB;
import com.energynews.app.model.News;
import com.energynews.app.util.HttpCallbackListener;
import com.energynews.app.util.HttpUtil;
import com.energynews.app.util.LogUtil;
import com.energynews.app.util.Utility;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NewsListActivity extends BaseActivity implements AnimationListener {
	
	private final static String DEBUG_TAG = "NewsListActivity";

	private GestureDetectorCompat mDetector;
	
	private TextView homeTextView;
	private TextView titleTextView;
	private ImageView titleImage;
	private EnergyNewsDB energyNewsDB;
	Animation animTopIn;
	Animation animTopOut;
	Animation animRightIn;
	Animation animRightOut;
	Animation animLeftIn;
	Animation animLeftOut;
	Animation animBottomIn;
	Animation animBottomOut;
	
	private boolean bTopIn = false;//纪录向下滑动,需要将图片从顶部翻进来
	
	private final static int NEED_TOP_IN = 1;//动画中间变小消失后,是否需要上面翻进
	private final static int NEED_BOTTOM_IN = 2;//动画向上翻出后,是否需要中间变大显示
	private final static int TOP_OUTTING = 1;//动画正在向上翻出
	private final static int BOTTOM_OUTTING = 2;//动画正在中间变小消失
	private final static int LEFT_OUTTING = 3;//动画正在从左边移出
	private final static int RIGHT_OUTTING = 4;//动画正在从右边移出
	private static int needAnimIn = 0;
	private static int outting = 0;
	
	
	private int emotionChangeType = 1;
	
	private boolean firstNews = false;//是否已经显示了第一个新闻
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LogUtil.d(DEBUG_TAG,"onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.news_list_relative);
		
		firstNews = false;

     // load the animation
     	animTopIn = AnimationUtils.loadAnimation(this,R.anim.top_in);
     	animTopIn.setAnimationListener(this);
     	animTopOut = AnimationUtils.loadAnimation(this,R.anim.top_out);
     	animTopOut.setAnimationListener(this);
     	animRightIn = AnimationUtils.loadAnimation(this,R.anim.right_in);
     	animRightIn.setAnimationListener(this);
     	animRightOut = AnimationUtils.loadAnimation(this,R.anim.right_out);
     	animRightOut.setAnimationListener(this);
     	animBottomIn = AnimationUtils.loadAnimation(this,R.anim.bottom_in);
     	animBottomIn.setAnimationListener(this);
     	animBottomOut = AnimationUtils.loadAnimation(this,R.anim.bottom_out);
     	animBottomOut.setAnimationListener(this);
     	animLeftIn = AnimationUtils.loadAnimation(this,R.anim.left_in);
     	animLeftIn.setAnimationListener(this);
     	animLeftOut = AnimationUtils.loadAnimation(this,R.anim.left_out);
     	animLeftOut.setAnimationListener(this);
     		
     	homeTextView = (TextView) findViewById(R.id.home_title_text);
     	titleTextView = (TextView) findViewById(R.id.news_title_text);
     	//TextPaint tp = titleTextView.getPaint();
        //tp.setFakeBoldText(true); 
    	titleImage = (ImageView) findViewById(R.id.news_image);
    	//titleImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
    	energyNewsDB = EnergyNewsDB.getInstance(this);
    	int yestoday = Utility.getDays() - 1;
    	energyNewsDB.setOldNews(yestoday);//设置一天之前的新闻为旧新闻
    	queryNewsList(true, false);//加载新闻列表
    	queryFromServer(NewsManager.getInstance(this).getApiAddress());//网上加载新闻

    	mDetector = new GestureDetectorCompat(this, new MyGestureListener());
    		
		AdManager.getInstance(this).init("fa1dbd432d37f9f2", "96b8d76780e66416", false);//测试
		SpotManager.getInstance(this).loadSpotAds();//初始化
		//SpotManager.getInstance(this).setSpotOrientation(SpotManager.ORIENTATION_LANDSCAPE);//横屏
		SpotManager.getInstance(this).setSpotOrientation(SpotManager.ORIENTATION_PORTRAIT);//竖屏
		SpotManager.getInstance(this).setAnimationType(SpotManager.ANIM_ADVANCE);
		// 实例化广告条
		AdView adView = new AdView(this, AdSize.FIT_SCREEN);
		// 获取要嵌入广告条的布局
		LinearLayout adLayout=(LinearLayout)findViewById(R.id.youmi_ad);
		// 将广告条加入到布局中
		adLayout.addView(adView);
		
	}
	
	public void changeNews(int changeType) {
		LogUtil.d(DEBUG_TAG,"changeNews");
		NewsManager.getInstance(this).changeNews(changeType);
		//changeType > 0 is right in, else right out
		titleImage.setVisibility(View.VISIBLE);
		if (changeType > 0) {
			titleImage.startAnimation(animLeftOut);
		} else {
			titleImage.startAnimation(animRightOut);
		}
		if (NewsManager.getInstance(this).checkRefresh()) {
			refreshFromServer();//刷新
			NewsManager.getInstance(this).resetScanCount();//重新初始化浏览总数
		}
		//LogUtil.d("ss", "changeNews id:" + android.os.Process.myTid());
	}

	public void changeEmotion(int changeType) {
		LogUtil.d(DEBUG_TAG,"changeEmotion...bTopIn,needAnimIn,outting");
		LogUtil.d(DEBUG_TAG,bTopIn + "," + needAnimIn + "," + outting);
		bTopIn = (changeType <= 0);//top in
		if (emotionChangeType != changeType) {//说明是手动上下滑动触发事件
			titleImage.setVisibility(View.VISIBLE);
			if (changeType > 0) {//top out
				if (outting != TOP_OUTTING) {
					if (outting == BOTTOM_OUTTING) {//说明用户先向下滑动,后又向上滑动,那就直接从下进
						outting = 0;
						titleImage.startAnimation(animBottomIn);
					} else {
						outting = TOP_OUTTING;//避免因为延迟启动动画而导致变量没有得到更新
						titleImage.startAnimation(animTopOut);
					}
				}
			} else if (outting != BOTTOM_OUTTING){
				if (outting == TOP_OUTTING) {//说明用户先向上滑动,后又向下滑动,那就直接从上进
					outting = 0;
					titleImage.startAnimation(animTopIn);
				} else {
					outting = BOTTOM_OUTTING;//避免因为延迟启动动画而导致变量没有得到更新
					titleImage.startAnimation(animBottomOut);
				}
			}
		}
		emotionChangeType = changeType;
		NewsManager.getInstance(this).changeEmotion(changeType);
		boolean exits = queryNewsList(true, false);
		if (!exits) {//该情绪没有对应的新闻
			boolean isRequested = NewsManager.getInstance(this).isCurrentEmotionRequested();
			if (!isRequested) {//没有从服务器上查询过
				queryFromServer(NewsManager.getInstance(this).getApiAddress());//网上加载新闻
			} else {
				changeEmotion(emotionChangeType);//改变情绪继续查找
			}
		}
	}
	
	public void showNewsContent() {
		LogUtil.d(DEBUG_TAG,"showNewsContent");
		News news = NewsManager.getInstance(this).getCurrentNews();
		if (news != null) {
			NewsContentActivity.actionStart(this, news.getLink());
		}
	}
	
	/**
	 * 显示新闻标题和图片
	 * @param showLastNews 是否显示上一个新闻
	 */
	private void showNewsTitle(boolean showLastNews) {
		LogUtil.d(DEBUG_TAG,"showNewsTitle");
		News news = null;
		if (showLastNews) {
			news = NewsManager.getInstance(this).getLastNews();
		} else {
			news = NewsManager.getInstance(this).getCurrentNews();
		}
		if (news != null) {
			homeTextView.setText(news.getEmotion());
			titleTextView.setText(news.getTitle());
			String imgUrl = news.getPicture();
			if (!"null".equals(imgUrl) && !TextUtils.isEmpty(imgUrl) && imgUrl.contains("http")) {
				UrlImageViewHelper.setUrlDrawable(titleImage, imgUrl);
			}
			firstNews = true;
		}
	}
	
	/**
	 * 加锁,设置动画显示类型.
	 */
	private synchronized void setAnimType() {
		LogUtil.d(DEBUG_TAG,"setAnimType...bTopIn,needAnimIn,outting");
		LogUtil.d(DEBUG_TAG,bTopIn + "," + needAnimIn + "," + outting);
		titleImage.setVisibility(View.VISIBLE);
		if (bTopIn) {//需要从上面翻进
			if (outting == BOTTOM_OUTTING) {//判断是否正在进行中间变小消失动画
				needAnimIn = NEED_TOP_IN;;//纪录该值,让中间变小消失动画结束时再进行从上翻进的动画
			} else {
				titleImage.startAnimation(animTopIn);//直接进行从上翻进的动画
			}
		} else {//从中间变大显示
			if (outting == TOP_OUTTING) {//判断是否正在进行从上翻出动画
				needAnimIn = NEED_BOTTOM_IN;//纪录,等从上翻出动画结束后,进行从中间变大显示动画
			} else {
				titleImage.startAnimation(animBottomIn);//直接进行从中间变大显示动画
			}
		}
	}
	
	/**
	 * 查询数据库中的数据,如果存在数据,则进行显示
	 * @param saveData 是否需要保存查到的数据
	 */
	private boolean queryNewsList(boolean saveData, boolean newData) {
		LogUtil.d(DEBUG_TAG,"queryNewsList");
		boolean exists = NewsManager.getInstance(this).queryNewsList(saveData, newData);
		if (exists) {
			setAnimType();//设置动画显示
			return true;
		}
		return false;
	}
	
	/**
	 * 向服务器请求成功
	 * @param newData 是否有新数据更新
	 */
	public void requestSuccess(boolean newData) {
		LogUtil.d(DEBUG_TAG,"requestSuccess");
		boolean exists = NewsManager.getInstance(this).queryNewsList(false, false);
		if (exists) {
			NewsManager.getInstance(this).setCurrentEmotionRequested();//纪录当前情绪已经被查找过
			if (newData) {
				queryNewsList(true, true);//有数据更新,则重新从数据库中获取数据,并保存获得的数据
			}
		} else {
			changeEmotion(emotionChangeType);//改变情绪继续查找
		}
	}
	
	public void refreshFromServer() {
		LogUtil.e(DEBUG_TAG,"refreshFromServer");
		String address = NewsManager.getInstance(this).getApiAddress();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				boolean savedSuccess = Utility.handleEnergyNewsResponse(energyNewsDB, response);
				if (savedSuccess) {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							requestSuccess(true);
						}
					});
				} else {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							requestSuccess(false);
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				// 通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(NewsListActivity.this,"加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	private void queryFromServer(String address) {
		LogUtil.d(DEBUG_TAG,"queryFromServer");
		//showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				boolean savedSuccess = Utility.handleEnergyNewsResponse(energyNewsDB, response);
				if (savedSuccess) {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//closeProgressDialog();
							requestSuccess(true);//如果有新的数据,则刷新页面
						}
					});
				} else {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//closeProgressDialog();
							requestSuccess(false);
							//Toast.makeText(this,"无数据加载", Toast.LENGTH_SHORT).show();
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				// 通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						//closeProgressDialog();
						Toast.makeText(NewsListActivity.this,"加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	@Override
	public void onAnimationEnd(Animation animation) {
		LogUtil.d(DEBUG_TAG,"onAnimationEnd");
		outting = 0;
		// Take any action after completing the animation
		if (animation == animRightOut) {//右滑动改变新闻
			titleImage.startAnimation(animLeftIn);
		}
		if (animation == animLeftOut) {//右滑动改变新闻
			titleImage.startAnimation(animRightIn);
		}
		if (animation == animTopOut) {//右滑动改变新闻
			if (needAnimIn == NEED_BOTTOM_IN) {
				titleImage.startAnimation(animBottomIn);
			}
		}
		if (animation == animBottomOut) {//右滑动改变新闻
			if (needAnimIn == NEED_TOP_IN) {
				titleImage.startAnimation(animTopIn);
			}
		}
		//LogUtil.d("onAnimationEnd", "onAnimationEnd......");
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		LogUtil.d(DEBUG_TAG, "onAnimationRepeat......");
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		LogUtil.d(DEBUG_TAG, "onAnimationStart......");
		if (animation == animRightIn) {//左滑动改变新闻
			showNewsTitle(false);//显示新闻
		}
		if (animation == animRightOut) {
			if (outting == LEFT_OUTTING) {//  如果正在左出,则直接左进
				titleImage.startAnimation(animLeftIn);
			} else if (outting == RIGHT_OUTTING) {//如果正在右出,则改变图像继续右出
				showNewsTitle(true);//显示上一个新闻
			}
			outting = RIGHT_OUTTING;
		}
		if (animation == animLeftIn) {//左滑动改变新闻
			showNewsTitle(false);//显示新闻
		}
		if (animation == animLeftOut) {
			if (outting == LEFT_OUTTING) {//如果正在左出,则改变图片继续左出
				showNewsTitle(true);//显示上一个新闻
			} else if (outting == RIGHT_OUTTING) {
				titleImage.startAnimation(animRightIn);
			}
			outting = LEFT_OUTTING;
		}
		if (animation == animTopIn) {
			bTopIn = false;//上下滑动时,启动动画的时候重新设置该值为false
			needAnimIn = 0;
			showNewsTitle(false);//显示新闻
		}
		if (animation == animTopOut) {
			outting =TOP_OUTTING;
		}
		if (animation == animBottomIn) {
			needAnimIn = 0;
			showNewsTitle(false);//显示新闻
		}
		if (animation == animBottomOut) {//右滑动改变新闻
			outting =BOTTOM_OUTTING;
		}
	}
	
	@Override
	protected void onRestart() {
		LogUtil.d(DEBUG_TAG,"onRestart");
		//AutoUpdateService.actionStop(this);
		super.onRestart();
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
	
	@Override 
    public boolean onTouchEvent(MotionEvent event){ 
		LogUtil.d(DEBUG_TAG,"onTouchEvent");
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
	
	class MyGestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, 
                float velocityX, float velocityY) {
    		LogUtil.d(DEBUG_TAG,"onFling");
    		if (!firstNews) {
    			return true;
    		}
        	//x 左>右, y 上>下
    		float xdis = event2.getX() - event1.getX();
    		float ydis = event2.getY() - event1.getY();
    		if (Math.abs(xdis) > 15 ||Math.abs(ydis) > 15) {
    			if (Math.abs(xdis) > Math.abs(ydis)) {
    				changeNews(-(int)xdis);
    			} else {
    				changeEmotion(-(int)ydis);
    			}
    		}
            return true;
        }

        @Override//单击刷新新闻
        public boolean onSingleTapConfirmed(MotionEvent event) {
    		LogUtil.d(DEBUG_TAG,"onSingleTapConfirmed");
    		if (!firstNews) {
    			return true;
    		}
    		showNewsContent();
            return true;
        }
    }
	
}

package com.energynews.app.fragment;

import android.app.Fragment;
import android.app.Activity;
import android.app.ProgressDialog;
import android.support.v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.view.View;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.energynews.app.R;
import com.energynews.app.activity.NewsContentActivity;
import com.energynews.app.activity.NewsListActivity;
import com.energynews.app.data.NewsManager;
import com.energynews.app.db.EnergyNewsDB;
import com.energynews.app.model.News;
import com.energynews.app.util.HttpCallbackListener;
import com.energynews.app.util.HttpUtil;
import com.energynews.app.util.LogUtil;
import com.energynews.app.util.Utility;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class NewsTitleFragment extends Fragment implements AnimationListener {

	private String DEBUG_TAG = "NewsTitleFragment";
	
	private TextView titleTextView;
	private ImageView titleImage;
	private boolean isTwoPane;
	private EnergyNewsDB energyNewsDB;
	private ProgressDialog progressDialog;
	private NewsListActivity myActivity;
	
	Animation animTopIn;
	Animation animTopOut;
	Animation animRightIn;
	Animation animRightOut;
	Animation animLeftIn;
	Animation animLeftOut;
	Animation animCenterIn;
	Animation animCenterOut;
	
	private boolean bTopIn = false;//纪录向下滑动,需要将图片从顶部翻进来
	
	private final static int NEED_TOP_IN = 1;//动画中间变小消失后,是否需要上面翻进
	private final static int NEED_CENTER_IN = 2;//动画向上翻出后,是否需要中间变大显示
	private final static int TOP_OUTTING = 1;//动画正在向上翻出
	private final static int CENTER_OUTTING = 2;//动画正在中间变小消失
	private static int needAnimIn = 0;
	private static int outting = 0;
	
	
	private int emotionChangeType = 1;
	
	@Override
	public void onAttach(Activity activity) {
		LogUtil.d(DEBUG_TAG,"onAttach");
		super.onAttach(activity);
		myActivity = (NewsListActivity) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		LogUtil.d(DEBUG_TAG,"onCreateView");
		// load the animation
		animTopIn = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),R.anim.top_in);
		animTopIn.setAnimationListener(this);
		animTopOut = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),R.anim.top_out);
		animTopOut.setAnimationListener(this);
		animRightIn = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),R.anim.right_in);
		animRightIn.setAnimationListener(this);
		animRightOut = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),R.anim.right_out);
		animRightOut.setAnimationListener(this);
		animCenterIn = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),R.anim.center_in);
		animCenterIn.setAnimationListener(this);
		animCenterOut = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),R.anim.center_out);
		animCenterOut.setAnimationListener(this);
		animLeftIn = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),R.anim.left_in);
		animLeftIn.setAnimationListener(this);
		animLeftOut = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),R.anim.left_out);
		animLeftOut.setAnimationListener(this);
		
		View view = inflater.inflate(R.layout.news_item_frame, container, false);
		titleTextView = (TextView) view.findViewById(R.id.news_title_text);
		titleImage = (ImageView) view.findViewById(R.id.news_image);
		titleImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
		energyNewsDB = EnergyNewsDB.getInstance(getActivity());
		int yestoday = Utility.getDays() - 1;
		energyNewsDB.deleteOldNews(yestoday);//删除一天之前的旧新闻
		queryNewsList(true);//加载新闻列表
		queryFromServer(NewsManager.getInstance(getActivity()).getApiAddress());//网上加载新闻
		//AutoUpdateService.actionStart(getActivity());//启动自动更新数据库服务,立即执行从网上加载数据
		view.setOnTouchListener(new OnTouchListener() {
			private float xdown = 0;
			private float ydown = 0;
		    public boolean onTouch(View v, MotionEvent event) {
		    	// ... Respond to touch events
		    	int action = MotionEventCompat.getActionMasked(event);
		    	switch(action) {
		    	case (MotionEvent.ACTION_DOWN): {
		        	//Log.e(TAG,"ACTION_DOWN");
		        	xdown = event.getX();
		        	ydown = event.getY();
		            break;
		        }
		        case (MotionEvent.ACTION_UP): {
		        	//Log.e(TAG,"ACTION_UP");
		        	float xdis = event.getX() - xdown;//x左<右,y上<下
		        	float ydis = event.getY() - ydown;
		        	float tdis = event.getEventTime() - event.getDownTime();//时间间隔
		        	if (Math.abs(xdis) > 15 || Math.abs(ydis) > 15) {//当做滑动
		        		if (Math.abs(xdis) > Math.abs(ydis)) {//横向,更新新闻
		        			changeNews(-(int)xdis);
		        		} else {//竖向,改变情绪
		        			changeEmotion(-(int)ydis);
		        			//LogUtil.d("MyGestureListener", "onFling.....changeEmotion");
		        		}
		        	} else if (tdis < 1000 && Math.abs(xdis) < 5 && Math.abs(ydis) <5) {//当做点击
		        		showNewsContent();
		        	}
		        	break;
		        }     
		        default: 
		        }
		        return true;
		    }
		});
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		LogUtil.d(DEBUG_TAG,"onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		if (false) {
		//if (getActivity().findViewById(R.id.news_content_layout) != null) {
			isTwoPane = true;
		} else {
			isTwoPane = false;
		}
	}
	
	public void changeNews(int changeType) {
		LogUtil.d(DEBUG_TAG,"changeNews");
		NewsManager.getInstance(getActivity()).changeNews(changeType);
		//changeType > 0 is right in, else right out
		titleImage.setVisibility(View.VISIBLE);
		if (changeType > 0) {
			titleImage.startAnimation(animLeftOut);
		} else {
			titleImage.startAnimation(animRightOut);
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
					outting = TOP_OUTTING;//避免因为延迟启动动画而导致变量没有得到更新
					titleImage.startAnimation(animTopOut);
				}
			} else if (outting != CENTER_OUTTING){
				outting = CENTER_OUTTING;//避免因为延迟启动动画而导致变量没有得到更新
				titleImage.startAnimation(animCenterOut);
			}
		}
		emotionChangeType = changeType;
		NewsManager.getInstance(getActivity()).changeEmotion(changeType);
		boolean exits = queryNewsList(true);
		if (!exits) {//该情绪没有对应的新闻
			boolean isRequested = NewsManager.getInstance(getActivity()).isCurrentEmotionRequested();
			if (!isRequested) {//没有从服务器上查询过
				queryFromServer(NewsManager.getInstance(getActivity()).getApiAddress());//网上加载新闻
			} else {
				changeEmotion(emotionChangeType);//改变情绪继续查找
			}
		}
	}
	
	public void showNewsContent() {
		LogUtil.d(DEBUG_TAG,"showNewsContent");
		News news = NewsManager.getInstance(getActivity()).getCurrentNews();
		if (news != null) {
			NewsContentActivity.actionStart(getActivity(), news.getLink());
		}
	}
	
	private void showNewsTitle() {
		LogUtil.d(DEBUG_TAG,"showNewsTitle");
		News news = NewsManager.getInstance(getActivity()).getCurrentNews();
		if (news != null) {
			myActivity.changeEmotionTitleText();//改变情绪标题
			titleTextView.setText(news.getTitle());
			String imgUrl = news.getPicture();
			if ("null".equals(imgUrl) || TextUtils.isEmpty(imgUrl) || !imgUrl.contains("http")) {
				imgUrl="http://h.hiphotos.baidu.com/image/pic/item/b151f8198618367a0d517ec22c738bd4b21ce5d1.jpg";
			}
			UrlImageViewHelper.setUrlDrawable(titleImage, imgUrl);
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
			if (outting == CENTER_OUTTING) {//判断是否正在进行中间变小消失动画
				needAnimIn = NEED_TOP_IN;;//纪录该值,让中间变小消失动画结束时再进行从上翻进的动画
			} else {
				titleImage.startAnimation(animTopIn);//直接进行从上翻进的动画
			}
		} else {//从中间变大显示
			if (outting == TOP_OUTTING) {//判断是否正在进行从上翻出动画
				needAnimIn = NEED_CENTER_IN;//纪录,等从上翻出动画结束后,进行从中间变大显示动画
			} else {
				titleImage.startAnimation(animCenterIn);//直接进行从中间变大显示动画
			}
		}
	}
	
	/**
	 * 查询数据库中的数据,如果存在数据,则进行显示
	 * @param saveData 是否需要保存查到的数据
	 */
	private boolean queryNewsList(boolean saveData) {
		LogUtil.d(DEBUG_TAG,"queryNewsList");
		boolean exists = NewsManager.getInstance(getActivity()).queryNewsList(saveData);
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
		NewsManager.getInstance(getActivity()).setCurrentEmotionRequested();//纪录当前情绪已经被查找过
		if (newData) {
			queryNewsList(true);//有数据更新,则重新从数据库中获取数据,并保存获得的数据
		} else {//没有数据更新,则查询当前情绪没有没对应的数据
			boolean exists = NewsManager.getInstance(getActivity()).queryNewsList(false);
			if (!exists) {//没有数据
				changeEmotion(emotionChangeType);//改变情绪继续查找
			}
		}
	}
	
	public void refreshFromServer() {
		LogUtil.d(DEBUG_TAG,"refreshFromServer");
		if (getActivity() == null) return;
		String address = NewsManager.getInstance(getActivity()).getApiAddress();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				boolean savedSuccess = Utility.handleEnergyNewsResponse(energyNewsDB, response);
				if (savedSuccess) {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							requestSuccess(true);
						}
					});
				} else {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					getActivity().runOnUiThread(new Runnable() {
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
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity(),"加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	private void queryFromServer(String address) {
		LogUtil.d(DEBUG_TAG,"queryFromServer");
		if (getActivity() == null) return;
		//showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				boolean savedSuccess = Utility.handleEnergyNewsResponse(energyNewsDB, response);
				if (savedSuccess) {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//closeProgressDialog();
							requestSuccess(true);//如果有新的数据,则刷新页面
						}
					});
				} else {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//closeProgressDialog();
							requestSuccess(false);
							//Toast.makeText(getActivity(),"无数据加载", Toast.LENGTH_SHORT).show();
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				// 通过runOnUiThread()方法回到主线程处理逻辑
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						//closeProgressDialog();
						Toast.makeText(getActivity(),"加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		LogUtil.d(DEBUG_TAG,"showProgressDialog");
		if (getActivity() == null) return;
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/**
	 * 关闭进度对话框
	 */
	private void closeProgressDialog() {
		LogUtil.d(DEBUG_TAG,"closeProgressDialog");
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	@Override
	public void onAnimationEnd(Animation animation) {
		LogUtil.d(DEBUG_TAG,"onAnimationEnd");
		// Take any action after completing the animation
		if (animation == animRightOut) {//右滑动改变新闻
			titleImage.startAnimation(animLeftIn);
		}
		if (animation == animLeftOut) {//右滑动改变新闻
			titleImage.startAnimation(animRightIn);
		}
		if (animation == animTopOut) {//右滑动改变新闻
			outting = 0;
			if (needAnimIn == NEED_CENTER_IN) {
				titleImage.startAnimation(animCenterIn);
			}
		}
		if (animation == animCenterOut) {//右滑动改变新闻
			outting = 0;
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
			showNewsTitle();//显示新闻
		}
		if (animation == animLeftIn) {//左滑动改变新闻
			showNewsTitle();//显示新闻
		}
		if (animation == animTopIn) {
			bTopIn = false;//上下滑动时,启动动画的时候重新设置该值为false
			needAnimIn = 0;
			showNewsTitle();//显示新闻
		}
		if (animation == animTopOut) {
			outting =TOP_OUTTING;
		}
		if (animation == animCenterIn) {
			needAnimIn = 0;
			showNewsTitle();//显示新闻
		}
		if (animation == animCenterOut) {//右滑动改变新闻
			outting =CENTER_OUTTING;
		}
	}

}

package com.energynews.app.fragment;

import android.app.Fragment;
import android.app.Activity;
import android.app.ProgressDialog;
import android.support.v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import com.energynews.app.R;
import com.energynews.app.activity.NewsContentActivity;
import com.energynews.app.activity.NewsListActivity;
import com.energynews.app.data.NewsManager;
import com.energynews.app.db.EnergyNewsDB;
import com.energynews.app.layout.TitleLayout;
import com.energynews.app.model.News;
import com.energynews.app.service.AutoUpdateService;
import com.energynews.app.util.HttpCallbackListener;
import com.energynews.app.util.HttpUtil;
import com.energynews.app.util.LogUtil;
import com.energynews.app.util.Utility;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class NewsTitleFragment extends Fragment {
	
	private TextView titleTextView;
	private ImageView titleImage;
	private boolean isTwoPane;
	private EnergyNewsDB energyNewsDB;
	private ProgressDialog progressDialog;
	private NewsListActivity myActivity;
	
	private String TAG = "NewsTitleFragment";
	
	private int emotionChangeType = 1;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		myActivity = (NewsListActivity) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.news_item_frame, container, false);
		titleImage = (ImageView) view.findViewById(R.id.news_image);
		titleTextView = (TextView) view.findViewById(R.id.news_title_text);
		titleImage.setScaleType(ImageView.ScaleType.FIT_XY);
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
		        			//LogUtil.e("MyGestureListener", "onFling.....changeEmotion");
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
		super.onActivityCreated(savedInstanceState);
		if (false) {
		//if (getActivity().findViewById(R.id.news_content_layout) != null) {
			isTwoPane = true;
		} else {
			isTwoPane = false;
		}
	}
	
	public void changeNews(int changeType) {
		NewsManager.getInstance(getActivity()).changeNews(changeType);
		showNewsTitle();
	}

	public void changeEmotion(int changeType) {
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
		News news = NewsManager.getInstance(getActivity()).getCurrentNews();
		if (news != null) {
			NewsContentActivity.actionStart(getActivity(), news.getLink());
		}
	}
	
	private void showNewsTitle() {
		News news = NewsManager.getInstance(getActivity()).getCurrentNews();
		if (news != null) {
			myActivity.changeEmotionTitleText();//改变情绪标题
			titleTextView.setText(news.getTitle());
			String imgUrl = news.getPicture();
			if (!"null".equals(imgUrl) && !TextUtils.isEmpty(imgUrl) && imgUrl.contains("http")) {
				UrlImageViewHelper.setUrlDrawable(titleImage, imgUrl);
			} else {
				//viewHolder.newsTitleText.setVisibility(View.GONE);
				imgUrl="http://h.hiphotos.baidu.com/image/pic/item/b151f8198618367a0d517ec22c738bd4b21ce5d1.jpg";
				UrlImageViewHelper.setUrlDrawable(titleImage, imgUrl);
			}
			//LogUtil.e("showNewsTitle","URL:"+imgUrl);
		}
	}
	
	/**
	 * 查询数据库中的数据,如果存在数据,则进行显示
	 * @param saveData 是否需要保存查到的数据
	 */
	private boolean queryNewsList(boolean saveData) {
		boolean exists = NewsManager.getInstance(getActivity()).queryNewsList(saveData);
		if (exists) {
			showNewsTitle();
			return true;
		}
		return false;
	}
	
	/**
	 * 向服务器请求成功
	 * @param newData 是否有新数据更新
	 */
	public void requestSuccess(boolean newData) {
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
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

}

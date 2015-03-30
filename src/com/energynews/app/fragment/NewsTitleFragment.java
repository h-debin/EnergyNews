package com.energynews.app.fragment;

import android.app.Fragment;
import android.app.Activity;
import android.app.ProgressDialog;
import android.text.TextUtils;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import com.energynews.app.R;
import com.energynews.app.activity.NewsContentActivity;
import com.energynews.app.data.NewsManager;
import com.energynews.app.db.EnergyNewsDB;
import com.energynews.app.model.News;
import com.energynews.app.service.AutoUpdateService;
import com.energynews.app.util.HttpCallbackListener;
import com.energynews.app.util.HttpUtil;
import com.energynews.app.util.LogUtil;
import com.energynews.app.util.Utility;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class NewsTitleFragment extends Fragment {
	
	private TextView titleText;
	private ImageView titleImage;
	private boolean isTwoPane;
	private EnergyNewsDB energyNewsDB;
	private ProgressDialog progressDialog;
	
	private int emotionChangeType = 1;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.news_item, container, false);
		titleText = (TextView) view.findViewById(R.id.news_title_text);
		titleImage = (ImageView) view.findViewById(R.id.news_image);
		energyNewsDB = EnergyNewsDB.getInstance(getActivity());
		AutoUpdateService.actionStart(getActivity());//启动自动更新数据库服务
		queryNewsList();//加载新闻列表
		refreshFromServer();//刷新
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
		String emotion = NewsManager.getInstance(getActivity()).changeEmotion(changeType);
		TextView homeTitle = (TextView) getActivity().findViewById(R.id.title_text);
		homeTitle.setText("今日" + emotion + "闻");
		queryNewsList();
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
			titleText.setText(news.getTitle());
			String imgUrl = news.getPicture();
			if (!"null".equals(imgUrl) && !TextUtils.isEmpty(imgUrl)) {
				UrlImageViewHelper.setUrlDrawable(titleImage, imgUrl);
			} else {
				//viewHolder.newsTitleText.setVisibility(View.GONE);
				imgUrl="http://h.hiphotos.baidu.com/image/pic/item/b151f8198618367a0d517ec22c738bd4b21ce5d1.jpg";
				UrlImageViewHelper.setUrlDrawable(titleImage, imgUrl);
			}
		}
	}
	
	private void queryNewsList() {
		List<News> newslistLoad = energyNewsDB.queryNewsByEmotionType(
				NewsManager.getInstance(getActivity()).getCurrentEmotionType());
		if (newslistLoad.size() > 0) {
			NewsManager.getInstance(getActivity()).resetNewsList();
			for (News news : newslistLoad) {
				NewsManager.getInstance(getActivity()).addToNewsList(news);
			}
			showNewsTitle();
		} else {
			queryFromServer(NewsManager.getInstance(getActivity()).getApiAddress());
		}
	}
	
	/**
	 * 无数据加载的时候
	 */
	public void noDataSave() {
		List<News> newslistLoad = energyNewsDB.queryNewsByEmotionType(
				NewsManager.getInstance(getActivity()).getCurrentEmotionType());
		if (newslistLoad.size() <= 0) {
			changeEmotion(emotionChangeType);//用下一种情绪继续查找
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
							queryNewsList();
						}
					});
				} else {
					noDataSave();
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
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				boolean savedSuccess = Utility.handleEnergyNewsResponse(energyNewsDB, response);
				if (savedSuccess) {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							queryNewsList();//如果有新的数据,则刷新页面
						}
					});
				} else {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							noDataSave();
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
						closeProgressDialog();
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

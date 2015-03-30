package com.energynews.app.fragment;

import android.app.Fragment;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.List;
import java.util.ArrayList;

import com.energynews.app.R;
import com.energynews.app.activity.NewsContentActivity;
import com.energynews.app.activity.NewsListActivity;
import com.energynews.app.adapter.NewsAdapter;
import com.energynews.app.db.EnergyNewsDB;
import com.energynews.app.model.News;
import com.energynews.app.service.AutoUpdateService;
import com.energynews.app.util.HttpCallbackListener;
import com.energynews.app.util.HttpUtil;
import com.energynews.app.util.LogUtil;
import com.energynews.app.util.Utility;

public class NewsTitleFragment extends Fragment implements OnItemClickListener {

	public static final String API_ADRESS_PRE = "http://api.minghe.me/api/v1/news?emotion_type=";
	public static final String EMOTION_TYPE__HAO = "好";
	public static final String EMOTION_TYPE__LE = "乐";
	public static final String EMOTION_TYPE__NU = "怒";
	public static final String EMOTION_TYPE__AI = "哀";
	public static final String EMOTION_TYPE__JU = "惧";
	public static final String EMOTION_TYPE__E = "恶";
	public static final String EMOTION_TYPE__JING = "惊";
	
	private String currentEmotionType;
	
	private ListView newsTitleListView;
	private List<News> newsList = new ArrayList<News>();
	private boolean isTwoPane;
	private EnergyNewsDB energyNewsDB;
	private NewsAdapter adapter;
	private ProgressDialog progressDialog;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		adapter = new NewsAdapter(activity, R.layout.news_item, newsList);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		currentEmotionType = EMOTION_TYPE__HAO; 
		View view = inflater.inflate(R.layout.news_title_frag, container, false);
		energyNewsDB = EnergyNewsDB.getInstance(getActivity());
		newsTitleListView = (ListView) view.findViewById(R.id.news_title_list_view);
		newsTitleListView.setAdapter(adapter);
		newsTitleListView.setOnItemClickListener(this);
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
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		News news = newsList.get(position);
		if (isTwoPane) {
			//NewsContentFragment newsContentFragment = (NewsContentFragment)
			//		getFragmentManager().findFragmentById(R.id.news_content_fragment);
			//newsContentFragment.refresh(news.getTitle(), news.getContent());
		} else {
			NewsContentActivity.actionStart(getActivity(), news.getLink());
		}
	}
	
	private void queryNewsList() {
		List<News> newslistLoad = energyNewsDB.queryNewsByEmotionType(currentEmotionType);
		if (newslistLoad.size() > 0) {
			newsList.clear();
			for (News news : newslistLoad) {
				newsList.add(news);
			}
			adapter.notifyDataSetChanged();
			newsTitleListView.setSelection(0);
		} else {
			//String address = "http://api.minghe.me/api/v1/news?emotion_type=%E5%A5%BD";
			queryFromServer(API_ADRESS_PRE + currentEmotionType);
			//queryFromServer(address);
		}
	}
	
	public void refreshFromServer() {
		if (getActivity() == null) return;
		String etype = "";
		try {
			etype = URLEncoder.encode(currentEmotionType, "UTF-8");
		} catch (Exception e) {
			return;
		}
		String address = API_ADRESS_PRE + etype;
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
							Toast.makeText(getActivity(),"无数据加载", Toast.LENGTH_SHORT).show();
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

package com.energynews.app.data;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.energynews.app.model.News;

import android.content.Context;

public class NewsManager {
	
	public static final String API_ADRESS_PRE = "http://api.minghe.me/api/v1/news?emotion_type=";
	public static final String[] EMOTION_TYPE = {"好","乐","怒","哀","惧","恶","惊"};
	
	private int currentEmotionTypeId;
	private int currentNewsId;//当前新闻在newsList中的索引

	private List<News> newsList = new ArrayList<News>();
	
	private static NewsManager mNewsManager = null;
	
	/**
	 * 将构造方法私有化
	 */
	private NewsManager(Context context) {
		setCurrentEmotionType(0);
		setCurrentNewsId(-1);
	}
	
	/**
	 * 获取EnergyNewsDB的实例。
	 */
	public synchronized static NewsManager getInstance(Context context) {
		if (mNewsManager == null) {
			mNewsManager = new NewsManager(context);
		}
		return mNewsManager;
	}
	
	public List<News> getNewsList() {
		return newsList;
	}
	public void resetNewsList() {
		newsList.clear();
		setCurrentNewsId(-1);
	}
	public void addToNewsList(News news) {
		newsList.add(news);
	}
	private void setCurrentEmotionType(int emotionTypeId) {
		currentEmotionTypeId = emotionTypeId;
	}
	private int setCurrentEmotionTypeId() {
		return currentEmotionTypeId;
	}
	public String getCurrentEmotionType() {
		return EMOTION_TYPE[currentEmotionTypeId];
	}
	private void setCurrentNewsId(int newsId) {
		currentNewsId = newsId;
	}
	public News getCurrentNews() {
		if (currentNewsId < 0) {
			if (newsList.size() <= 0) {
				return null;
			}
			setCurrentNewsId(0);
		}
		return newsList.get(currentNewsId);
	}
	public String getApiAddress() {
		String address = "";
		try {
			address = URLEncoder.encode(getCurrentEmotionType(), "UTF-8");
		} catch (Exception e) {
			return address;
		}
		address = API_ADRESS_PRE + address;
		return address;
	}
	/**
	 * 转变情绪类型
	 * @param changeType >=0 则+1, <0则-1
	 */
	public String changeEmotion(int changeType) {
		int len = EMOTION_TYPE.length;
		int emotionId = setCurrentEmotionTypeId();
		if (changeType >= 0) {
			emotionId = (1 + emotionId) % len;
		} else {
			emotionId = (emotionId - 1 + len) % len;
		}
		setCurrentEmotionType(emotionId);
		return getCurrentEmotionType();
	}
	/**
	 * 转变当前新闻
	 * @param changeType >=0 则+1, <0则-1
	 */
	public News changeNews(int changeType) {
		int len = newsList.size();
		if (changeType >= 0) {
			currentNewsId = (1 + currentNewsId) % len;
		} else {
			currentNewsId = (currentNewsId - 1 + len) % len;
		}
		return getCurrentNews();
	}

}

package com.energynews.app.data;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Color;

import com.energynews.app.db.EnergyNewsDB;
import com.energynews.app.model.News;
import com.energynews.app.util.LogUtil;

import android.content.Context;

public class NewsManager {
	
	public static final String API_ADRESS_PRE = "http://api.minghe.me/api/v1/news?emotion_type=";
	public static final String[] EMOTION_TYPE = {"好","乐","怒","哀","惧","恶","惊"};
	private static final int startColor = Color.RED;//当前情绪的标题颜色start + n*colorDis
	private static final int colorDis = 1000;
	
	private static boolean[] isEmotionRequested = {false,false,false,false,false,false,false};
	
	private int currentEmotionTypeId;
	private int currentNewsId;//当前新闻在newsList中的索引

	private List<News> newsList = new ArrayList<News>();
	
	private static NewsManager mNewsManager = null;
	private static EnergyNewsDB db;
	
	/**
	 * 将构造方法私有化
	 */
	private NewsManager(Context context) {
		setCurrentEmotionTypeId(0);
		setCurrentNewsId(-1);
		db = EnergyNewsDB.getInstance(context);
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
	private void setCurrentEmotionTypeId(int emotionTypeId) {
		currentEmotionTypeId = emotionTypeId;
	}
	private int getCurrentEmotionTypeId() {
		return currentEmotionTypeId;
	}
	public String getCurrentEmotionType() {
		return EMOTION_TYPE[currentEmotionTypeId];
	}
	public int getCurrentEmotionColor() {
		return startColor + colorDis * currentEmotionTypeId;
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
		int emotionId = getCurrentEmotionTypeId();
		if (changeType >= 0) {
			emotionId = (1 + emotionId) % len;
		} else {
			emotionId = (emotionId - 1 + len) % len;
		}
		setCurrentEmotionTypeId(emotionId);
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

	/**
	 * 查询当前类型的数据是否存在
	 * @param saveList 是否需要保存新闻列表
	 * @return 是否存在数据
	 */
	public boolean queryNewsList(boolean saveList) {
		List<News> newslistLoad = db.queryNewsByEmotionType(getCurrentEmotionType());
		if (newslistLoad.size() <= 0) {
			return false;
		}
		if (saveList) {
			resetNewsList();
			for (News news : newslistLoad) {
				addToNewsList(news);
			}
		}
		return true;
	}
	
	/**
	 * 判断该情绪是否从服务器上更新过
	 * @return
	 */
	public boolean isCurrentEmotionRequested() {
		return isEmotionRequested[currentEmotionTypeId];
	}
	public void setCurrentEmotionRequested() {
		if (currentEmotionTypeId >= 0) {
			isEmotionRequested[currentEmotionTypeId] = true;
		}
	}

}

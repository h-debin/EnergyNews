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
	
	private final static String DEBUG_TAG = "NewsManager";
	
	public static final String API_ADRESS_PRE = "http://api.minghe.me/api/v1/news?emotion_type=";
	public static final String[] EMOTION_TYPE = {"好","乐","怒","哀","惧","恶","惊"};
	private static final int startColor = Color.RED;//当前情绪的标题颜色start + n*colorDis
	private static final int colorDis = 1000;
	//标题栏的背景颜色,深色
	private static final int[] EMOTION_HOME_COLOR = {0xff1e6c0a, 0xffc40e39, 0xff974013, 
		0xff0c313d, 0xff2b2638, 0xff4e4b0a, 0xff091f6a};
	//背景色,浅色
	private static final int[] EMOTION_NEWS_COLOR = {0xffa9df9c, 0xffdd6a85, 0xffc4967e,
		0xff70869b, 0xffafacb5, 0xffa3a067, 0xff4e65b7};
	//标题栏的字体颜色
	private static final int[] EMOTION_TEXT_COLOR = {0xff030702, 0xff3d0613, 0xff32190c,
		0xffccd7db, 0xffbcaaee, 0xff181706, 0xff060a1a};
	
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
		LogUtil.d(DEBUG_TAG,"NewsManager");
		setCurrentEmotionTypeId(0);
		setCurrentNewsId(-1);
		db = EnergyNewsDB.getInstance(context);
	}
	
	/**
	 * 获取EnergyNewsDB的实例。
	 */
	public synchronized static NewsManager getInstance(Context context) {
		LogUtil.d(DEBUG_TAG,"getInstance");
		if (mNewsManager == null) {
			mNewsManager = new NewsManager(context);
		}
		return mNewsManager;
	}
	
	public List<News> getNewsList() {
		LogUtil.d(DEBUG_TAG,"getNewsList");
		return newsList;
	}
	public void resetNewsList() {
		LogUtil.d(DEBUG_TAG,"resetNewsList");
		newsList.clear();
		setCurrentNewsId(-1);
	}
	public void addToNewsList(News news) {
		LogUtil.d(DEBUG_TAG,"addToNewsList");
		newsList.add(news);
	}
	private void setCurrentEmotionTypeId(int emotionTypeId) {
		LogUtil.d(DEBUG_TAG,"setCurrentEmotionTypeId");
		currentEmotionTypeId = emotionTypeId;
	}
	private int getCurrentEmotionTypeId() {
		LogUtil.d(DEBUG_TAG,"getCurrentEmotionTypeId");
		return currentEmotionTypeId;
	}
	public String getCurrentEmotionType() {
		LogUtil.d(DEBUG_TAG,"getCurrentEmotionType");
		return EMOTION_TYPE[currentEmotionTypeId];
	}
	public int getCurrentEmotionColor() {
		LogUtil.d(DEBUG_TAG,"getCurrentEmotionColor");
		return EMOTION_HOME_COLOR[currentEmotionTypeId];
	}
	public int getCurrentEmotionColorBg() {
		LogUtil.d(DEBUG_TAG,"getCurrentEmotionColorBg");
		return EMOTION_NEWS_COLOR[currentEmotionTypeId];
	}
	public int getCurrentEmotionColorText() {
		LogUtil.d(DEBUG_TAG,"getCurrentEmotionColorText");
		return EMOTION_TEXT_COLOR[currentEmotionTypeId];
	}
	private void setCurrentNewsId(int newsId) {
		LogUtil.d(DEBUG_TAG,"setCurrentNewsId");
		currentNewsId = newsId;
	}
	public News getCurrentNews() {
		LogUtil.d(DEBUG_TAG,"getCurrentNews");
		if (currentNewsId < 0) {
			if (newsList.size() <= 0) {
				return null;
			}
			setCurrentNewsId(0);
		}
		return newsList.get(currentNewsId);
	}
	public String getApiAddress() {
		LogUtil.d(DEBUG_TAG,"getApiAddress");
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
		LogUtil.d(DEBUG_TAG,"changeEmotion");
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
		LogUtil.d(DEBUG_TAG,"changeNews");
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
		LogUtil.d(DEBUG_TAG,"queryNewsList");
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
		LogUtil.d(DEBUG_TAG,"isCurrentEmotionRequested");
		return isEmotionRequested[currentEmotionTypeId];
	}
	public void setCurrentEmotionRequested() {
		LogUtil.d(DEBUG_TAG,"setCurrentEmotionRequested");
		if (currentEmotionTypeId >= 0) {
			isEmotionRequested[currentEmotionTypeId] = true;
		}
	}

}

package com.energynews.app.data;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.energynews.app.db.EnergyNewsDB;
import com.energynews.app.model.News;
import com.energynews.app.util.LogUtil;

import android.content.Context;

public class NewsManager {
	
	private final static String DEBUG_TAG = "NewsManager";
	
	public static final String API_ADRESS_PRE = "http://api.minghe.me/api/v1/news?emotion_type=";
	public static final String[] EMOTION_TYPE = {"好","乐","怒","哀","惧","恶","惊"};
	public static final String[] EMOTION_TEXT = {"完美世界","一笑倾城","令人发指","哀鸿满路",
		"毛骨悚然","罄竹难书","春雷乍动"};
	//标题栏的背景颜色,深色
	private static final int[] EMOTION_HOME_COLOR = {0xff1e6c0a, 0xffc40e39, 0xff974013, 
		0xff0c313d, 0xff2b2638, 0xff4e4b0a, 0xff091f6a};
	//背景色,浅色
	private static final int[] EMOTION_NEWS_COLOR = {0xffa9df9c, 0xffdd6a85, 0xffc4967e,
		0xff70869b, 0xffafacb5, 0xffa3a067, 0xff4e65b7};
	//标题栏的字体颜色
	private static final int[] EMOTION_TEXT_COLOR = {0xff030702, 0xff3d0613, 0xff32190c,
		0xffccd7db, 0xffbcaaee, 0xff181706, 0xff060a1a};
	//纪录上一次离开该情绪时的索引
	private static final int[] EMOTION_LEAVE_ID = {-1, -1, -1, -1, -1, -1, -1};
	
	private static boolean[] isEmotionRequested = {false,false,false,false,false,false,false};
	
	private int currentEmotionTypeId = 0;
	private int currentNewsId = -1;//当前新闻在newsList中的索引
	private int lastNewsId = -1;//上一个新闻在newsList中的索引

	private List<News> newsList = new ArrayList<News>();
	
	private static NewsManager mNewsManager = null;
	private static EnergyNewsDB db = null;
	
	/**
	 * 将构造方法私有化
	 */
	private NewsManager(Context context) {
		LogUtil.d(DEBUG_TAG,"NewsManager");
		setCurrentEmotionTypeId(0);
		setNewsId(-1);
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
	}
	public void addToNewsList(News news) {
		LogUtil.d(DEBUG_TAG,"addToNewsList");
		newsList.add(news);
	}
	private void setCurrentEmotionTypeId(int emotionTypeId) {
		LogUtil.d(DEBUG_TAG,"setCurrentEmotionTypeId");
		//纪录当前选择的新闻索引
		rememberEmotionLeaveId();
		currentEmotionTypeId = emotionTypeId;
		//根据纪录信息设置当前情绪默认的新闻索引
		setNewsId(EMOTION_LEAVE_ID[currentEmotionTypeId]);
	}
	public void rememberEmotionLeaveId() {
		EMOTION_LEAVE_ID[currentEmotionTypeId] = currentNewsId;
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
	public String getCurrentText() {
		return EMOTION_TEXT[currentEmotionTypeId];
	}
	private void setNewsId(int newsId) {
		LogUtil.d(DEBUG_TAG,"setCurrentNewsId");
		lastNewsId = currentNewsId;
		currentNewsId = newsId;
		rememberEmotionLeaveId();
		//LogUtil.e(DEBUG_TAG,"setCurrentNewsId:last,crt:"+lastNewsId+","+currentNewsId);
	}
	public News getCurrentNews() {
		LogUtil.d(DEBUG_TAG,"getCurrentNews");
		if (currentNewsId < 0 || currentNewsId >= newsList.size()) {
			if (newsList.size() <= 0) {
				return null;
			}
			setNewsId(0);
		}
		return newsList.get(currentNewsId);
	}
	//获得上一个新闻
	public News getLastNews() {
		LogUtil.d(DEBUG_TAG,"getLastNews");
		if (lastNewsId < 0 || lastNewsId >= newsList.size()) {
			return getCurrentNews();
		}
		return newsList.get(lastNewsId);
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
			setNewsId((1 + currentNewsId) % len);
		} else {
			setNewsId((currentNewsId - 1 + len) % len);
		}
		return getCurrentNews();
	}

	/**
	 * 查询当前类型的数据是否存在
	 * @param saveList 是否需要保存新闻列表
	 *        newData 是否有了新数据,如果是,则索引设为-1,否则用之前记录的索引
	 * @return 是否存在数据
	 */
	public boolean queryNewsList(boolean saveList, boolean newData) {
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
			//LogUtil.e(DEBUG_TAG, "0 eTypeId="+currentEmotionTypeId+"nId="+currentNewsId);
			//LogUtil.e(DEBUG_TAG, "newData="+newData+"LEAVE_ID="+EMOTION_LEAVE_ID[currentEmotionTypeId]);
			if (newData) {
				setNewsId(-1);
			} else {
				setNewsId(EMOTION_LEAVE_ID[currentEmotionTypeId]);
			}
			//LogUtil.e(DEBUG_TAG, "1 eTypeId="+currentEmotionTypeId+"nId="+currentNewsId);
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

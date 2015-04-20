package com.energynews.app.data;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.energynews.app.db.EnergyNewsDB;
import com.energynews.app.model.News;
import com.energynews.app.util.LogUtil;

import android.content.Context;
import android.text.TextUtils;

public class NewsManager {
	
	private final static String DEBUG_TAG = "NewsManager";

    private static Context mContext;
	
	public static final String API_ADRESS_PRE = "http://api.minghe.me/api/v1/news?emotion_type=";
	public static final String[] EMOTION_TYPE = {"好","乐","怒","哀","惧","恶","惊"};
	//纪录上一次离开该情绪时的索引
	private static final int[] EMOTION_LEAVE_ID = {-1, -1, -1, -1, -1, -1, -1};
	//纪录该情绪已浏览的新闻数量,如果浏览结束,则从服务器上再进行刷新浏览
	private static final int[] EMOTION_SCAN_COUNT = {0, 0, 0, 0, 0, 0, 0};
	
	private static boolean[] isEmotionRequested = {false,false,false,false,false,false,false};
	
	private int currentEmotionTypeId = 0;
	private int currentNewsId = -1;//当前新闻在newsList中的索引
	private int lastNewsId = -1;//上一个新闻在newsList中的索引

	private static List<News> newsList = new ArrayList<News>();
    private static List<News> newsListHead = new ArrayList<News>();
	
	private static NewsManager mNewsManager = null;
	private static EnergyNewsDB db = null;
	
	/**
	 * 将构造方法私有化
	 */
	private NewsManager(Context context) {
		LogUtil.d(DEBUG_TAG,"NewsManager");
        mContext = context;
		setCurrentEmotionTypeId(0);
		setNewsId(-1);
		db = EnergyNewsDB.getInstance(context);
        initNewsListHead();
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

    public static List<News> getNewsListHead() {
        LogUtil.d(DEBUG_TAG,"getNewsListHead");
        for (News news : newsListHead) {
            if (TextUtils.isEmpty(news.getPicture())) {

            }
        }
        return newsListHead;
    }
	public static List<News> getNewsList() {
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
		LogUtil.d(DEBUG_TAG,"setCurrentEmotionTypeId currentEmotionTypeId = " + emotionTypeId);
		//纪录当前选择的新闻索引
		rememberEmotionLeaveId();
		currentEmotionTypeId = emotionTypeId;
		//根据纪录信息设置当前情绪默认的新闻索引
		setNewsId(EMOTION_LEAVE_ID[currentEmotionTypeId]);
	}
	public void rememberEmotionLeaveId() {
		EMOTION_LEAVE_ID[currentEmotionTypeId] = currentNewsId;
	}
    public int getCurrentEmotionTypeId() {
		LogUtil.d(DEBUG_TAG,"getCurrentEmotionTypeId");
		return currentEmotionTypeId;
	}
	public String getCurrentEmotionType() {
		LogUtil.d(DEBUG_TAG,"getCurrentEmotionType");
		return EMOTION_TYPE[currentEmotionTypeId];
	}
    public synchronized void initNewsListHead() {
        LogUtil.d(DEBUG_TAG,"initNewsListHead");
        for (int i = 0; i < EMOTION_TYPE.length; i++) {
            List<News> newslistLoad = db.queryNewsByEmotionType(EMOTION_TYPE[i]);
            if (newslistLoad.size() > 0) {
                newsListHead.add(newslistLoad.get(0));
                continue;
            }
            newsListHead.add(new News());
        }
    }
    public synchronized void updateNewsListHead() {
        LogUtil.d(DEBUG_TAG,"updateNewsListHead");
        if (currentNewsId >= 0) {
            News news = newsList.get(currentNewsId);
            newsListHead.set(currentEmotionTypeId, news);
        }
    }
	public void setNewsId(int newsId) {
		LogUtil.d(DEBUG_TAG,"setCurrentNewsId");
		lastNewsId = currentNewsId;
		currentNewsId = newsId;
		rememberEmotionLeaveId();
		//LogUtil.e(DEBUG_TAG,"setCurrentNewsId:last,crt:"+lastNewsId+","+currentNewsId);
	}
    public int getCurrentNewsId() {
        if (currentNewsId < 0 || currentNewsId >= newsList.size()) {
            if (newsList.size() <= 0) {
                return -1;
            }
            if (EMOTION_LEAVE_ID[currentEmotionTypeId] >= 0 && EMOTION_LEAVE_ID[currentEmotionTypeId] < newsList.size()) {
                setNewsId(EMOTION_LEAVE_ID[currentEmotionTypeId]);
            } else {
                setNewsId(0);
            }
        }
        return currentNewsId;
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
			EMOTION_SCAN_COUNT[currentEmotionTypeId] += 1;
		} else {
			setNewsId((currentNewsId - 1 + len) % len);
			EMOTION_SCAN_COUNT[currentEmotionTypeId] -= 1;
		}
		return getCurrentNews();
	}
	
	public boolean checkRefresh() {
		int scan = Math.abs(EMOTION_SCAN_COUNT[currentEmotionTypeId]);
		return scan > 0 && (scan >= newsList.size());
	}
	public void resetScanCount() {
		EMOTION_SCAN_COUNT[currentEmotionTypeId] = 0;
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
			if (newData) {
				setNewsId(0);
			} else {
				setNewsId(EMOTION_LEAVE_ID[currentEmotionTypeId]);
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

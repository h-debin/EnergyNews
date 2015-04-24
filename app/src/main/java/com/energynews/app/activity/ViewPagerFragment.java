package com.energynews.app.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ToxicBakery.viewpager.transforms.CubeOutTransformer;
import com.ToxicBakery.viewpager.transforms.FlipVerticalTransformer;
import com.energynews.app.R;
import com.energynews.app.data.NewsManager;
import com.energynews.app.db.EnergyNewsDB;
import com.energynews.app.model.News;
import com.energynews.app.util.HttpCallbackListener;
import com.energynews.app.util.HttpUtil;
import com.energynews.app.util.LogUtil;
import com.energynews.app.util.Utility;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aoshi on 15-4-21.
 */
public class ViewPagerFragment extends Fragment {

    private final static String DEBUG_TAG = "ViewPagerFragment";

    private static Activity mActivity;
    private final static String NEWSLIST = "NEWSLIST";
    private final static String NEWSHEAD = "NEWSHEAD";
    private static String NEWS_SHOW_TYPE;
    private static ViewPager mPager;
    private static ImagePagerAdapter mAdapter;
    private static List<News> newsList = new ArrayList<News>();
    private static int positionOffset = 0;
    private static int currentPosition = 4000;

    private static EnergyNewsDB energyNewsDB;
    private static NewsManager newsManager;

    private static int emotionChangeType = 1;
    private static int errorcount = 0;
    float xScreenCenter, yScreenCenter;
    int yodis = 0;
    boolean yorien = false;//记录是y方向移动
    boolean orienconfirm = false;//记录已经确认是y方向移动

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        mActivity = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_view_pager, container, false);
        mPager = (ViewPager) rootView.findViewById(R.id.view_pager_fragment);

        WindowManager wm = mActivity.getWindowManager();
        xScreenCenter = wm.getDefaultDisplay().getWidth() / 2;
        yScreenCenter = wm.getDefaultDisplay().getHeight() / 2;

        mAdapter = new ImagePagerAdapter(mActivity.getFragmentManager());

        setPageTransf(NEWSHEAD);
        //mPager = (ViewPager) findViewById(R.id.view_pager);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(currentPosition);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                LogUtil.d(DEBUG_TAG, "mPager onPageSelected position = " + position);
                currentPosition = position;
                if (yorien) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            changeEmotion(-yodis);
                        }
                    }).start();
                }
                yorien = false;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        energyNewsDB = EnergyNewsDB.getInstance(mActivity);
        newsManager = NewsManager.getInstance(mActivity);

        new Thread(new Runnable() {
            @Override
            public void run() {
                energyNewsDB.setOldNews(Utility.getDays() - 1);//设置一天之前的新闻为旧新闻
                boolean exists = newsManager.queryNewsList(false);//从数据库中查询数据
                if (exists) {//存在数据
                    updateViewPager();//设置界面的数据
                }
                if (savedInstanceState == null) {
                    for (String emotionType : newsManager.getEmotionType()) {
                        queryFromServer(newsManager.getApiAddress(emotionType), emotionType);//从服务器上查找新数据
                    }
                }
            }
        }).start();

        return rootView;
    }
    public ViewPager getViewPager() {
        return  mPager;
    }
    public void onSaveInstanceState(Bundle outState) {
        if (newsList.size() > 0 && NEWS_SHOW_TYPE == NEWSLIST) {
            newsManager.setNewsId(calNewsIdx(mPager.getCurrentItem()));//记录浏览到的位置
        }
    }

    private synchronized void updateViewPager() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                newsList = newsManager.getNewsList();
                if (newsList.size() > 0) {
                    int cpos = mPager.getCurrentItem();
                    positionOffset = cpos % newsList.size() - newsManager.getCurrentNewsId();
                    setPageTransf(NEWSLIST);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private synchronized void showHorizonPager(boolean force) {
        LogUtil.d(DEBUG_TAG,"showHorizonPager");
        if (!force && NEWS_SHOW_TYPE == NEWSLIST) return;
        updateViewPager();
    }
    private synchronized void showVerticalPager(boolean force) {
        LogUtil.d(DEBUG_TAG,"showVerticalPager");
        if (!force && NEWS_SHOW_TYPE == NEWSHEAD) return;
        if (newsList.size() > 0) {
            newsManager.updateNewsListHead();//改变当前newsListHead
        }
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                newsList = newsManager.getNewsListHead();
                if (newsList.size() > 0) {
                    int cpos = mPager.getCurrentItem();
                    positionOffset = cpos % newsList.size() - newsManager.getCurrentEmotionTypeId();
                    setPageTransf(NEWSHEAD);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }
    private synchronized void reshowPager() {
        LogUtil.d(DEBUG_TAG,"reshowPager");
        //刷新当前列表,让标题变为红色
        if (NEWS_SHOW_TYPE == NEWSHEAD) {
            showVerticalPager(true);
        } else {
            showHorizonPager(true);
        }
    }
    public synchronized void scanHistory() {
        newsManager.queryNewsList(true);//从数据库中查询数据
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                newsList = newsManager.getNewsList();
                if (newsList.size() > 0) {
                    int cpos = mPager.getCurrentItem();
                    positionOffset = cpos % newsList.size() - newsManager.getReadedId();
                    setPageTransf(NEWSLIST);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public synchronized void setPageTransf(String type) {
        if (NEWS_SHOW_TYPE == type) return;
        if (type == NEWSLIST) {
            mPager.setPageTransformer(true, new CubeOutTransformer());
        } else {
            mPager.setPageTransformer(true, new FlipVerticalTransformer());//上下翻动
        }
        NEWS_SHOW_TYPE = type;
    }

    /**
     * 向服务器请求成功
     */
    private void requestSuccess(boolean saved, String emotionType) {
        LogUtil.d(DEBUG_TAG,"requestSuccess");
        boolean exists = newsManager.isExistNews(emotionType);
        if (exists) {
            //LogUtil.e(DEBUG_TAG, "requestSuccess emotionType = " + emotionType);
            errorcount = 0;
            newsManager.setEmotionRequested(emotionType);//纪录情绪已经被查找过
            if (emotionType == newsManager.getCurrentEmotionType()) {
                if (saved || newsList.size() == 0) {
                    newsManager.queryNewsList(true);//有数据更新,则重新从数据库中获取数据,并保存获得的数据
                    updateViewPager();
                }
            } else if (saved){//更新listHead
                if (newsManager.updateNewsListHead(emotionType)) {
                    if (NEWS_SHOW_TYPE == NEWSHEAD) {//刷新当前显示
                        showVerticalPager(true);
                    }
                }
            }
        } else {
            if (emotionType == newsManager.getCurrentEmotionType()) {
                errorcount += 1;
                if (errorcount < 7) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            changeEmotion(emotionChangeType);//改变情绪继续查找
                        }
                    }).start();
                }
            }
        }
    }

    private void changeEmotion(int changeType) {
        LogUtil.d(DEBUG_TAG,"changeEmotion");
        emotionChangeType = changeType;
        newsManager.changeEmotion(changeType);
        boolean exits = newsManager.queryNewsList(false);
        boolean isRequested = newsManager.isCurrentEmotionRequested();
        if (!exits) {//该情绪没有对应的新闻
            if (!isRequested) {//没有从服务器上查询过
                queryFromServer(newsManager.getApiAddress(), newsManager.getCurrentEmotionType());//网上加载新闻
            } else {
                changeEmotion(emotionChangeType);//改变情绪继续查找
            }
        } else {
            updateViewPager();//刷新显示数据
            if (!isRequested) {//没有从服务器上查询过
                queryFromServer(newsManager.getApiAddress(), newsManager.getCurrentEmotionType());//网上加载新闻
            }
        }
    }

    public synchronized void jumpToStart() {
        newsManager.setNewsId(0);
        reshowPager();
    }

    public void refreshFromServer(final String emotionType) {
        LogUtil.d(DEBUG_TAG,"queryFromServer");
        newsManager.resetEmotionRequested();//重新记录所有的情绪都没有被查询过
        HttpUtil.sendHttpRequest(newsManager.getApiAddress(), new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                final boolean saved = Utility.handleEnergyNewsResponse(energyNewsDB, response);
                // 通过runOnUiThread()方法回到主线程处理逻辑
                if (saved && emotionType == newsManager.getCurrentEmotionType()) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newsManager.queryNewsList(true);//有数据更新,则重新从数据库中获取数据,并保存获得的数据
                            updateViewPager();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                errorcount += 1;
                if (errorcount > 4) {
                    // 通过runOnUiThread()方法回到主线程处理逻辑
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //网络不通
                            Toast.makeText(mActivity, "Network offline.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    refreshFromServer(emotionType);//try again
                }
            }
        });
    }

    private void queryFromServer(String address, final String emotionType) {
        LogUtil.d(DEBUG_TAG,"queryFromServer");
        //LogUtil.e(DEBUG_TAG, "queryFromServer emotionType = " + emotionType);
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                final boolean saved = Utility.handleEnergyNewsResponse(energyNewsDB, response);
                // 通过runOnUiThread()方法回到主线程处理逻辑
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        requestSuccess(saved, emotionType);//如果有新的数据,则刷新页面
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                errorcount += 1;
                if (errorcount > 4) {
                    // 通过runOnUiThread()方法回到主线程处理逻辑
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //网络不通
                            Toast.makeText(mActivity, "Network offline.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    queryFromServer(newsManager.getApiAddress(), emotionType);//try again
                }
            }
        });
    }

    private static int calNewsIdx(int cpos) {
        if (newsList.size() > 0) {
            return Math.abs(cpos - positionOffset) % newsList.size();
        }
        return 0;
    }

    private void showNewsContent() {
        LogUtil.d(DEBUG_TAG,"showNewsContent current newsid = "+newsManager.getCurrentNewsId());
        synchronized(newsList) {
            if (newsList.size() > 0) {
                News news = newsList.get(calNewsIdx(mPager.getCurrentItem()));
                if (!news.isReaded()) {
                    newsManager.addReadedNews(news);
                    news.setReaded(1);
                    reshowPager();//刷新截面显示
                }
                NewsContentActivity.actionStart(mActivity, news.getLink());
            }
        }
    }

    float xdown, ydown, xpre, ypre;
    public boolean myDispatchTouchEvent(MotionEvent event, boolean downInToolbar) {
        // TODO Auto-generated method stub
        int action = event.getAction();
        float xdis = event.getX() - xdown;
        float ydis = event.getY() - ydown;
        if (action == MotionEvent.ACTION_DOWN){
            //LogUtil.e(DEBUG_TAG, "mPager ACTION_DOWN");
            xdown = event.getX();
            ydown = event.getY();
            xpre = xdown;
            ypre = ydown;
            yorien = true;
            orienconfirm = false;
            if (newsList.size() > 0 && NEWS_SHOW_TYPE == NEWSLIST) {
                newsManager.setNewsId(calNewsIdx(mPager.getCurrentItem()));//记录浏览到的位置
            }

        } else if (action == MotionEvent.ACTION_UP){
            yodis = ydis > 0 ? 1 : -1;
            if (yorien && !orienconfirm && Math.abs(ydis) < 5 && !downInToolbar) {
                showNewsContent();//显示新闻,如果toolbar之前时显示的,则暂不打开新闻内容
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if ((Math.abs(xdis) + Math.abs(ydis)) < 3) {
                return false;
            }
            if (yorien && !orienconfirm) {//没有确定方向
                float xmdis = event.getX() - xpre;
                float ymdis = event.getY() - ypre;
                if (Math.abs(xdis) > Math.abs(ydis)) {
                    yorien = false;
                    //LogUtil.e(DEBUG_TAG, "yorien = false. xmdis = "+xmdis+", ymdis"+ymdis);
                }
                xpre = event.getX();
                ypre = event.getY();
                //变为横向 yorien = false, 重新设置viewpager
                if (!yorien) {
                    showHorizonPager(false);//刷新显示情绪列表
                }
            }
            if (!orienconfirm && Math.abs(ydis) * 30 > yScreenCenter) {//确定是移动方向
                orienconfirm = true;
            }
        }
        float xnew, ynew;
        if (yorien) {
            if (action == MotionEvent.ACTION_DOWN) {
                xnew = xScreenCenter;
                ynew = yScreenCenter;
            } else {
                showVerticalPager(false);//刷新显示y方向
                xnew = xScreenCenter + 2 * (event.getY() - ydown);
                ynew = yScreenCenter + 2 * (event.getX() - xdown);
            }
        } else {
            xnew = xScreenCenter + 2 * (event.getX() - xdown);
            ynew = yScreenCenter + 2 * (event.getY() - ydown);
        }
        //LogUtil.e(DEBUG_TAG, NEWS_SHOW_TYPE+", xnew = "+xnew+", ynew = "+ynew);
        event.setLocation(xnew, ynew);
        return true;
    }

    public static class PlaceholderFragment extends android.app.Fragment {

        private static final String EXTRA_POSITION = "EXTRA_POSITION";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final int position = getArguments().getInt(EXTRA_POSITION);
            int itempositon = -1;
            News news = null;
            synchronized(newsList) {
                if (newsList.size() > 0) {
                    itempositon = calNewsIdx(position);
                    news = newsList.get(itempositon);
                }
            }
            LogUtil.d("PlaceholderFragment", "onCreateView position = " + position + ", cpos = " + currentPosition);

            final View rootView = inflater.inflate(R.layout.news_list_relative, container, false);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.news_image);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            TextView emotion = (TextView) rootView.findViewById(R.id.home_title_text);
            TextView newstitle = (TextView) rootView.findViewById(R.id.news_title_text);
            if (itempositon >= 0) {
                if (TextUtils.isEmpty(news.getLink())) {//显示的是每种情绪的第一条，没有该新闻，则找下一条新闻
                    int idis = (currentPosition - position) < 0 ? 1 : -1;
                    for (int i = 1; i < newsList.size(); i++) {
                        int cids = (newsList.size() + itempositon + i * idis) % newsList.size();
                        if (!TextUtils.isEmpty(newsList.get(cids).getLink())) {
                            news = newsList.get(cids);
                            break;
                        }
                    }
                }
                if (TextUtils.isEmpty(news.getLink())) {//显示的是每种情绪的第一条，没有该新闻，则找下一条新闻
                    imageView.setImageResource(R.drawable.loading1);
                    emotion.setVisibility(View.GONE);
                    newstitle.setVisibility(View.GONE);
                } else {
                    UrlImageViewHelper.setUrlDrawable(imageView, news.getPicture());
                    emotion.setText(news.getEmotion());
                    newstitle.setText(news.getTitle());
                    emotion.setVisibility(View.VISIBLE);
                    newstitle.setVisibility(View.VISIBLE);
                    if (news.isReaded()) {
                        int txtcolor = Color.GREEN;
                        emotion.setTextColor(txtcolor);
                        newstitle.setTextColor(txtcolor);
                    }
                }
            } else {
                imageView.setImageResource(R.drawable.loading1);
                emotion.setVisibility(View.GONE);
                newstitle.setVisibility(View.GONE);
            }

            return rootView;
        }

    }

    private static final class ImagePagerAdapter extends FragmentStatePagerAdapter {

        public ImagePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public android.app.Fragment getItem(int position) {
            //LogUtil.e("ImagePagerAdapter", "getItem " + position);
            final Bundle bundle = new Bundle();
            bundle.putInt(PlaceholderFragment.EXTRA_POSITION, position);

            final PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.setArguments(bundle);

            return fragment;
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        public int getItemPosition(Object object){
            return POSITION_NONE;
        }
    }
}

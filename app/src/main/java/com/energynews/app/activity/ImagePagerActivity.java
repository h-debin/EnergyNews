package com.energynews.app.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ImagePagerActivity extends BaseActivity {//FragmentActivity

    private final static String DEBUG_TAG = "ImagePagerActivity";

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ImagePagerActivity.class);
        context.startActivity(intent);
    }

    private ViewPager mPager;
    private ImagePagerAdapter mAdapter;
    private static List<News> newsList = new ArrayList<News>();
    private static int positionOffset = 0;

    private EnergyNewsDB energyNewsDB;
    private NewsManager newsManager;

    private int emotionChangeType = 1;
    private static int errorcount = 0;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.d(DEBUG_TAG,"onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_image_pager);

        mAdapter = new ImagePagerAdapter(getFragmentManager());

        mPager = (ViewPager) findViewById(R.id.view_pager);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(4000);
        mPager.setPageTransformer(true, new CubeOutTransformer());
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                LogUtil.e(DEBUG_TAG, "mPager onPageSelected position = " + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        energyNewsDB = EnergyNewsDB.getInstance(this);
        newsManager = NewsManager.getInstance(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                energyNewsDB.setOldNews(Utility.getDays() - 1);//设置一天之前的新闻为旧新闻
                boolean exists = newsManager.queryNewsList(true, false);//从数据库中查询数据
                if (exists) {//存在数据
                    updateViewPager();//设置界面的数据
                }
                queryFromServer(newsManager.getApiAddress());//从服务器上查找新数据
            }
        }).start();
    }

    protected void onSaveInstanceState(Bundle outState) {
        if (newsList.size() > 0) {
            newsManager.setNewsId(calNewsIdx(mPager.getCurrentItem()));//记录浏览到的位置
        }
    }

    private synchronized void updateViewPager() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                newsList = newsManager.getNewsList();
                int cpos = mPager.getCurrentItem();
                positionOffset = cpos % newsList.size() - newsManager.getCurrentNewsId();
                mAdapter.notifyDataSetChanged();
                mPager.setCurrentItem(cpos);
            }
        });
    }

    /**
     * 向服务器请求成功
     */
    private void requestSuccess(boolean saved) {
        LogUtil.d(DEBUG_TAG,"requestSuccess");
        boolean exists = newsManager.queryNewsList(false, false);
        if (exists) {
            errorcount = 0;
            newsManager.setCurrentEmotionRequested();//纪录当前情绪已经被查找过
            if (saved) {
                newsManager.queryNewsList(true, true);//有数据更新,则重新从数据库中获取数据,并保存获得的数据
                updateViewPager();
            }
        } else {
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

    private void changeEmotion(int changeType) {
        LogUtil.d(DEBUG_TAG,"changeEmotion");
        emotionChangeType = changeType;
        newsManager.changeEmotion(changeType);
        boolean exits = newsManager.queryNewsList(true, false);
        boolean isRequested = newsManager.isCurrentEmotionRequested();
        if (!exits) {//该情绪没有对应的新闻
            if (!isRequested) {//没有从服务器上查询过
                queryFromServer(newsManager.getApiAddress());//网上加载新闻
            } else {
                changeEmotion(emotionChangeType);//改变情绪继续查找
            }
        } else {
            updateViewPager();//刷新显示数据
            if (!isRequested) {//没有从服务器上查询过
                queryFromServer(newsManager.getApiAddress());//网上加载新闻
            }
        }
    }

    private void queryFromServer(String address) {
        LogUtil.d(DEBUG_TAG,"queryFromServer");
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                final boolean saved = Utility.handleEnergyNewsResponse(energyNewsDB, response);
                // 通过runOnUiThread()方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        requestSuccess(saved);//如果有新的数据,则刷新页面
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                errorcount += 1;
                if (errorcount > 4) {
                    // 通过runOnUiThread()方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //网络不通
                            Toast.makeText(ImagePagerActivity.this, "Network offline.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    queryFromServer(newsManager.getApiAddress());//try again
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
        LogUtil.d(DEBUG_TAG,"showNewsContent");
        if (newsList.size() > 0) {
            News news = newsList.get(calNewsIdx(mPager.getCurrentItem()));
            NewsContentActivity.actionStart(this, news.getLink());
        }
    }

    float xdown, ydown, xpre, ypre;
    int yodis;
    boolean yorien = true;
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN){
            //LogUtil.e(DEBUG_TAG, "mPager ACTION_DOWN");
            xdown = event.getX();
            ydown = event.getY();
            xpre = xdown;
            ypre = ydown;
            yorien = true;
            yodis = 0;
            //mPager.setPageTransformer(true, new CubeOutTransformer());//左右翻动
        } else if (action == MotionEvent.ACTION_UP){
            float xdis = event.getX() - xdown;
            float ydis = event.getY() - ydown;
            //LogUtil.e(DEBUG_TAG, "action up ydis = " + ydis + " yorien = " + yorien);
            if (yorien && Math.abs(ydis) > 50) {
                mPager.setPageTransformer(true, new FlipVerticalTransformer());//上下翻动
                if (newsList.size() > 0) {
                    newsManager.setNewsId(calNewsIdx(mPager.getCurrentItem()));//记录浏览到的位置
                }
                //newsList = new ArrayList<News>();
                //mAdapter.notifyDataSetChanged();
                mPager.setCurrentItem(mPager.getCurrentItem() - yodis);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        changeEmotion(yodis);
                    }
                }).start();
            } else if (yorien && Math.abs(ydis) < 5) {
                showNewsContent();//显示新闻
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (yorien) {
                float xmdis = event.getX() - xpre;
                float ymdis = event.getY() - ypre;
                if (Math.abs(xmdis) > Math.abs(ymdis)) {
                    if (Math.abs(xmdis) > 1) {
                        yorien = false;
                    }
                } else if (Math.abs(ymdis) > 0.001) {
                    if (yodis == 0) {
                        yodis = ymdis > 0 ? 1 : -1;
                    } else {
                        yorien = (((float)yodis * ymdis) > 0);//相同方向
                    }
                }
                xpre = event.getX();
                ypre = event.getY();
                //变为横向 yorien = false, 重新设置viewpager
                if (!yorien) {

                }
            }
        }
        if (yorien) {
            event.setLocation(event.getY(), event.getX());
            mPager.setPageTransformer(true, new FlipVerticalTransformer());//上下翻动
        } else {
            event.setLocation(ydown + event.getX() - xdown, xdown + event.getY() - ydown);
            mPager.setPageTransformer(true, new CubeOutTransformer());//左右翻动
        }
        return super.dispatchTouchEvent(event);
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String EXTRA_POSITION = "EXTRA_POSITION";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final int position = getArguments().getInt(EXTRA_POSITION);
            int itempositon = -1;
            if (newsList.size() > 0) {
                itempositon = calNewsIdx(position);
            }
            //LogUtil.e("PlaceholderFragment", "onCreateView position = " + position);

            final View rootView = inflater.inflate(R.layout.news_list_relative, container, false);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.news_image);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            TextView emotion = (TextView) rootView.findViewById(R.id.home_title_text);
            TextView newstitle = (TextView) rootView.findViewById(R.id.news_title_text);
            if (itempositon >= 0) {
                News news = newsList.get(itempositon);
                UrlImageViewHelper.setUrlDrawable(imageView, news.getPicture(), R.drawable.loading1);

                emotion.setText(news.getEmotion());
                newstitle.setText(news.getTitle());
                emotion.setVisibility(View.VISIBLE);
                newstitle.setVisibility(View.VISIBLE);
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
        public Fragment getItem(int position) {
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

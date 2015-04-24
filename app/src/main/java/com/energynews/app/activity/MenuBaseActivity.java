package com.energynews.app.activity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.energynews.app.R;
import com.energynews.app.data.NewsManager;
import com.energynews.app.db.EnergyNewsDB;
import com.energynews.app.util.LogUtil;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemLongClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aoshi on 15-4-21.
 */
public class MenuBaseActivity extends ActionBarActivity implements OnMenuItemClickListener,
        OnMenuItemLongClickListener {

    private final static String DEBUG_TAG = "MenuBaseActivity";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private final static String ISFIRSTLOAD = "is_first_load";
    private FragmentManager fragmentManager;
    private DialogFragment mMenuDialogFragment;
    private Toolbar mToolbar;
    private ViewPagerFragment viewPagerFragment;
    private int toolbarHight;
    private int[] toolbarLocation = new int[2];

    private final static int CLOSE_ITEM = 0;
    private final static int SHOW_HELPER = 1;
    private final static int REFRESH = 2;
    private final static int SCAN_HISTORY = 3;
    private final static int JUNPTOSTART = 4;
    private final static int CLOSE_TOOLBAR = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d(DEBUG_TAG,"onCreate");
        setContentView(R.layout.activity_main);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        fragmentManager = getSupportFragmentManager();
        initToolbar();
        toolbarHight = (int) getResources().getDimension(R.dimen.tool_bar_height);
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(toolbarHight, getMenuObjects());
        if (savedInstanceState != null) {
            //LogUtil.e(DEBUG_TAG, "savedInstanceState != null");
            viewPagerFragment = (ViewPagerFragment) fragmentManager.getFragment(savedInstanceState, "mFragment");
        } else {
            viewPagerFragment = new ViewPagerFragment();
            addFragment(viewPagerFragment, true, R.id.container);
        }

        if (pref.getBoolean(ISFIRSTLOAD, true)) {//第一次运行程序,打开helper窗口
            editor = pref.edit();
            editor.putBoolean(ISFIRSTLOAD, false);//记录已经打开过了
            editor.commit();
            showHelperActivity();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        LogUtil.d(DEBUG_TAG,"onSaveInstanceState");
        super.onSaveInstanceState(outState);
        if (viewPagerFragment.isAdded()){
            fragmentManager.putFragment(outState, "mFragment", viewPagerFragment);
        }
    }

    @Override
    public void onDestroy() {
        LogUtil.d(DEBUG_TAG,"onDestroy");
        NewsManager.getInstance(this).setDbReaded();//把浏览过的新闻写入数据库
        super.onDestroy();
    }

    private List<MenuObject> getMenuObjects() {
        LogUtil.d(DEBUG_TAG,"getMenuObjects");
        // You can use any [resource, bitmap, drawable, color] as image:
        // item.setResource(...)
        // item.setBitmap(...)
        // item.setDrawable(...)
        // item.setColor(...)
        // You can set image ScaleType:
        // item.setScaleType(ScaleType.FIT_XY)
        // You can use any [resource, drawable, color] as background:
        // item.setBgResource(...)
        // item.setBgDrawable(...)
        // item.setBgColor(...)
        // You can use any [color] as text color:
        // item.setTextColor(...)
        // You can set any [color] as divider color:
        // item.setDividerColor(...)

        List<MenuObject> menuObjects = new ArrayList<>();

        MenuObject close = new MenuObject();
        close.setResource(R.drawable.icn_close);

        MenuObject send = new MenuObject("关于");
        send.setResource(R.drawable.icn_info);

        MenuObject like = new MenuObject("刷新");
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.icn_refresh);
        like.setBitmap(b);

        MenuObject addFr = new MenuObject("浏览记录");
        BitmapDrawable bd = new BitmapDrawable(getResources(),
                BitmapFactory.decodeResource(getResources(), R.drawable.icn_eye));
        addFr.setDrawable(bd);

        MenuObject addFav = new MenuObject("查看最新");
        addFav.setResource(R.drawable.icn_restart);

        MenuObject block = new MenuObject();
        block.setResource(R.drawable.icn_5);

        menuObjects.add(close);
        menuObjects.add(send);
        menuObjects.add(like);
        menuObjects.add(addFr);
        menuObjects.add(addFav);
        menuObjects.add(block);
        return menuObjects;
    }

    private void initToolbar() {
        LogUtil.d(DEBUG_TAG,"initToolbar");
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mToolBarTextView = (TextView) findViewById(R.id.text_view_toolbar_title);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mToolbar.setNavigationIcon(R.drawable.btn_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mToolBarTextView.setText("心闻");
        //mToolbar.setVisibility(View.GONE);
    }

    protected void addFragment(Fragment fragment, boolean addToBackStack, int containerId) {
        LogUtil.d(DEBUG_TAG,"addFragment");
        invalidateOptionsMenu();
        String backStackName = fragment.getClass().getName();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(containerId, fragment, backStackName)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        LogUtil.d(DEBUG_TAG,"onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogUtil.d(DEBUG_TAG,"onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.context_menu:
                if (fragmentManager.findFragmentByTag(ContextMenuDialogFragment.TAG) == null) {
                    mMenuDialogFragment.show(fragmentManager, ContextMenuDialogFragment.TAG);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        LogUtil.d(DEBUG_TAG,"onBackPressed");
        if (fragmentManager.getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMenuItemClick(View clickedView, int position) {
        LogUtil.d(DEBUG_TAG,"onMenuItemClick");
        //Toast.makeText(this, "Clicked on position: " + position, Toast.LENGTH_SHORT).show();
        switch (position) {
            case CLOSE_ITEM: {
                break;
            }
            case SHOW_HELPER: {
                showHelperActivity();
                break;
            }
            case REFRESH: {
                refreshFromServer();
                break;
            }
            case SCAN_HISTORY: {
                scanHistory();
                break;
            }
            case JUNPTOSTART: {
                jumpToStart();
                break;
            }
            case CLOSE_TOOLBAR: {
                mToolbar.setVisibility(View.GONE);
                break;
            }
            default:
        }
        mToolbar.setVisibility(View.GONE);
    }

    @Override
    public void onMenuItemLongClick(View clickedView, int position) {
        //Toast.makeText(this, "Long clicked on position: " + position, Toast.LENGTH_SHORT).show();
    }

    int toolbarVisibilityDown;
    boolean downInToolbar;
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mToolbar.getLocationOnScreen(toolbarLocation);
            toolbarVisibilityDown = mToolbar.getVisibility();
            downInToolbar = false;
            //LogUtil.e(DEBUG_TAG, "lx="+toolbarLocation[0]+", ly="+toolbarLocation[1]+", lh="+toolbarHight+", "+event.toString());
            if (event.getY() > toolbarLocation[1] && event.getY() - toolbarLocation[1] < toolbarHight) {
                if (toolbarVisibilityDown == View.GONE) {
                    mToolbar.setVisibility(View.VISIBLE);
                }
                downInToolbar = true;
            } else if (toolbarVisibilityDown == View.VISIBLE) {
                mToolbar.setVisibility(View.GONE);
            }
        }
        if (downInToolbar) {
            return super.dispatchTouchEvent(event);
        }
        //LogUtil.e(DEBUG_TAG, event.toString());
        if ( viewPagerFragment.myDispatchTouchEvent(event, downInToolbar)) {
            return super.dispatchTouchEvent(event);
        }
        return false;
    }

    public void showHelperActivity() {
        LogUtil.d(DEBUG_TAG,"showHelperActivity");
        HelperActivity.startActivity(this);
    }

    private void scanHistory() {
        EnergyNewsDB.getInstance(this).mergeReadedToNews();//把浏览记录写入News表中
        viewPagerFragment.scanHistory();
    }

    private void refreshFromServer() {
        viewPagerFragment.refreshFromServer(NewsManager.getInstance(this).getCurrentEmotionType());
    }

    private void jumpToStart() {
        viewPagerFragment.jumpToStart();
    }
}

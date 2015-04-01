package com.energynews.app.util;

import android.app.Activity;

import java.util.List;
import java.util.ArrayList;

public class ActivityCollector {

	private final static String DEBUG_TAG = "AutoUpdateReceiver";
	
	public static List<Activity> activities = new ArrayList<Activity>();
	
	public static void addActivity(Activity activity) {
		LogUtil.d(DEBUG_TAG,"addActivity");
		activities.add(activity);
	}
	
	public static void removeActivity(Activity activity) {
		LogUtil.d(DEBUG_TAG,"removeActivity");
		activities.remove(activity);
	}
	
	public static void finishAll() {
		LogUtil.d(DEBUG_TAG,"finishAll");
		for (Activity activity : activities) {
			if (!activity.isFinishing()) {
				activity.finish();
			}
		}
	}

}

package com.energynews.app.service;

import com.energynews.app.activity.NewsContentActivity;
import com.energynews.app.db.EnergyNewsDB;
import com.energynews.app.receiver.AutoUpdateReceiver;
import com.energynews.app.util.HttpCallbackListener;
import com.energynews.app.util.HttpUtil;
import com.energynews.app.util.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class AutoUpdateService extends Service {
	
	public static void actionStart(Context context) {
		Intent intent = new Intent(context, AutoUpdateService.class);
		context.startService(intent);
	}
	
	public static void actionStop(Context context) {
		Intent intent = new Intent(context, AutoUpdateService.class);
		context.stopService(intent);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				updateNews();
			}
		}).start();
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		int anHour = 1 * 60 * 60 * 1000; // 这是8小时的毫秒数
		long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
		Intent i = new Intent(this, AutoUpdateReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		return super.onStartCommand(intent, flags, startId);
	}
	/**
	 * 更新新闻信息。
	 */
	private void updateNews() {
		//删除旧新闻
		int yestoday = Utility.getDays() - 1;
		EnergyNewsDB.getInstance(this).deleteOldNews(yestoday);
		String address = "";
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				Log.d("TAG", response);
				Utility.handleEnergyNewsResponse(EnergyNewsDB.getInstance(AutoUpdateService.this), response);
			}
			@Override
			public void onError(Exception e) {
				e.printStackTrace();
				}
			});
	}
}

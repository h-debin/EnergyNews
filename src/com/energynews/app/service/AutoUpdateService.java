package com.energynews.app.service;

import com.energynews.app.db.EnergyNewsDB;
import com.energynews.app.receiver.AutoUpdateReceiver;
import com.energynews.app.util.HttpCallbackListener;
import com.energynews.app.util.HttpUtil;
import com.energynews.app.util.LogUtil;
import com.energynews.app.util.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class AutoUpdateService extends Service {

	private final static String DEBUG_TAG = "AutoUpdateService";
	
	public static void actionStart(Context context) {
		LogUtil.d(DEBUG_TAG,"actionStart");
		Intent intent = new Intent(context, AutoUpdateService.class);
		context.startService(intent);
	}
	
	public static void actionStop(Context context) {
		LogUtil.d(DEBUG_TAG,"actionStop");
		Intent intent = new Intent(context, AutoUpdateService.class);
		context.stopService(intent);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.d(DEBUG_TAG,"onStartCommand");
		new Thread(new Runnable() {
			@Override
			public void run() {
				updateNews();
			}
		}).start();
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		int anHour = 1 * 60 * 60 * 1000; // 这是毫秒数
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
		LogUtil.d(DEBUG_TAG,"updateNews");
		//设置旧新闻
		int yestoday = Utility.getDays() - 1;
		EnergyNewsDB.getInstance(this).setOldNews(yestoday);
		String address = "";
		LogUtil.e("AutoUpdateService","updateNews()........");
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				Utility.handleEnergyNewsResponse(EnergyNewsDB.getInstance(AutoUpdateService.this), 
						response);
			}
			@Override
			public void onError(Exception e) {
				e.printStackTrace();
				}
			});
	}
}

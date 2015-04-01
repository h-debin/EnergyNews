package com.energynews.app.receiver;

import com.energynews.app.service.AutoUpdateService;
import com.energynews.app.util.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoUpdateReceiver extends BroadcastReceiver {

	private final static String DEBUG_TAG = "AutoUpdateReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		LogUtil.d(DEBUG_TAG,"onReceive");
		Intent i = new Intent(context, AutoUpdateService.class);
		context.startService(i);
	}

}

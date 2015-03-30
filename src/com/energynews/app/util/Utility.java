package com.energynews.app.util;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.energynews.app.db.EnergyNewsDB;
import com.energynews.app.model.News;

import android.text.TextUtils;
import android.util.Log;

public class Utility {
	
	/**
	 * 解析和处理服务器返回JSON，并将解析出的数据存储到本地。
	 */
	public static boolean handleEnergyNewsResponse(EnergyNewsDB energyNewsDB, String response) {
		boolean savedSuccess = false;
		try {
			JSONArray jsonArray = new JSONArray(response);
			int updateTime = getDays();
			//LogUtil.e("handleEnergyNewsResponse", "jsonArray.length() == " + jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				//LogUtil.e("handleEnergyNewsResponse", "i =  " + i);
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				News news = new News();
				news.setTitle(jsonObject.getString("title"));
				news.setLink(jsonObject.getString("link"));
				news.setPicture(jsonObject.getString("picture"));
				news.setEmotionType(jsonObject.getString("emotion_type"));
				String[] emotionList = {"le","hao","nu","ai","ju","e","jing"};
				int[] values = new int[7];
				for (int j = 0; j < 7; j++) {
					String temp = jsonObject.getString(emotionList[j]);
					if (TextUtils.isEmpty(temp) || "null".equals(temp)) {
						values[j] = 0;
					} else {
						values[j] = Integer.parseInt(temp);
					}
				}
				news.setLeValue(values[0]);
				news.setHaoValue(values[1]);
				news.setNuValue(values[2]);
				news.setAiValue(values[3]);
				news.setJuValue(values[4]);
				news.setEValue(values[5]);
				news.setJingValue(values[6]);
				news.setUpdateTime(updateTime);
				boolean bsave = energyNewsDB.saveNews(news);
				savedSuccess = savedSuccess || bsave;//是否保存成功
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return savedSuccess;
	}
	
	/**
	 * 获得今天-2015.01.01的天数
	 */
	public static int getDays() {
		//到1970.01.01的毫秒数
		long saveTime = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.set(2015,1,1,0,0,0);
		cal.set(Calendar.MILLISECOND,0);
		long lCut = cal.getTime().getTime();
		long aDay = 24*60*60*1000;//一天的毫秒数
		int iTime = (int)((saveTime-lCut)/aDay);//今天-2015.01.01的天数
		return iTime;
	}

}

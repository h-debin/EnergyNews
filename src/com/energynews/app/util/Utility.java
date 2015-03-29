package com.energynews.app.util;

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
	public static void handleEnergyNewsResponse(EnergyNewsDB energyNewsDB, String response) {
		try {
			JSONArray jsonArray = new JSONArray(response);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				News news = new News();
				news.setTitle(jsonObject.getString("title"));
				news.setLink(jsonObject.getString("link"));
				news.setPicture(jsonObject.getString("picture"));
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
				energyNewsDB.saveNews(news);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}

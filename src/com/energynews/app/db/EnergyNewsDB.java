package com.energynews.app.db;

import com.energynews.app.model.News;
import com.energynews.app.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;


public class EnergyNewsDB {
	
	private final static String DEBUG_TAG = "EnergyNewsDB";
	
	/**
	 * 数据库名
	 */
	public static final String DB_NAME = "energy_news.db";
	/**
	 * 数据库版本
	 */
	public static final int VERSION = 1;
	private static EnergyNewsDB energyNewsDB;
	private SQLiteDatabase db;
	
	private HashMap emotionMap;
	
	/**
	 * 将构造方法私有化
	 */
	private EnergyNewsDB(Context context) {
		LogUtil.d(DEBUG_TAG,"EnergyNewsDB");
		EnergyNewsOpenHelper dbHelper = new EnergyNewsOpenHelper(context,
				DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
		emotionMap = new HashMap();
		emotionMap.put("好", "hao_value");
		emotionMap.put("乐", "le_value");
		emotionMap.put("怒", "nu_value");
		emotionMap.put("哀", "ai_value");
		emotionMap.put("惧", "ju_value");
		emotionMap.put("恶", "e_value");
		emotionMap.put("惊", "jing_value");
	}
	
	/**
	 * 获取EnergyNewsDB的实例。
	 */
	public synchronized static EnergyNewsDB getInstance(Context context) {
		LogUtil.d(DEBUG_TAG,"EnergyNewsDB");
		if (energyNewsDB == null) {
			energyNewsDB = new EnergyNewsDB(context);
		}
		return energyNewsDB;
	}
	
	/**
	 * 根据情绪类型查询数据库
	 * @param emotionType
	 * @return 查询到的结果数据
	 */
	public synchronized List<News> queryNewsByEmotionType(String emotionType) {
		LogUtil.d(DEBUG_TAG,"queryNewsByEmotionType");
		List<News> list = new ArrayList<News>();
		if (TextUtils.isEmpty(emotionType)) {
			return list;
		}
		Cursor cursor = db.rawQuery("select * from News where emotion_type = ? "
				+ "and picture like 'http%' and old_news = 0 ORDER BY ? DESC", 
				new String[] {emotionType, "update_time"});
		//LogUtil.e("queryNewsByEmotionType", emotionType + "," + (String)(emotionMap.get(emotionType)));
		if (cursor.moveToFirst()) {
			do {
				News news = new News();
				news.setId(cursor.getInt(cursor.getColumnIndex("id")));
				news.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				news.setLink(cursor.getString(cursor.getColumnIndex("link")));
				news.setPicture(cursor.getString(cursor.getColumnIndex("picture")));
				news.setLeValue(cursor.getInt(cursor.getColumnIndex("le_value")));
				news.setHaoValue(cursor.getInt(cursor.getColumnIndex("hao_value")));
				news.setNuValue(cursor.getInt(cursor.getColumnIndex("nu_value")));
				news.setAiValue(cursor.getInt(cursor.getColumnIndex("ai_value")));
				news.setJuValue(cursor.getInt(cursor.getColumnIndex("ju_value")));
				news.setEValue(cursor.getInt(cursor.getColumnIndex("e_value")));
				news.setJingValue(cursor.getInt(cursor.getColumnIndex("jing_value")));
				list.add(news);
			} while (cursor.moveToNext());
		}
		return list;
	}
	
	/**
	 * 将News实例存储到数据库。
	 * return 是否有新数据保存
	 */
	public synchronized boolean saveNews(News news) {
		LogUtil.d(DEBUG_TAG,"saveNews");
		if (news != null) {
			Cursor cursor = db.rawQuery("select id from News where link = ?", new String[] {news.getLink()});
			//Cursor cursor = db.query("News", null, null, null, null, null, null);
			if (cursor.moveToFirst()) {
				return false;//链接已经存在
			}
			ContentValues values = new ContentValues();
			values.put("title", news.getTitle());
			values.put("link", news.getLink());
			values.put("picture", news.getPicture());
			values.put("emotion_type", news.getEmotionType());
			values.put("le_value", news.getLeValue());
			values.put("hao_value", news.getHaoValue());
			values.put("nu_value", news.getNuValue());
			values.put("ai_value", news.getAiValue());
			values.put("ju_value", news.getJuValue());
			values.put("e_value", news.getEValue());
			values.put("jing_value", news.getJingValue());
			values.put("update_time", news.getUpdateTime());
			db.insert("News", null, values);
			return true;
		}
		return false;
	}
	
	public synchronized void setOldNews(int iDays) {
		LogUtil.d(DEBUG_TAG,"setOldNews");
		String delSql = "update News set old_news = 1 where update_time < ? and old_news = 0";
		db.execSQL(delSql, new String[] {String.valueOf(iDays)});
		//删除五天前的记录
		deleteOldNews(iDays - 5);
	}
	/**
	 * 删除过期的新闻
	 * iDays <iDays的纪录都删除
	 */
	public synchronized void deleteOldNews(int iDays) {
		LogUtil.d(DEBUG_TAG,"deleteOldNews");
		String delSql = "delete from News where update_time < ?";
		db.execSQL(delSql, new String[] {String.valueOf(iDays)});
	}

}

package com.energynews.app.db;

import com.energynews.app.model.News;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class EnergyNewsDB {
	
	/**
	 * 数据库名
	 */
	public static final String DB_NAME = "energy_news";

	/**
	 * 数据库版本
	 */
	public static final int VERSION = 1;

	private static EnergyNewsDB energyNewsDB;

	private SQLiteDatabase db;
	
	/**
	 * 将构造方法私有化
	 */
	private EnergyNewsDB(Context context) {
		EnergyNewsOpenHelper dbHelper = new EnergyNewsOpenHelper(context,
				DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
	}
	
	/**
	 * 获取EnergyNewsDB的实例。
	 */
	public synchronized static EnergyNewsDB getInstance(Context context) {
		if (energyNewsDB == null) {
			energyNewsDB = new EnergyNewsDB(context);
		}
		return energyNewsDB;
	}
	
	/**
	 * 从数据库读取新闻信息。
	 */
	public List<News> loadNews() {
		List<News> list = new ArrayList<News>();
		Cursor cursor = db.query("News", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				News news = new News();
				news.setId(cursor.getInt(cursor.getColumnIndex("id")));
				news.setTitle(cursor.getString(cursor
						.getColumnIndex("title")));
				news.setLink(cursor.getString(cursor
						.getColumnIndex("link")));
				news.setPicture(cursor.getString(cursor
						.getColumnIndex("picture")));
				list.add(news);
			} while (cursor.moveToNext());
		}
		return list;
	}
	
	/**
	 * 将News实例存储到数据库。
	 */
	public void saveNews(News news) {
		if (news != null) {
			Cursor cursor = db.rawQuery("select * from News where link = ?", new String[] {news.getLink()});
			//Cursor cursor = db.query("News", null, null, null, null, null, null);
			if (cursor.moveToFirst()) {
				return;//链接已经存在
			}
			ContentValues values = new ContentValues();
			values.put("title", news.getTitle());
			values.put("link", news.getLink());
			values.put("picture", news.getPicture());
			values.put("le_value", news.getLeValue());
			values.put("hao_value", news.getHaoValue());
			values.put("nu_value", news.getNuValue());
			values.put("ai_value", news.getAiValue());
			values.put("ju_value", news.getJuValue());
			values.put("e_value", news.getEValue());
			values.put("jing_value", news.getJingValue());
			db.insert("News", null, values);
		}
	}

}

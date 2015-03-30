package com.energynews.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class EnergyNewsOpenHelper extends SQLiteOpenHelper {

	/** 
	 * News 表创建语句
	 */
	private static final String CREATE_NEWS = "create table News ("
			+ "id integer primary key autoincrement, "
			+ "title text, "
			+ "link text, "
			+ "picture text, "
			+ "emotion_type text, "
			+ "le_value integer, "
			+ "hao_value integer, "
			+ "nu_value integer, "
			+ "ai_value integer, "
			+ "ju_value integer, "
			+ "e_value integer, "
			+ "jing_value integer, "
			+ "update_time integer)";
	
	public EnergyNewsOpenHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_NEWS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}

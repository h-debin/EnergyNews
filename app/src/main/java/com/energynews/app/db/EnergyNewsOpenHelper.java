package com.energynews.app.db;

import com.energynews.app.util.LogUtil;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class EnergyNewsOpenHelper extends SQLiteOpenHelper {

	private final static String DEBUG_TAG = "EnergyNewsOpenHelper";
	
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
			+ "update_time integer, "
            + "old_news integer default 0,"//记录该新闻是否是已经过时的新闻,默认为0,旧新闻为1
			+ "readed integer default 0"//2版本开始增加,已经读过为1, 未读为0
            + ")";

    /**
     * News_readed 表创建语句
     */
    private static final String CREATE_NEWS_READED ="create table News_readed ("
            + "id integer primary key autoincrement, "
            + "link text, "
            + "to_news_table integer default 0" //是否已经加到News表中,1为已加入,0为未加入
            + ")";
	
	public EnergyNewsOpenHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		LogUtil.d(DEBUG_TAG,"EnergyNewsOpenHelper");
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		LogUtil.d(DEBUG_TAG,"onCreate");
		// TODO Auto-generated method stub
		db.execSQL(CREATE_NEWS);
        db.execSQL(CREATE_NEWS_READED);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		LogUtil.d(DEBUG_TAG,"onUpgrade");
        switch (oldVersion) {
            case 1: {//增加readed列
                String addReadedSql = "alter table News add readed integer default 0";
                db.execSQL(addReadedSql);
                //创建News_readed表
                db.execSQL(CREATE_NEWS_READED);
            }
            default:
        }
	}

}

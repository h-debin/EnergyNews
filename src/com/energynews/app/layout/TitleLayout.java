package com.energynews.app.layout;

import com.energynews.app.R;
import com.energynews.app.util.LogUtil;

import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.content.Context;
import android.util.AttributeSet;

public class TitleLayout extends LinearLayout {

	private final static String DEBUG_TAG = "TitleLayout";
	
	public TitleLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		LogUtil.d(DEBUG_TAG,"TitleLayout");
		LayoutInflater.from(context).inflate(R.layout.home_title, this);
	}

}

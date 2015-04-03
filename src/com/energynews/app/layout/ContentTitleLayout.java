package com.energynews.app.layout;

import com.energynews.app.R;
import com.energynews.app.util.LogUtil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class ContentTitleLayout extends LinearLayout {

	private final static String DEBUG_TAG = "ContentTitleLayout";
	
	public ContentTitleLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		LogUtil.d(DEBUG_TAG,"ContentTitleLayout");
		LayoutInflater.from(context).inflate(R.layout.content_title, this);
	}
}

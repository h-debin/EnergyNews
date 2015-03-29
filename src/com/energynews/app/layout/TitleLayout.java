package com.energynews.app.layout;

import com.energynews.app.R;
import com.energynews.app.fragment.NewsTitleFragment;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Toast;

public class TitleLayout extends LinearLayout implements OnClickListener {
	
	private Button homeTitle;
	private Button refresh;
	private TextView titleText;
	
	public TitleLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.home_title, this);
		
		titleText = (TextView) findViewById(R.id.title_text);
		homeTitle = (Button) findViewById(R.id.home_button);
		homeTitle.setOnClickListener(this);
		refresh = (Button) findViewById(R.id.refresh);
		refresh.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.home_button:
			((Activity) getContext()).finish();
			break;
		case R.id.refresh:
			NewsTitleFragment newsTitleFrag = (NewsTitleFragment) (((Activity) getContext()).
					getFragmentManager().findFragmentById(R.id.news_title_fragment));
			newsTitleFrag.refreshFromServer();
			break;
		default:
		}
	}

}

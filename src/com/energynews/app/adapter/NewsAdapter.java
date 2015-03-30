package com.energynews.app.adapter;

import java.util.List;

import com.energynews.app.R;
import com.energynews.app.model.News;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NewsAdapter extends ArrayAdapter<News> {

	private int resourceId;
	
	public NewsAdapter(Context context, int textViewResourceId, 
			List<News> objects) {
		super(context, textViewResourceId, objects);
		resourceId = textViewResourceId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		News news = getItem(position);
		View view;
		ViewHolder viewHolder;
		if (convertView == null) {
			view = LayoutInflater.from(getContext()).inflate(resourceId, null);
			viewHolder = new ViewHolder();
			viewHolder.newsImage = (ImageView) view.findViewById(R.id.news_image);
			viewHolder.newsTitleText = (TextView) view.findViewById(R.id.news_title_text);
			view.setTag(viewHolder);
		} else {
			view = convertView;
			viewHolder = (ViewHolder) view.getTag();
		}
		String imgUrl = news.getPicture();
		if (!"null".equals(imgUrl) && !TextUtils.isEmpty(imgUrl)) {
			UrlImageViewHelper.setUrlDrawable(viewHolder.newsImage, imgUrl);
			//Log.e("NewsAdapter", imgUrl);
		} else {
			//viewHolder.newsTitleText.setVisibility(View.GONE);
			imgUrl="http://h.hiphotos.baidu.com/image/pic/item/b151f8198618367a0d517ec22c738bd4b21ce5d1.jpg";
			UrlImageViewHelper.setUrlDrawable(viewHolder.newsImage, imgUrl);
		}
		viewHolder.newsTitleText.setText(news.getTitle());
		return view;
	}
	
	class ViewHolder {
		TextView newsTitleText;
		ImageView newsImage;
	}
}

package com.energynews.app.model;

import java.util.ArrayList;
import java.util.List;

public class News {

	private final static String DEBUG_TAG = "News";
	
	private int id;
	private String title;
	private String link;
	private String picture;
	private String emotionType;
	private int leValue;
	private int haoValue;
	private int nuValue;
	private int aiValue;
	private int juValue;
	private int eValue;
	private int jingValue;
	private int updateTime;
	
	private List<ValueToText> valueList = new ArrayList<ValueToText>();
	
	public String getEmotion() {
		String temp = "情绪指数";
        int count = 0;
		for (ValueToText item : valueList) {
			temp += item.txt;
            count += 1;
            if (count >= 3) break;
		}
		return temp;
	}
	public int getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
	public String getLink() {
		return link;
	}
	public String getPicture() {
		return picture;
	}
	public String getEmotionType() {
		return emotionType;
	}
	public int getLeValue() {
		return leValue;
	}
	public int getHaoValue() {
		return haoValue;
	}
	public int getNuValue() {
		return nuValue;
	}
	public int getAiValue() {
		return aiValue;
	}
	public int getJuValue() {
		return juValue;
	}
	public int getEValue() {
		return eValue;
	}
	public int getJingValue() {
		return jingValue;
	}
	public int getUpdateTime() {
		return updateTime;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public void setPicture(String picture) {
		this.picture = picture;
	}
	public void setEmotionType(String emotionType) {
		this.emotionType = emotionType;
	}
	public void setLeValue(int leValue) {
		this.leValue = leValue;
		if (leValue != 0) {
			String txt = "\n乐:" + Integer.toString(leValue);
			addList(leValue, txt);
		}
	}
	public void setHaoValue(int haoValue) {
		this.haoValue = haoValue;
		if (haoValue != 0) {
			String txt = "\n好:" + Integer.toString(haoValue);
			addList(haoValue, txt);
		}
	}
	public void setNuValue(int nuValue) {
		this.nuValue = nuValue;
		if (nuValue != 0) {
			String txt = "\n怒:" + Integer.toString(nuValue);
			addList(nuValue, txt);
		}
	}
	public void setAiValue(int aiValue) {
		this.aiValue = aiValue;
		if (aiValue != 0) {
			String txt = "\n哀:" + Integer.toString(aiValue);
			addList(aiValue, txt);
		}
	}
	public void setJuValue(int juValue) {
		this.juValue = juValue;
		if (juValue != 0) {
			String txt = "\n惧:" + Integer.toString(juValue);
			addList(juValue, txt);
		}
	}
	public void setEValue(int eValue) {
		this.eValue = eValue;
		if (eValue != 0) {
			String txt = "\n恶:" + Integer.toString(eValue);
			addList(eValue, txt);
		}
	}
	public void setJingValue(int jingValue) {
		this.jingValue = jingValue;
		if (jingValue != 0) {
			String txt = "\n惊:" + Integer.toString(jingValue);
			addList(jingValue, txt);
		}
	}
	public void setUpdateTime(int updateTime) {
		this.updateTime = updateTime;
	}
	private void addList(int vl, String tx) {
		ValueToText item = new ValueToText(vl, tx);
		for (int i = 0; i < valueList.size(); i++) {
			if (valueList.get(i).value < vl) {
				valueList.add(i, item);
				return;
			}
		}
		valueList.add(item);
	}
	private class ValueToText {
		public ValueToText(int vl, String tx) {
			value = vl;
			txt = tx;
		}
		int value;
		String txt;
	}

}

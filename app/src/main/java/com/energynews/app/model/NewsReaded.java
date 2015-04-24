package com.energynews.app.model;

/**
 * Created by aoshi on 15-4-22.
 */
public class NewsReaded {

    private final static String DEBUG_TAG = "NewsReaded";
    private int id;
    private String link;

    public void setId(int id) {
        this.id = id;
    }
    public void setLink(String link) {
        this.link = link;
    }

    public int getId() {
        return id;
    }
    public String getLink() {
        return link;
    }
}

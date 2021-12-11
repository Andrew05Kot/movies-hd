package com.kinofilm.kino.movie.models;

import java.io.Serializable;

public class News implements Serializable {

    public int id;
    public long nid = -1;
    public String news_title = "";
    public String category_name = "";
    public String news_date = "";
    public String news_image = "";
    public String news_description = "";
    public String content_type = "";
    public String video_url = "";
    public String video_id = "";
    public long comments_count = -1;

    public News(long nid, String news_title, String category_name, String news_date, String news_image, String news_description, String content_type, String video_url, String video_id, long comments_count) {
        this.nid = nid;
        this.news_title = news_title;
        this.category_name = category_name;
        this.news_date = news_date;
        this.news_image = news_image;
        this.news_description = news_description;
        this.content_type = content_type;
        this.video_url = video_url;
        this.video_id = video_id;
        this.comments_count = comments_count;
    }

    public News() {
    }

    public News(long nid) {
        this.nid = nid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getNid() {
        return nid;
    }

    public void setNid(long nid) {
        this.nid = nid;
    }

    public String getNews_title() {
        return news_title;
    }

    public void setNews_title(String news_title) {
        this.news_title = news_title;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getNews_date() {
        return news_date;
    }

    public void setNews_date(String news_date) {
        this.news_date = news_date;
    }

    public String getNews_image() {
        return news_image;
    }

    public void setNews_image(String news_image) {
        this.news_image = news_image;
    }

    public String getNews_description() {
        return news_description;
    }

    public void setNews_description(String news_description) {
        this.news_description = news_description;
    }

    public String getContent_type() {
        return content_type;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }

    public long getComments_count() {
        return comments_count;
    }

    public void setComments_count(long comments_count) {
        this.comments_count = comments_count;
    }

}

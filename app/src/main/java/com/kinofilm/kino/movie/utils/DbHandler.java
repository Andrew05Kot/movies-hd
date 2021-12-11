package com.kinofilm.kino.movie.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kinofilm.kino.movie.models.News;

import java.util.ArrayList;
import java.util.List;

public class DbHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "db_android_news_app";
    private static final String TABLE_NAME = "tbl_favorite";
    private static final String KEY_ID = "id";
    private static final String NOADS = "noads";

    private static final String KEY_NID = "nid";
    private static final String KEY_NEWS_TITLE = "news_title";
    private static final String KEY_CATEGORY_NAME = "category_name";
    private static final String KEY_NEWS_DATE = "news_date";
    private static final String KEY_NEWS_IMAGE = "news_image";
    private static final String KEY_NEWS_DESCRIPTION = "news_description";
    private static final String KEY_CONTENT_TYPE = "content_type";
    private static final String KEY_VIDEO_URL = "video_url";
    private static final String KEY_VIDEO_ID = "video_id";
    private static final String KEY_COMMENTS_COUNT = "comments_count";


    public DbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NID + " INTEGER,"
                + KEY_NEWS_TITLE + " TEXT,"
                + KEY_CATEGORY_NAME + " TEXT,"
                + KEY_NEWS_DATE + " TEXT,"
                + KEY_NEWS_IMAGE + " TEXT,"
                + KEY_NEWS_DESCRIPTION + " TEXT,"
                + KEY_CONTENT_TYPE + " TEXT,"
                + KEY_VIDEO_URL + " TEXT,"
                + KEY_VIDEO_ID + " TEXT,"
                + KEY_COMMENTS_COUNT + " INTEGER"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);

    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    //Adding Record in Database

    public void AddtoFavorite(News pj) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NID, pj.getNid());
        values.put(KEY_NEWS_TITLE, pj.getNews_title());
        values.put(KEY_CATEGORY_NAME, pj.getCategory_name());
        values.put(KEY_NEWS_DATE, pj.getNews_date());
        values.put(KEY_NEWS_IMAGE, pj.getNews_image());
        values.put(KEY_NEWS_DESCRIPTION, pj.getNews_description());
        values.put(KEY_CONTENT_TYPE, pj.getContent_type());
        values.put(KEY_VIDEO_URL, pj.getVideo_url());
        values.put(KEY_VIDEO_ID, pj.getVideo_id());
        values.put(KEY_COMMENTS_COUNT, pj.getComments_count());

        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection

    }

    // Getting All Data
    public List<News> getAllData() {
        List<News> dataList = new ArrayList<News>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY id DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                News contact = new News();
                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setNid(Integer.parseInt(cursor.getString(1)));
                contact.setNews_title(cursor.getString(2));
                contact.setCategory_name(cursor.getString(3));
                contact.setNews_date(cursor.getString(4));
                contact.setNews_image(cursor.getString(5));
                contact.setNews_description(cursor.getString(6));
                contact.setContent_type(cursor.getString(7));
                contact.setVideo_url(cursor.getString(8));
                contact.setVideo_id(cursor.getString(9));
                contact.setComments_count(Integer.parseInt(cursor.getString(10)));

                // Adding contact to list
                dataList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return dataList;
    }

    //getting single row
    public List<News> getFavRow(long id) {
        List<News> dataList = new ArrayList<News>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE nid=" + id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                News contact = new News();
                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setNid(Integer.parseInt(cursor.getString(1)));
                contact.setNews_title(cursor.getString(2));
                contact.setCategory_name(cursor.getString(3));
                contact.setNews_date(cursor.getString(4));
                contact.setNews_image(cursor.getString(5));
                contact.setNews_description(cursor.getString(6));
                contact.setContent_type(cursor.getString(7));
                contact.setVideo_url(cursor.getString(8));
                contact.setVideo_id(cursor.getString(9));
                contact.setComments_count(Integer.parseInt(cursor.getString(10)));

                // Adding contact to list
                dataList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return dataList;
    }

    //for remove favorite
    public void RemoveFav(News contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_NID + " = ?",
                new String[]{String.valueOf(contact.getNid())});
        db.close();
    }

    public enum DatabaseManager {
        INSTANCE;
        private SQLiteDatabase db;
        private boolean isDbClosed = true;
        DbHandler dbHelper;

        public void init(Context context) {
            dbHelper = new DbHandler(context);
            if (isDbClosed) {
                isDbClosed = false;
                this.db = dbHelper.getWritableDatabase();
            }

        }

        public boolean isDatabaseClosed() {
            return isDbClosed;
        }

        public void closeDatabase() {
            if (!isDbClosed && db != null) {
                isDbClosed = true;
                db.close();
                dbHelper.close();
            }
        }
    }

}

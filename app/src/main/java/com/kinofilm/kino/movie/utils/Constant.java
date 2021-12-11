package com.kinofilm.kino.movie.utils;

import com.kinofilm.kino.movie.Config;

import static com.kinofilm.kino.movie.Config.ADMIN_PANEL_URL;

public class Constant {

    public static final String REGISTER_URL = ADMIN_PANEL_URL + "/api/user_register/?user_type=normal&name=";
    public static final String NORMAL_LOGIN_URL = ADMIN_PANEL_URL + "/api/get_user_login/?email=";
    public static final String PROFILE_URL = ADMIN_PANEL_URL + "/api/get_user_profile/?id=";
    public static final String PROFILE_UPDATE_URL = ADMIN_PANEL_URL + "/api/update_user_profile/?user_id=";
    public static final String FORGET_PASSWORD_URL = ADMIN_PANEL_URL + "/api/forgot_password/?email=";
    public static final String CATEGORY_ARRAY_NAME = "result";
    public static int GET_SUCCESS_MSG;
    public static final String MSG = "msg";
    public static final String SUCCESS = "success";
    public static final String USER_NAME = "name";
    public static final String USER_ID = "user_id";
    public static final long DELAY_REFRESH = 1000;
    public static final int DELAY_PROGRESS_DIALOG = 2000;

    public static long DELAY_TIME = 100;
    public static long DELAY_TIME_MEDIUM = 500;
    public static final String YOUTUBE_IMG_FRONT = "https://img.youtube.com/vi/";
    public static final String YOUTUBE_IMG_BACK = "/mqdefault.jpg";
    public static int MAX_SEARCH_RESULT = 50;

    public static final String TOKEN_URL = Config.ADMIN_PANEL_URL + "/register.php";
    public static final String TOPIC_GLOBAL = "global";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";
    public static final String SHARED_PREF = "ah_firebase";
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

}
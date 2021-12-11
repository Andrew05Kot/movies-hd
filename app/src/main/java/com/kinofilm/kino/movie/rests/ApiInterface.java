package com.kinofilm.kino.movie.rests;

import com.kinofilm.kino.movie.Config;
import com.kinofilm.kino.movie.callbacks.CallbackCategories;
import com.kinofilm.kino.movie.callbacks.CallbackCategoryDetails;
import com.kinofilm.kino.movie.callbacks.CallbackComments;
import com.kinofilm.kino.movie.callbacks.CallbackNewsDetail;
import com.kinofilm.kino.movie.callbacks.CallbackRecent;
import com.kinofilm.kino.movie.callbacks.CallbackUser;
import com.kinofilm.kino.movie.models.Setting;
import com.kinofilm.kino.movie.models.Value;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "Data-Agent: Android News App";

    @Headers({CACHE, AGENT})
    @GET("api/get_post_detail")
    Call<CallbackNewsDetail> getPostDetail(
            @Query("id") long id
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_recent_posts/?api_key=" + Config.API_KEY)
    Call<CallbackRecent> getRecentPost(
            @Query("page") int page,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_video_posts/?api_key=" + Config.API_KEY)
    Call<CallbackRecent> getVideoPost(
            @Query("page") int page,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_category_index/?api_key=" + Config.API_KEY)
    Call<CallbackCategories> getAllCategories();

    @Headers({CACHE, AGENT})
    @GET("api/get_category_posts" + "&api_key=" + Config.API_KEY)
    Call<CallbackCategoryDetails> getCategoryDetailsByPage(
            @Query("id") long id,
            @Query("page") long page,
            @Query("count") long count
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_search_results/?api_key=" + Config.API_KEY)
    Call<CallbackRecent> getSearchPosts(
            @Query("search") String search,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_comments")
    Call<CallbackComments> getComments(@Query("nid") Long nid
    );

    @FormUrlEncoded
    @POST("api/post_comment")
    Call<Value> sendComment(@Field("nid") Long nid,
                            @Field("user_id") String user_id,
                            @Field("content") String content,
                            @Field("date_time") String date_time);

    @FormUrlEncoded
    @POST("api/update_comment")
    Call<Value> updateComment(@Field("comment_id") String comment_id,
                              @Field("date_time") String date_time,
                              @Field("content") String content);

    @FormUrlEncoded
    @POST("api/delete_comment")
    Call<Value> deleteComment(@Field("comment_id") String comment_id);

    @Headers({CACHE, AGENT})
    @GET("api/get_privacy_policy")
    Call<Setting> getPrivacyPolicy();

    @Headers({CACHE, AGENT})
    @GET("api/get_user_token")
    Call<CallbackUser> getUserToken(@Query("user_unique_id") String user_unique_id);

    @Headers({CACHE, AGENT})
    @GET("api/get_package_name")
    Call<Setting> getPackageName();

}

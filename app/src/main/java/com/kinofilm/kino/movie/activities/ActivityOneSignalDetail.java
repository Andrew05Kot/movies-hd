package com.kinofilm.kino.movie.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinofilm.kino.movie.Config;
import com.kinofilm.kino.movie.R;
import com.kinofilm.kino.movie.callbacks.CallbackNewsDetail;
import com.kinofilm.kino.movie.models.News;
import com.kinofilm.kino.movie.rests.ApiInterface;
import com.kinofilm.kino.movie.rests.RestAdapter;
import com.kinofilm.kino.movie.utils.AppBarLayoutBehavior;
import com.kinofilm.kino.movie.utils.Constant;
import com.kinofilm.kino.movie.utils.DbHandler;
import com.kinofilm.kino.movie.utils.NetworkCheck;
import com.kinofilm.kino.movie.utils.Tools;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityOneSignalDetail extends AppCompatActivity {

    TextView txt_title, txt_category, txt_date, txt_comment_count, txt_comment_text;
    ImageView news_image, btn_comment, img_thumb_video;
    private WebView webview;
    long nid;
    private Menu menu;
    View parent_view, lyt_parent;
    News post;
    private Call<CallbackNewsDetail> callbackCall = null;
    DbHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        databaseHandler = new DbHandler(getApplicationContext());

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        initToolbar();

        Intent intent = getIntent();
        nid = intent.getLongExtra("id", 0);

        parent_view = findViewById(android.R.id.content);
        lyt_parent = findViewById(R.id.lyt_parent);
        txt_title = findViewById(R.id.title);
        txt_date = findViewById(R.id.date);
        news_image = findViewById(R.id.image);
        img_thumb_video = findViewById(R.id.thumbnail_video);

        requestAction();
    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    private void requestAction() {
        showFailedView(false, "");
        requestDetailsPostApi();
    }

    private void requestDetailsPostApi() {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getPostDetail(nid);
        callbackCall.enqueue(new Callback<CallbackNewsDetail>() {
            @Override
            public void onResponse(Call<CallbackNewsDetail> call, Response<CallbackNewsDetail> response) {
                CallbackNewsDetail resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    post = resp.post;
                    if (Config.ENABLE_RTL_MODE) {
                        displayDataRTL();
                    } else {
                        displayData();
                    }
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<CallbackNewsDetail> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }

        });
    }

    private void onFailRequest() {
        if (NetworkCheck.isConnect(this)) {
            showFailedView(true, getString(R.string.msg_no_network));
        } else {
            showFailedView(true, getString(R.string.msg_offline));
        }
    }

    private void displayData() {
        txt_title.setText(Html.fromHtml(post.news_title));
        txt_comment_count.setText("" + post.comments_count);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (post.comments_count == 0) {
                    txt_comment_text.setText(R.string.txt_no_comment);
                }
                if (post.comments_count == 1) {
                    txt_comment_text.setText(getResources().getString(R.string.txt_read) + " " + post.comments_count + " " + getResources().getString(R.string.txt_comment));
                } else if (post.comments_count > 1) {
                    txt_comment_text.setText(getResources().getString(R.string.txt_read) + " " + post.comments_count + " " + getResources().getString(R.string.txt_comments));
                }
            }
        }, 1000);

        webview.setBackgroundColor(Color.parseColor("#ffffff"));
        webview.getSettings().setDefaultTextEncodingName("UTF-8");
        webview.setFocusableInTouchMode(false);
        webview.setFocusable(false);

        if (!Config.ENABLE_TEXT_SELECTION) {
            webview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });
            webview.setLongClickable(false);
        }

        webview.getSettings().setJavaScriptEnabled(true);

        WebSettings webSettings = webview.getSettings();
        Resources res = getResources();
        int fontSize = res.getInteger(R.integer.font_size);
        webSettings.setDefaultFontSize(fontSize);

        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";
        String htmlText = post.news_description;

        String text = "<html><head>"
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + "<style type=\"text/css\">body{color: #000000;}"
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Config.OPEN_LINK_INSIDE_APP) {
                    if (url.startsWith("http://")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                    }
                    if (url.startsWith("https://")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                    }
                    if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebViewImage.class);
                        intent.putExtra("image_url", url);
                        startActivity(intent);
                    }
                    if (url.endsWith(".pdf")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
                return true;
            }
        });

        webview.loadData(text, mimeType, encoding);

        txt_category.setText(post.category_name);
        txt_category.setBackgroundColor(ContextCompat.getColor(this, R.color.colorCategory));

        if (Config.ENABLE_DATE_DISPLAY) {
            txt_date.setVisibility(View.VISIBLE);
        } else {
            txt_date.setVisibility(View.GONE);
        }
        txt_date.setText(Tools.getFormatedDate(post.news_date));

        if (post.content_type != null && post.content_type.equals("youtube")) {
            Picasso.get()
                    .load(Constant.YOUTUBE_IMG_FRONT + post.video_id + Constant.YOUTUBE_IMG_BACK)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ActivityYoutubePlayer.class);
                    intent.putExtra("video_id", post.video_id);
                    startActivity(intent);
                }
            });

        } else if (post.content_type != null && post.content_type.equals("Url")) {

            Picasso.get()
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + post.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                    intent.putExtra("video_url", post.video_url);
                    startActivity(intent);
                }
            });
        } else if (post.content_type != null && post.content_type.equals("Upload")) {

            Picasso.get()
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + post.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                    intent.putExtra("video_url", Config.ADMIN_PANEL_URL + "/upload/video/" + post.video_url);
                    startActivity(intent);
                }
            });
        } else {
            Picasso.get()
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + post.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), ActivityFullScreenImage.class);
                    intent.putExtra("image", post.news_image);
                    startActivity(intent);
                }
            });
        }

        if (!post.content_type.equals("Post")) {
            img_thumb_video.setVisibility(View.VISIBLE);
        } else {
            img_thumb_video.setVisibility(View.GONE);
        }

        btn_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        txt_comment_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

    }

    private void displayDataRTL() {
        txt_title.setText(Html.fromHtml(post.news_title));
        txt_comment_count.setText("" + post.comments_count);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (post.comments_count == 0) {
                    txt_comment_text.setText(R.string.txt_no_comment);
                }
                if (post.comments_count == 1) {
                    txt_comment_text.setText(getResources().getString(R.string.txt_read) + " " + post.comments_count + " " + getResources().getString(R.string.txt_comment));
                } else if (post.comments_count > 1) {
                    txt_comment_text.setText(getResources().getString(R.string.txt_read) + " " + post.comments_count + " " + getResources().getString(R.string.txt_comments));
                }
            }
        }, 1000);

        webview.setBackgroundColor(Color.parseColor("#ffffff"));
        webview.getSettings().setDefaultTextEncodingName("UTF-8");
        webview.setFocusableInTouchMode(false);
        webview.setFocusable(false);

        if (!Config.ENABLE_TEXT_SELECTION) {
            webview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });
            webview.setLongClickable(false);
        }

        webview.getSettings().setJavaScriptEnabled(true);

        WebSettings webSettings = webview.getSettings();
        Resources res = getResources();
        int fontSize = res.getInteger(R.integer.font_size);
        webSettings.setDefaultFontSize(fontSize);

        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";
        String htmlText = post.news_description;

        String text = "<html dir='rtl'><head>"
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + "<style type=\"text/css\">body{color: #000000;}"
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Config.OPEN_LINK_INSIDE_APP) {
                    if (url.startsWith("http://")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                    }
                    if (url.startsWith("https://")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                    }
                    if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebViewImage.class);
                        intent.putExtra("image_url", url);
                        startActivity(intent);
                    }
                    if (url.endsWith(".pdf")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
                return true;
            }
        });

        webview.loadData(text, mimeType, encoding);

        txt_category.setText(post.category_name);
        txt_category.setBackgroundColor(ContextCompat.getColor(this, R.color.colorCategory));

        if (Config.ENABLE_DATE_DISPLAY) {
            txt_date.setVisibility(View.VISIBLE);
        } else {
            txt_date.setVisibility(View.GONE);
        }
        txt_date.setText(Tools.getFormatedDate(post.news_date));

        if (post.content_type != null && post.content_type.equals("youtube")) {
            Picasso.get()
                    .load(Constant.YOUTUBE_IMG_FRONT + post.video_id + Constant.YOUTUBE_IMG_BACK)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ActivityYoutubePlayer.class);
                    intent.putExtra("video_id", post.video_id);
                    startActivity(intent);
                }
            });

        } else if (post.content_type != null && post.content_type.equals("Url")) {

            Picasso.get()
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + post.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                    intent.putExtra("video_url", post.video_url);
                    startActivity(intent);
                }
            });
        } else if (post.content_type != null && post.content_type.equals("Upload")) {

            Picasso.get()
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + post.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                    intent.putExtra("video_url", Config.ADMIN_PANEL_URL + "/upload/video/" + post.video_url);
                    startActivity(intent);
                }
            });
        } else {
            Picasso.get()
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + post.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), ActivityFullScreenImage.class);
                    intent.putExtra("image", post.news_image);
                    startActivity(intent);
                }
            });
        }

        if (!post.content_type.equals("Post")) {
            img_thumb_video.setVisibility(View.VISIBLE);
        } else {
            img_thumb_video.setVisibility(View.GONE);
        }

        btn_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        txt_comment_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news_detail, menu);
        this.menu = menu;
        addToFavorite();
        return true;
    }

    public void addToFavorite() {
        List<News> data = databaseHandler.getFavRow(nid);
        if (data.size() == 0) {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_outline_white));
        } else {
            if (data.get(0).getNid() == nid) {
                menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_white));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_later:

                List<News> data = databaseHandler.getFavRow(nid);
                if (data.size() == 0) {
                    databaseHandler.AddtoFavorite(new News(
                            post.nid,
                            post.news_title,
                            post.category_name,
                            post.news_date,
                            post.news_image,
                            post.news_description,
                            post.content_type,
                            post.video_url,
                            post.video_id,
                            post.comments_count
                    ));
                    Snackbar.make(parent_view, R.string.favorite_added, Snackbar.LENGTH_SHORT).show();
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_white));

                } else {
                    if (data.get(0).getNid() == nid) {
                        databaseHandler.RemoveFav(new News(nid));
                        Snackbar.make(parent_view, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show();
                        menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_outline_white));
                    }
                }

                break;

            case R.id.action_share:

                String formattedString = Html.fromHtml(post.news_description).toString();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, post.news_title + "\n" + formattedString + "\n" + getResources().getString(R.string.share_content) + "https://play.google.com/store/apps/details?id=" + getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed_home);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

}

package com.kinofilm.kino.movie.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinofilm.kino.movie.Config;
import com.kinofilm.kino.movie.R;
import com.kinofilm.kino.movie.adapter.AdapterRecent;
import com.kinofilm.kino.movie.callbacks.CallbackCategoryDetails;
import com.kinofilm.kino.movie.models.Category;
import com.kinofilm.kino.movie.models.News;
import com.kinofilm.kino.movie.rests.ApiInterface;
import com.kinofilm.kino.movie.rests.RestAdapter;
import com.kinofilm.kino.movie.utils.Constant;
import com.kinofilm.kino.movie.utils.NetworkCheck;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityCategoryDetails extends AppCompatActivity {

    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";

    // give preparation animation activity transition
    public static void navigate(AppCompatActivity activity, View transitionView, Category obj) {
        Intent intent = new Intent(activity, ActivityCategoryDetails.class);
        intent.putExtra(EXTRA_OBJC, obj);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, EXTRA_OBJC);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    private Toolbar toolbar;
    private ActionBar actionBar;
    private RecyclerView recyclerView;
    private AdapterRecent mAdapter;
    private SwipeRefreshLayout swipe_refresh;
    private Call<CallbackCategoryDetails> callbackCall = null;
    private Category category;
    private View parent_view;
    private long post_total = 0;
    private long failed_page = 0;
    TextView txt_title_toolbar;
    BroadcastReceiver broadcastReceiver;
    int counter = 1;
    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_details);
        parent_view = findViewById(android.R.id.content);

        loadText();

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }
        loadText();
        onReceiveNotification();

        // animation transition
        ViewCompat.setTransitionName(findViewById(R.id.txt_title_toolbar), EXTRA_OBJC);

        // get extra object
        category = (Category) getIntent().getSerializableExtra(EXTRA_OBJC);
        post_total = category.post_count;

        swipe_refresh = findViewById(R.id.swipe_refresh_layout);
        swipe_refresh.setColorSchemeResources(R.color.colorOrange, R.color.colorGreen, R.color.colorBlue, R.color.colorRed);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        //set data and list adapter
        mAdapter = new AdapterRecent(this, recyclerView, new ArrayList<News>());
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterRecent.OnItemClickListener() {
            @Override
            public void onItemClick(View v, News obj, int position) {
                ActivityNewsDetail.navigate(ActivityCategoryDetails.this, v.findViewById(R.id.image), obj);
            }
        });

        // detect when scroll reach bottom
        mAdapter.setOnLoadMoreListener(new AdapterRecent.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (post_total > mAdapter.getItemCount() && current_page != 0) {
                    int next_page = current_page + 1;
                    requestAction(next_page);
                } else {
                    mAdapter.setLoaded();
                }
            }
        });

        // on swipe list
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (callbackCall != null && callbackCall.isExecuted()) {
                    callbackCall.cancel();
                }
                mAdapter.resetListData();
                requestAction(1);
            }
        });

        requestAction(1);

        initToolbar();
        txt_title_toolbar = findViewById(R.id.txt_title_toolbar);
        txt_title_toolbar.setText(category.category_name);

    }

    private void loadText() {
        sPref = getSharedPreferences("PREFS", MODE_PRIVATE);
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.search:
                Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                startActivity(intent);
                return true;

            case  R.id.no_ads:
                loadText();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void displayApiResult(final List<News> posts) {
        mAdapter.insertData(posts);
        swipeProgress(false);
        if (posts.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestPostApi(final long page_no) {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getCategoryDetailsByPage(category.cid, page_no, Config.LOAD_MORE);
        callbackCall.enqueue(new Callback<CallbackCategoryDetails>() {
            @Override
            public void onResponse(@NonNull Call<CallbackCategoryDetails> call, @NonNull Response<CallbackCategoryDetails> response) {
                CallbackCategoryDetails resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    displayApiResult(resp.posts);
                } else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CallbackCategoryDetails> call, @NonNull Throwable t) {
                if (!call.isCanceled()) onFailRequest(page_no);
            }

        });
    }

    private void onFailRequest(long page_no) {
        failed_page = page_no;
        mAdapter.setLoaded();
        swipeProgress(false);
        if (NetworkCheck.isConnect(getApplicationContext())) {
            showFailedView(true, getString(R.string.msg_no_network));
        } else {
            showFailedView(true, getString(R.string.msg_offline));
        }
    }

    private void requestAction(final long page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            mAdapter.setLoading();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestPostApi(page_no);
            }
        }, Constant.DELAY_TIME);
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction(failed_page);
            }
        });
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.msg_no_news);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipe_refresh.setRefreshing(show);
            return;
        }
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(show);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
    }

    public void onReceiveNotification() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // checking for type intent filter
                if (intent.getAction().equals(Constant.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    final long id = intent.getLongExtra("id", 1L);
                    final String title = intent.getStringExtra("title");
                    final String message = intent.getStringExtra("message");
                    final String image_url = intent.getStringExtra("image_url");
                    final String url = intent.getStringExtra("link");

                    LayoutInflater layoutInflaterAndroid = LayoutInflater.from(ActivityCategoryDetails.this);
                    View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog, null);

                    final AlertDialog.Builder alert = new AlertDialog.Builder(ActivityCategoryDetails.this);
                    alert.setView(mView);

                    final TextView notification_title = mView.findViewById(R.id.title);
                    final TextView notification_message = mView.findViewById(R.id.message);
                    final ImageView notification_image = mView.findViewById(R.id.big_image);

                    if (id != 1L) {
                        if (id == 0) {
                            if (!url.equals("")) {
                                notification_title.setText(title);
                                notification_message.setText(Html.fromHtml(message));
                                Picasso.get()
                                        .load(image_url.replace(" ", "%20"))
                                        .placeholder(R.drawable.ic_thumbnail)
                                        .into(notification_image);
                                alert.setPositiveButton("Open link", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
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
                                    }
                                });
                                alert.setNegativeButton(getResources().getString(R.string.dialog_dismiss), null);
                            } else {
                                notification_title.setText(title);
                                notification_message.setText(Html.fromHtml(message));
                                Picasso.get()
                                        .load(image_url.replace(" ", "%20"))
                                        .placeholder(R.drawable.ic_thumbnail)
                                        .into(notification_image);
                                alert.setPositiveButton(getResources().getString(R.string.dialog_ok), null);
                            }
                        } else {
                            notification_title.setText(title);
                            notification_message.setText(Html.fromHtml(message));
                            Picasso.get()
                                    .load(image_url.replace(" ", "%20"))
                                    .placeholder(R.drawable.ic_thumbnail)
                                    .into(notification_image);

                            alert.setPositiveButton(getResources().getString(R.string.dialog_read_more), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(getApplicationContext(), ActivityFCMDetail.class);
                                    intent.putExtra("id", id);
                                    startActivity(intent);
                                }
                            });
                            alert.setNegativeButton(getResources().getString(R.string.dialog_dismiss), null);
                        }
                        alert.setCancelable(false);
                        alert.show();
                    }

                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(Constant.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(Constant.PUSH_NOTIFICATION));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

}

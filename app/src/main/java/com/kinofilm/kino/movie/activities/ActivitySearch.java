package com.kinofilm.kino.movie.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.kinofilm.kino.movie.Config;
import com.kinofilm.kino.movie.R;
import com.kinofilm.kino.movie.adapter.AdapterNews;
import com.kinofilm.kino.movie.adapter.AdapterSearch;
import com.kinofilm.kino.movie.callbacks.CallbackRecent;
import com.kinofilm.kino.movie.models.News;
import com.kinofilm.kino.movie.rests.ApiInterface;
import com.kinofilm.kino.movie.rests.RestAdapter;
import com.kinofilm.kino.movie.utils.Constant;
import com.kinofilm.kino.movie.utils.NetworkCheck;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySearch extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar actionBar;
    private EditText et_search;

    private RecyclerView recyclerView;
    private AdapterNews mAdapter;

    private RecyclerView recyclerSuggestion;
    private AdapterSearch mAdapterSuggestion;
    private LinearLayout lyt_suggestion;

    private ImageButton bt_clear;
    private ProgressBar progressBar;
    private View parent_view;
    private Call<CallbackRecent> callbackCall = null;
    private SharedPreferences sPref;
    private String adsResult = "false";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        parent_view = findViewById(android.R.id.content);

        loadText();

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        initComponent();
        setupToolbar();
        loadText();

    }

    private void loadText() {
        sPref = getPreferences(MODE_PRIVATE);
    }

    private void initComponent() {
        lyt_suggestion = findViewById(R.id.lyt_suggestion);
        et_search = findViewById(R.id.et_search);
        bt_clear = findViewById(R.id.bt_clear);
        bt_clear.setVisibility(View.GONE);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerSuggestion = findViewById(R.id.recyclerSuggestion);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        recyclerSuggestion.setLayoutManager(new LinearLayoutManager(this));
        recyclerSuggestion.setHasFixedSize(true);

        et_search.addTextChangedListener(textWatcher);

        //set data and list adapter
        mAdapter = new AdapterNews(this, recyclerView, new ArrayList<News>());
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new AdapterNews.OnItemClickListener() {
            @Override
            public void onItemClick(View v, News obj, int position) {
                ActivityNewsDetail.navigate(ActivitySearch.this, v.findViewById(R.id.image), obj);
            }
        });

        //set data and list adapter suggestion
        mAdapterSuggestion = new AdapterSearch(this);
        recyclerSuggestion.setAdapter(mAdapterSuggestion);
        showSuggestionSearch();
        mAdapterSuggestion.setOnItemClickListener(new AdapterSearch.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String viewModel, int pos) {
                et_search.setText(viewModel);
                lyt_suggestion.setVisibility(View.GONE);
                hideKeyboard();
                searchAction();
            }
        });

        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search.setText("");
            }
        });

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard();
                    searchAction();
                    return true;
                }
                return false;
            }
        });

        et_search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showSuggestionSearch();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return false;
            }
        });

    }

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().length() == 0) {
                bt_clear.setVisibility(View.GONE);
            } else {
                bt_clear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void requestSearchApi(final String query) {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getSearchPosts(query, Constant.MAX_SEARCH_RESULT);
        callbackCall.enqueue(new Callback<CallbackRecent>() {
            @Override
            public void onResponse(Call<CallbackRecent> call, Response<CallbackRecent> response) {
                CallbackRecent resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    mAdapter.insertData(resp.posts);
                    if (resp.posts.size() == 0) showNotFoundView(true);
                } else {
                    onFailRequest();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<CallbackRecent> call, Throwable t) {
                onFailRequest();
                progressBar.setVisibility(View.GONE);
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

    private void searchAction() {
        lyt_suggestion.setVisibility(View.GONE);
        showFailedView(false, "");
        showNotFoundView(false);
        final String query = et_search.getText().toString().trim();
        if (!query.equals("")) {
            mAdapterSuggestion.addSearchHistory(query);
            mAdapter.resetListData();
            progressBar.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestSearchApi(query);
                }
            }, Constant.DELAY_TIME);
        } else {
            Toast.makeText(this, R.string.msg_search_input, Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuggestionSearch() {
        mAdapterSuggestion.refreshItems();
        lyt_suggestion.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            Snackbar.make(parent_view, item.getTitle() + " clicked", Snackbar.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
                searchAction();
            }
        });
    }

    private void showNotFoundView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.msg_no_news_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (et_search.length() > 0) {
            et_search.setText("");
        } else {
            super.onBackPressed();
        }
    }

}

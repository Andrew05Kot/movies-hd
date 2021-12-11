package com.kinofilm.kino.movie.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kinofilm.kino.movie.Config;
import com.kinofilm.kino.movie.R;
import com.kinofilm.kino.movie.activities.ActivityNewsDetail;
import com.kinofilm.kino.movie.activities.MainActivity;
import com.kinofilm.kino.movie.adapter.AdapterRecent;
import com.kinofilm.kino.movie.fragments_generator.FragmentsGenerator;
import com.kinofilm.kino.movie.callbacks.CallbackRecent;
import com.kinofilm.kino.movie.models.News;
import com.kinofilm.kino.movie.rests.ApiInterface;
import com.kinofilm.kino.movie.rests.RestAdapter;
import com.kinofilm.kino.movie.utils.Constant;
import com.kinofilm.kino.movie.utils.NetworkCheck;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentRecent extends Fragment {

    private View root_view, parent_view;
    private RecyclerView recyclerView;
    private AdapterRecent mAdapter;
    private SwipeRefreshLayout swipe_refresh;
    private Call<CallbackRecent> callbackCall = null;
    private int post_total = 0;
    private int failed_page = 0;
    int counter = 1;
    private SharedPreferences sPref;
    private String adsResult = "false";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_home, null);
        parent_view = getActivity().findViewById(R.id.main_content);
        loadText();
        if (adsResult.equals("true")){
            Log.d("TAGING", "FragmentRecent NOADS");
            //Toast.makeText(getActivity(), getString(R.string.title_ads_off), Toast.LENGTH_LONG).show();
        }
        else{
            Log.d("TAGING", "FragmentRecent ADS");
        }

        swipe_refresh = root_view.findViewById(R.id.swipe_refresh_layout_home);
        swipe_refresh.setColorSchemeResources(R.color.colorOrange, R.color.colorGreen, R.color.colorBlue, R.color.colorRed);
        recyclerView = root_view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        //set data and list adapter
        mAdapter = new AdapterRecent(getActivity(), recyclerView, new ArrayList<News>());
        FragmentsGenerator admobNativeAdAdapter = FragmentsGenerator.Builder
                .with(
                        mAdapter,
                        "small"
                )
                .adItemInterval(8)
                .build();


        //recyclerView.setAdapter(mAdapter);
        recyclerView.setAdapter(admobNativeAdAdapter);

        if (Config.ENABLE_RTL_MODE) {
            recyclerView.setRotationY(180);
        }

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterRecent.OnItemClickListener() {
            @Override
            public void onItemClick(View v, News obj, int position) {
                ActivityNewsDetail.navigate((MainActivity) getActivity(), v.findViewById(R.id.image), obj);
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
                if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
                mAdapter.resetListData();
                requestAction(1);
            }
        });

        requestAction(1);

        return root_view;
    }

    private void loadText() {
        sPref = getActivity().getPreferences(getActivity().MODE_PRIVATE);
    }

    private void displayApiResult(final List<News> posts) {
        mAdapter.insertData(posts);
        swipeProgress(false);
        if (posts.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestListPostApi(final int page_no) {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getRecentPost(page_no, Config.LOAD_MORE);
        callbackCall.enqueue(new Callback<CallbackRecent>() {
            @Override
            public void onResponse(Call<CallbackRecent> call, Response<CallbackRecent> response) {
                CallbackRecent resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    post_total = resp.count_total;
                    displayApiResult(resp.posts);
                } else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void onFailure(Call<CallbackRecent> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest(page_no);
            }

        });
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        mAdapter.setLoaded();
        swipeProgress(false);
        if (NetworkCheck.isConnect(getActivity())) {
            showFailedView(true, getString(R.string.msg_no_network));
        } else {
            showFailedView(true, getString(R.string.msg_offline));
        }
    }

    private void requestAction(final int page_no) {
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
                requestListPostApi(page_no);
            }
        }, Constant.DELAY_TIME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = root_view.findViewById(R.id.lyt_failed_home);
        ((TextView) root_view.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        root_view.findViewById(R.id.failed_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction(failed_page);
            }
        });
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = root_view.findViewById(R.id.lyt_no_item_home);
        ((TextView) root_view.findViewById(R.id.no_item_message)).setText(R.string.msg_no_news);
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

}


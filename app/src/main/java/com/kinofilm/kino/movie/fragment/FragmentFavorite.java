package com.kinofilm.kino.movie.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinofilm.kino.movie.Config;
import com.kinofilm.kino.movie.R;
import com.kinofilm.kino.movie.activities.ActivityNewsDetail;
import com.kinofilm.kino.movie.activities.MainActivity;
import com.kinofilm.kino.movie.adapter.AdapterNews;
import com.kinofilm.kino.movie.models.News;
import com.kinofilm.kino.movie.utils.DbHandler;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavorite extends Fragment {

    private List<News> data = new ArrayList<News>();
    private View root_view, parent_view;
    private RecyclerView recyclerView;
    private AdapterNews mAdapter;
    private MainActivity mainActivity;
    LinearLayout lyt_root;
    DbHandler databaseHandler;

    public FragmentFavorite() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_favorite, null);
        parent_view = getActivity().findViewById(R.id.main_content);
        lyt_root = root_view.findViewById(R.id.root_layout);

        recyclerView = root_view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        if (Config.ENABLE_RTL_MODE) {
            lyt_root.setRotationY(180);
        }

        return root_view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();

        databaseHandler = new DbHandler(getActivity());
        data = databaseHandler.getAllData();

        //set data and list adapter
        mAdapter = new AdapterNews(getActivity(), recyclerView, data);
        recyclerView.setAdapter(mAdapter);

        if (data.size() == 0) {
            showNoItemView(true);
        } else {
            showNoItemView(false);
        }

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterNews.OnItemClickListener() {
            @Override
            public void onItemClick(View v, News obj, int position) {
                ActivityNewsDetail.navigate((MainActivity) getActivity(), v.findViewById(R.id.image), obj);
            }
        });

    }

    private void showNoItemView(boolean show) {
        if (show) {
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}

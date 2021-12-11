package com.kinofilm.kino.movie.adapter;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinofilm.kino.movie.Config;
import com.kinofilm.kino.movie.R;
import com.kinofilm.kino.movie.models.News;
import com.kinofilm.kino.movie.utils.Constant;
import com.kinofilm.kino.movie.utils.Tools;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdapterRecent extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_PROG = 0;
    private final int VIEW_HEAD = 1;
    private final int VIEW_ITEM = 2;

    private List<News> items = new ArrayList<>();

    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, News obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterRecent(Context context, RecyclerView view, List<News> items) {
        this.items = items;
        ctx = context;
        lastItemViewDetector(view);
    }

    public class HeadingViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView title;
        public ImageView ic_date;
        public TextView date;
        public TextView category;
        public TextView comment;
        public ImageView image;
        public ImageView thumbnail_video;
        public RelativeLayout lyt_parent;

        public HeadingViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            ic_date = v.findViewById(R.id.ic_date);
            date = v.findViewById(R.id.date);
            category = v.findViewById(R.id.category_name);
            comment = v.findViewById(R.id.comment);
            image = v.findViewById(R.id.image);
            thumbnail_video = v.findViewById(R.id.thumbnail_video);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView title;
        public ImageView ic_date;
        public TextView date;
        public TextView category;
        public TextView comment;
        public ImageView image;
        public ImageView thumbnail_video;
        public LinearLayout lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            ic_date = v.findViewById(R.id.ic_date);
            date = v.findViewById(R.id.date);
            category = v.findViewById(R.id.category_name);
            comment = v.findViewById(R.id.comment);
            image = v.findViewById(R.id.image);
            thumbnail_video = v.findViewById(R.id.thumbnail_video);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }


    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar1);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_recent, parent, false);
            vh = new OriginalViewHolder(v);
        } else if (viewType == VIEW_HEAD) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_heading, parent, false);
            vh = new HeadingViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_load_more, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof HeadingViewHolder) {
            final News p = items.get(position);
            HeadingViewHolder vItem = (HeadingViewHolder) holder;
            vItem.title.setText(Html.fromHtml(p.news_title));

            if (Config.ENABLE_DATE_DISPLAY) {
                vItem.date.setVisibility(View.VISIBLE);
                vItem.ic_date.setVisibility(View.VISIBLE);
            } else {
                vItem.date.setVisibility(View.GONE);
                vItem.ic_date.setVisibility(View.GONE);
            }

            if (Config.DATE_DISPLAY_AS_TIME_AGO) {
                PrettyTime prettyTime = new PrettyTime();
                long timeAgo = Tools.timeStringtoMilis(p.news_date);
                vItem.date.setText(prettyTime.format(new Date(timeAgo)));
            } else {
                vItem.date.setText(Tools.getFormatedDateSimple(p.news_date));
            }

            vItem.category.setText(Html.fromHtml(p.news_description));

            vItem.comment.setText(p.comments_count + "");

            if (p.content_type != null && p.content_type.equals("Post")) {
                vItem.thumbnail_video.setVisibility(View.GONE);
            } else {
                vItem.thumbnail_video.setVisibility(View.VISIBLE);
            }

            if (p.content_type != null && p.content_type.equals("youtube")) {
                Picasso.get()
                        .load(Constant.YOUTUBE_IMG_FRONT + p.video_id + Constant.YOUTUBE_IMG_BACK)
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(vItem.image);
            } else {
                Picasso.get()
                        .load(Config.ADMIN_PANEL_URL + "/upload/" + p.news_image.replace(" ", "%20"))
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(vItem.image);
            }

            vItem.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, p, position);
                    }
                }
            });

        } else if (holder instanceof OriginalViewHolder) {
            final News p = items.get(position);
            OriginalViewHolder vItem = (OriginalViewHolder) holder;
            vItem.title.setText(Html.fromHtml(p.news_title));

            if (Config.ENABLE_DATE_DISPLAY) {
                vItem.date.setVisibility(View.VISIBLE);
                vItem.ic_date.setVisibility(View.VISIBLE);
            } else {
                vItem.date.setVisibility(View.GONE);
                vItem.ic_date.setVisibility(View.GONE);
            }

            if (Config.DATE_DISPLAY_AS_TIME_AGO) {
                PrettyTime prettyTime = new PrettyTime();
                long timeAgo = Tools.timeStringtoMilis(p.news_date);
                vItem.date.setText(prettyTime.format(new Date(timeAgo)));
            } else {
                vItem.date.setText(Tools.getFormatedDateSimple(p.news_date));
            }

            vItem.category.setText(Html.fromHtml(p.news_description));

            vItem.comment.setText(p.comments_count + "");

            if (p.content_type != null && p.content_type.equals("Post")) {
                vItem.thumbnail_video.setVisibility(View.GONE);
            } else {
                vItem.thumbnail_video.setVisibility(View.VISIBLE);
            }

            if (p.content_type != null && p.content_type.equals("youtube")) {
                Picasso.get()
                        .load(Constant.YOUTUBE_IMG_FRONT + p.video_id + Constant.YOUTUBE_IMG_BACK)
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(vItem.image);
            } else {
                Picasso.get()
                        .load(Config.ADMIN_PANEL_URL + "/upload/" + p.news_image.replace(" ", "%20"))
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(vItem.image);
            }

            vItem.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, p, position);
                    }
                }
            });
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        //return this.items.get(position) != null ? VIEW_ITEM : VIEW_PROG;
        if (items.get(position) != null) {
            if (position == 0) {
                return VIEW_HEAD;
            } else {
                return VIEW_ITEM;
            }
        } else {
            return VIEW_PROG;
        }
    }

    public void insertData(List<News> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void resetListData() {
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = layoutManager.findLastVisibleItemPosition();
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        if (onLoadMoreListener != null) {
                            int current_page = getItemCount() / Config.LOAD_MORE;
                            onLoadMoreListener.onLoadMore(current_page);
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

}
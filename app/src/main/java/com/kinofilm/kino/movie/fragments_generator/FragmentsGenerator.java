package com.kinofilm.kino.movie.fragments_generator;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.kinofilm.kino.movie.R;

public class FragmentsGenerator extends RecyclerViewAdapterWrapper {

    private static final int TYPE_FB_NATIVE_ADS = 900;
    private static final int DEFAULT_AD_ITEM_INTERVAL = 4;

    private final FragmentsGenerator.Param mParam;

    private FragmentsGenerator(FragmentsGenerator.Param param) {
        super(param.adapter);
        this.mParam = param;

        assertConfig();
        setSpanAds();
    }

    private void assertConfig() {
        if (mParam.gridLayoutManager != null) {
            //if user set span ads
            int nCol = mParam.gridLayoutManager.getSpanCount();
            if (mParam.adItemInterval % nCol != 0) {
                throw new IllegalArgumentException(String.format("The adItemInterval (%d) is not divisible by number of columns in GridLayoutManager (%d)", mParam.adItemInterval, nCol));
            }
        }
    }

    private int convertAdPosition2OrgPosition(int position) {

        return position - (position + 1) / (mParam.adItemInterval + 1);
    }

    @Override
    public int getItemCount() {
        int realCount = super.getItemCount();
        return realCount + realCount / mParam.adItemInterval;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(convertAdPosition2OrgPosition(position));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, convertAdPosition2OrgPosition(position));
    }

    private void setSpanAds() {
        if (mParam.gridLayoutManager == null) {
            return;
        }
        final GridLayoutManager.SpanSizeLookup spl = mParam.gridLayoutManager.getSpanSizeLookup();
        mParam.gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });
    }

    private static class Param {
        RecyclerView.Adapter adapter;
        int adItemInterval;
        boolean forceReloadAdOnBind;

        int layout;

        @LayoutRes
        int itemContainerLayoutRes;

        @IdRes
        int itemContainerId;

        GridLayoutManager gridLayoutManager;
    }

    public static class Builder {
        private final FragmentsGenerator.Param mParam;

        private Builder(FragmentsGenerator.Param param) {
            mParam = param;
        }

        public static FragmentsGenerator.Builder with(RecyclerView.Adapter wrapped, String layout) {
            FragmentsGenerator.Param param = new FragmentsGenerator.Param();
            param.adapter = wrapped;


            if (layout.toLowerCase().equals("small")) {
                param.layout = 0;
            } else if (layout.toLowerCase().equals("medium")) {

                param.layout = 1;
            } else {
                param.layout = 2;

            }

            //default value
            param.adItemInterval = DEFAULT_AD_ITEM_INTERVAL;
            param.itemContainerLayoutRes = R.layout.item_admob_native_ad_outline;
            param.itemContainerId = R.id.ad_container;
            param.forceReloadAdOnBind = true;
            return new FragmentsGenerator.Builder(param);
        }

        public FragmentsGenerator.Builder adItemInterval(int interval) {
            mParam.adItemInterval = interval;
            return this;
        }

        public FragmentsGenerator.Builder adLayout(@LayoutRes int layoutContainerRes, @IdRes int itemContainerId) {
            mParam.itemContainerLayoutRes = layoutContainerRes;
            mParam.itemContainerId = itemContainerId;
            return this;
        }

        public FragmentsGenerator build() {
            return new FragmentsGenerator(mParam);
        }

        public FragmentsGenerator.Builder enableSpanRow(GridLayoutManager layoutManager) {
            mParam.gridLayoutManager = layoutManager;
            return this;
        }

        public FragmentsGenerator.Builder adItemIterval(int i) {
            mParam.adItemInterval = i;
            return this;
        }

        public FragmentsGenerator.Builder forceReloadAdOnBind(boolean forced) {
            mParam.forceReloadAdOnBind = forced;
            return this;
        }
    }

    private static class AdViewHolder extends RecyclerView.ViewHolder {

        TemplateView templatesmall, templatemedium, templatecustom;
        LinearLayout adContainer;
        boolean loaded;

        AdViewHolder(View view) {
            super(view);
            templatesmall = view.findViewById(R.id.my_templatesmall);
            templatecustom = view.findViewById(R.id.my_templatecustom);
            templatemedium = view.findViewById(R.id.my_templatemedium);
            loaded = false;
            adContainer = (LinearLayout) view.findViewById(R.id.native_ad_container);
        }

        Context getContext() {
            return adContainer.getContext();
        }
    }
}
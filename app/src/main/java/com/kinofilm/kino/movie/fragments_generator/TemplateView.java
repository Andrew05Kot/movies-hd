// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.kinofilm.kino.movie.fragments_generator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.kinofilm.kino.movie.R;

/** Base class for a template view. * */
public class TemplateView extends FrameLayout {

  private int templateType;
  private NativeTemplateStyle styles;
  private TextView primaryView;
  private TextView secondaryView;
  private RatingBar ratingBar;
  private TextView tertiaryView;
  private ImageView iconView;
  private Button callToActionView;
  private ConstraintLayout background;

  private static final String MEDIUM_TEMPLATE = "medium_template";
  private static final String SMALL_TEMPLATE = "small_template";

  public TemplateView(Context context) {
    super(context);
  }

  public TemplateView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context, attrs);
  }

  public TemplateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initView(context, attrs);
  }

  public TemplateView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initView(context, attrs);
  }

  public void setStyles(NativeTemplateStyle styles) {
    this.styles = styles;
    this.applyStyles();
  }


  private void applyStyles() {

    Drawable mainBackground = styles.getMainBackgroundColor();
    if (mainBackground != null) {
      background.setBackground(mainBackground);
      if (primaryView != null) {
        primaryView.setBackground(mainBackground);
      }
      if (secondaryView != null) {
        secondaryView.setBackground(mainBackground);
      }
      if (tertiaryView != null) {
        tertiaryView.setBackground(mainBackground);
      }
    }

    Typeface primary = styles.getPrimaryTextTypeface();
    if (primary != null && primaryView != null) {
      primaryView.setTypeface(primary);
    }

    Typeface secondary = styles.getSecondaryTextTypeface();
    if (secondary != null && secondaryView != null) {
      secondaryView.setTypeface(secondary);
    }

    Typeface tertiary = styles.getTertiaryTextTypeface();
    if (tertiary != null && tertiaryView != null) {
      tertiaryView.setTypeface(tertiary);
    }

    Typeface ctaTypeface = styles.getCallToActionTextTypeface();
    if (ctaTypeface != null && callToActionView != null) {
      callToActionView.setTypeface(ctaTypeface);
    }

    int primaryTypefaceColor = styles.getPrimaryTextTypefaceColor();
    if (primaryTypefaceColor > 0 && primaryView != null) {
      primaryView.setTextColor(primaryTypefaceColor);
    }

    int secondaryTypefaceColor = styles.getSecondaryTextTypefaceColor();
    if (secondaryTypefaceColor > 0 && secondaryView != null) {
      secondaryView.setTextColor(secondaryTypefaceColor);
    }

    int tertiaryTypefaceColor = styles.getTertiaryTextTypefaceColor();
    if (tertiaryTypefaceColor > 0 && tertiaryView != null) {
      tertiaryView.setTextColor(tertiaryTypefaceColor);
    }

    int ctaTypefaceColor = styles.getCallToActionTypefaceColor();
    if (ctaTypefaceColor > 0 && callToActionView != null) {
      callToActionView.setTextColor(ctaTypefaceColor);
    }

    float ctaTextSize = styles.getCallToActionTextSize();
    if (ctaTextSize > 0 && callToActionView != null) {
      callToActionView.setTextSize(ctaTextSize);
    }

    float primaryTextSize = styles.getPrimaryTextSize();
    if (primaryTextSize > 0 && primaryView != null) {
      primaryView.setTextSize(primaryTextSize);
    }

    float secondaryTextSize = styles.getSecondaryTextSize();
    if (secondaryTextSize > 0 && secondaryView != null) {
      secondaryView.setTextSize(secondaryTextSize);
    }

    float tertiaryTextSize = styles.getTertiaryTextSize();
    if (tertiaryTextSize > 0 && tertiaryView != null) {
      tertiaryView.setTextSize(tertiaryTextSize);
    }

    Drawable ctaBackground = styles.getCallToActionBackgroundColor();
    if (ctaBackground != null && callToActionView != null) {
      callToActionView.setBackground(ctaBackground);
    }

    Drawable primaryBackground = styles.getPrimaryTextBackgroundColor();
    if (primaryBackground != null && primaryView != null) {
      primaryView.setBackground(primaryBackground);
    }

    Drawable secondaryBackground = styles.getSecondaryTextBackgroundColor();
    if (secondaryBackground != null && secondaryView != null) {
      secondaryView.setBackground(secondaryBackground);
    }

    Drawable tertiaryBackground = styles.getTertiaryTextBackgroundColor();
    if (tertiaryBackground != null && tertiaryView != null) {
      tertiaryView.setBackground(tertiaryBackground);
    }

    invalidate();
    requestLayout();
  }

  private void initView(Context context, AttributeSet attributeSet) {

    TypedArray attributes =
        context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.TemplateView, 0, 0);

    try {
      templateType =
          attributes.getResourceId(
              R.styleable.TemplateView_gnt_template_type, R.layout.gnt_custom_medium_template_view);
    } finally {
      attributes.recycle();
    }
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(templateType, this);
  }

  @Override
  public void onFinishInflate() {
    super.onFinishInflate();
    primaryView = (TextView) findViewById(R.id.primary);
    secondaryView = (TextView) findViewById(R.id.secondary);
    tertiaryView = (TextView) findViewById(R.id.body);

    ratingBar = (RatingBar) findViewById(R.id.rating_bar);
    ratingBar.setEnabled(false);

    callToActionView = (Button) findViewById(R.id.cta);
    iconView = (ImageView) findViewById(R.id.icon);
    background = (ConstraintLayout) findViewById(R.id.background);
  }
}

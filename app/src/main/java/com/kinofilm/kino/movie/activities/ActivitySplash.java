package com.kinofilm.kino.movie.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.kinofilm.kino.movie.Config;
import com.kinofilm.kino.movie.R;

public class ActivitySplash extends AppCompatActivity {

    private Boolean isCancelled = false;
    private ProgressBar progressBar;
    private long nid = 0;
    private String url = "";
    private SharedPreferences sPref;
    private MyApplication myApplication;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        loadText();

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        if (getIntent().hasExtra("nid")) {
            nid = getIntent().getLongExtra("nid", 0);
            url = getIntent().getStringExtra("external_link");
        }

        else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigate();
                }
            }, Config.SPLASH_TIME);
        }
    }

    private void loadText() {
        sPref = getSharedPreferences("PREFS", MODE_PRIVATE);
    }

    private void navigate() {
        if (!isCancelled) {
            if (nid == 0) {
                if (url.equals("") || url.equals("no_url")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    Log.d("TAGING", "iMainActivity1");
                    startActivity(intent);
                    finish();
                } else {
                    Intent a = new Intent(getApplicationContext(), MainActivity.class);
                    Log.d("TAGING", "iMainActivity2");
                    startActivity(a);

                    Intent b = new Intent(getApplicationContext(), ActivityWebView.class);
                    Log.d("TAGING", "ActivityWebView");
                    b.putExtra("url", url);
                    startActivity(b);

                    finish();
                }
            } else {
                Intent intent = new Intent(getApplicationContext(), ActivityOneSignalDetail.class);
                Log.d("TAGING", "ActivityOneSignalDetail");
                intent.putExtra("id", nid);
                startActivity(intent);
                finish();
            }
        }
    }
}

package com.kinofilm.kino.movie.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinofilm.kino.movie.Config;
import com.kinofilm.kino.movie.R;
import com.kinofilm.kino.movie.activities.ActivityProfile;
import com.kinofilm.kino.movie.activities.MyApplication;
import com.kinofilm.kino.movie.utils.ApiConnector;
import com.kinofilm.kino.movie.utils.Constant;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class FragmentProfile extends Fragment {

    private View root_view, parent_view;
    MyApplication myApplication;
    RelativeLayout lyt_is_login, lyt_login_register;
    TextView txt_edit;
    TextView txt_login;
    TextView txt_logout;
    ProgressDialog progressDialog;
    TextView txt_register, txt_username, txt_email;
    ImageView img_profile;
    RecyclerView recyclerView;
    LinearLayout lyt_root;
    private SharedPreferences sPref;
    private String adsResult = "false";
    final String SAVED_TEXT = "checkAdMob";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_profile, null);
        parent_view = getActivity().findViewById(R.id.main_content);
        lyt_root = root_view.findViewById(R.id.root_layout);
        loadText();
        myApplication = MyApplication.getInstance();

        lyt_is_login = root_view.findViewById(R.id.lyt_is_login);
        lyt_login_register = root_view.findViewById(R.id.lyt_login_register);
        txt_login = root_view.findViewById(R.id.btn_login);
        txt_logout = root_view.findViewById(R.id.txt_logout);
        txt_edit = root_view.findViewById(R.id.btn_logout);
        txt_register = root_view.findViewById(R.id.txt_register);
        txt_username = root_view.findViewById(R.id.txt_username);
        txt_email = root_view.findViewById(R.id.txt_email);
        img_profile = root_view.findViewById(R.id.img_profile);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getResources().getString(R.string.title_please_wait));
        progressDialog.setMessage(getResources().getString(R.string.logout_process));
        progressDialog.setCancelable(false);

        recyclerView = root_view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (Config.ENABLE_RTL_MODE) {
            lyt_root.setRotationY(180);
        }

        return root_view;
    }

    private void loadText() {

            sPref = getActivity().getPreferences(getActivity().MODE_PRIVATE);
            adsResult = sPref.getString(SAVED_TEXT, "");

    }

    @Override
    public void onResume() {

        if (myApplication.getIsLogin()) {
            lyt_is_login.setVisibility(View.VISIBLE);
            lyt_login_register.setVisibility(View.GONE);

            new getUserImage().execute(new ApiConnector());

            txt_logout.setVisibility(View.VISIBLE);
            txt_logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logoutDialog();
                }
            });

        } else {
            lyt_is_login.setVisibility(View.GONE);
            lyt_login_register.setVisibility(View.VISIBLE);
            txt_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            txt_register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            txt_logout.setVisibility(View.GONE);
        }

        super.onResume();
    }

    public void logoutDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.logout_title);
        builder.setMessage(R.string.logout_message);
        builder.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface di, int i) {

                MyApplication.getInstance().saveIsLogin(false);
                progressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.logout_success);
                        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                refreshFragment();
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                    }
                }, Constant.DELAY_PROGRESS_DIALOG);

            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();

    }

    public void refreshFragment() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.detach(this).attach(this).commit();
    }

    private class getUserImage extends AsyncTask<ApiConnector, Long, JSONArray> {
        @Override
        protected JSONArray doInBackground(ApiConnector... params) {
            return params[0].GetCustomerDetails(myApplication.getUserId());
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {

            try {
                JSONObject objJson = null;
                objJson = jsonArray.getJSONObject(0);
                final String user_id = objJson.getString("id");
                final String name = objJson.getString("name");
                final String email = objJson.getString("email");
                final String user_image = objJson.getString("imageName");
                final String password = objJson.getString("password");

                txt_username.setText(name);
                txt_email.setText(email);

                if (user_image.equals("")) {
                    img_profile.setImageResource(R.drawable.ic_user_account);
                } else {
                    /*Picasso.with(getActivity())
                            .load(Config.ADMIN_PANEL_URL + "/upload/avatar/" + user_image.replace(" ", "%20"))
                            .resize(300, 300)
                            .centerCrop()

                            .into(img_profile);*/
                }

                txt_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ActivityProfile.class);
                        intent.putExtra("name", name);
                        intent.putExtra("email", email);
                        intent.putExtra("user_image", user_image);
                        intent.putExtra("password", password);
                        startActivity(intent);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private List<Data> getDataInformation() {
        return null;
    }

    public class Data {
        private final int image;
        private final String title;
        private final String sub_title;

        public int getImage() {
            return image;
        }

        public String getTitle() {
            return title;
        }

        public String getSub_title() {
            return sub_title;
        }

        public Data(int image, String title, String sub_title) {
            this.image = image;
            this.title = title;
            this.sub_title = sub_title;
        }
    }

}

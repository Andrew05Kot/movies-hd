package com.kinofilm.kino.movie.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kinofilm.kino.movie.Config;
import com.kinofilm.kino.movie.R;
import com.kinofilm.kino.movie.utils.ApiConnector;
import com.kinofilm.kino.movie.utils.Constant;
import com.kinofilm.kino.movie.utils.NetworkCheck;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActivityProfile extends AppCompatActivity {

    EditText txt_email, txt_name, txt_pwd;
    MyApplication myApplication;
    Button btn_update;
    String strMessage;
    RelativeLayout lyt_profile;
    //ProgressBar progressBar;
    private ImageView img_profile, img_change;
    private static final int SELECT_PICTURE = 1;
    String str_user_id;
    ProgressDialog progressDialog;
    String str_name, str_email, str_image, str_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(R.string.title_menu_profile);
        }

        Intent intent = getIntent();
        str_name = intent.getStringExtra("name");
        str_email = intent.getStringExtra("email");
        str_image = intent.getStringExtra("user_image");
        str_password = intent.getStringExtra("password");

        progressDialog = new ProgressDialog(ActivityProfile.this);
        progressDialog.setTitle(getResources().getString(R.string.title_please_wait));
        progressDialog.setMessage(getResources().getString(R.string.logout_process));
        progressDialog.setCancelable(false);

        myApplication = MyApplication.getInstance();

        img_profile = findViewById(R.id.profile_image);
        img_change = findViewById(R.id.change_image);

        lyt_profile = findViewById(R.id.lyt_profile);
        //progressBar = (ProgressBar) findViewById(R.id.progressBar);

        txt_email = findViewById(R.id.edt_email);
        txt_name = findViewById(R.id.edt_user);
        txt_pwd = findViewById(R.id.edt_password);

        txt_name.setText(str_name);
        txt_email.setText(str_email);
        txt_pwd.setText(str_password);

        if (NetworkCheck.isNetworkAvailable(ActivityProfile.this)) {
            //new MyTask().execute(Constant.PROFILE_URL + myApplication.getUserId());
            new getUserImage().execute(new ApiConnector());
        } else {
            showToast("No Network Connection!!");
            SetMessage();
        }

        img_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestStoragePermission();
            }
        });

    }

    @TargetApi(16)
    private void requestStoragePermission() {
        Dexter.withActivity(ActivityProfile.this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Setting
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Error occurred! " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityProfile.this);
        builder.setTitle("Need Permissions");
        builder.setMessage(R.string.permission_upload);
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.save:
                if (NetworkCheck.isNetworkAvailable(ActivityProfile.this)) {
                    new MyTaskUp().execute(Constant.PROFILE_UPDATE_URL + myApplication.getUserId() +
                            "&name=" + txt_name.getText().toString().replace(" ", "%20") +
                            "&email=" + txt_email.getText().toString() +
                            "&password=" + txt_pwd.getText().toString()
                    );

                } else {
                    showToast("No Network Connection!!");
                    SetMessage();
                }
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public void setAdapter() {

    }

    public void showToast(String msg) {
        Toast.makeText(ActivityProfile.this, msg, Toast.LENGTH_LONG).show();
    }

    private class MyTaskUp extends AsyncTask<String, Void, String> {
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return NetworkCheck.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (null == result || result.length() == 0) {
                showToast("No Data Found!!!");


            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.CATEGORY_ARRAY_NAME);
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        strMessage = objJson.getString(Constant.MSG);
                        Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResult();
            }

        }
    }

    public void setResult() {

        if (Constant.GET_SUCCESS_MSG == 0) {

        } else {
            progressDialog = new ProgressDialog(ActivityProfile.this);
            progressDialog.setTitle(R.string.updating_profile);
            progressDialog.setMessage(getResources().getString(R.string.waiting_message));
            progressDialog.setCancelable(false);
            progressDialog.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityProfile.this);
                    builder.setMessage("Update Successfully");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                    builder.setCancelable(false);
                    builder.show();
                }
            }, Constant.DELAY_PROGRESS_DIALOG);
        }
    }

    private AlertDialog SetMessage() {

        AlertDialog.Builder alert = new AlertDialog.Builder(ActivityProfile.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alert = new AlertDialog.Builder(ActivityProfile.this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            alert = new AlertDialog.Builder(ActivityProfile.this);
        }
        alert.setTitle("No Network Connection!!");
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setMessage("Please connect to working Internet connection");

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        });
        alert.show();
        return alert.create();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (Build.VERSION.SDK_INT < 19) {
                    String selectedImagePath = getPath(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                    SetImage(bitmap);
                } else {
                    ParcelFileDescriptor parcelFileDescriptor;
                    try {
                        parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImageUri, "r");
                        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        parcelFileDescriptor.close();
                        SetImage(image);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void SetImage(Bitmap image) {
        this.img_profile.setImageBitmap(image);

        // upload
        String imageData = encodeTobase64(image);
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("image", imageData));
        params.add(new BasicNameValuePair("user_id", myApplication.getUserId()));

        new AsyncTask<ApiConnector, Long, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = new ProgressDialog(ActivityProfile.this);
                progressDialog.setTitle(R.string.updating_profile);
                progressDialog.setMessage(getResources().getString(R.string.waiting_message));
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(ApiConnector... apiConnectors) {
                return apiConnectors[0].uploadImageToserver(params);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                }, Constant.DELAY_PROGRESS_DIALOG);
            }

        }.execute(new ApiConnector());

    }

    public static String encodeTobase64(Bitmap image) {
        System.gc();

        if (image == null) return null;

        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        return imageEncoded;
    }

    public String getPath(Uri uri) {
        if (uri == null) {
            return null;
        }
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
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
                String user_id = objJson.getString("id");
                String user_image = objJson.getString("imageName");

                if (user_image.equals("")) {
                    img_profile.setImageResource(R.drawable.ic_user_account);
                } else {

                    Picasso.get()
                            .load(Config.ADMIN_PANEL_URL + "/upload/avatar/" + user_image.replace(" ", "%20"))
                            .resize(300, 300)
                            .centerCrop()
                            .placeholder(R.drawable.ic_user_account)
                            .into(img_profile);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}

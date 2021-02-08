package com.knowhow.android.picturewithai;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.knowhow.android.picturewithai.remote.ApiConstants;
import com.knowhow.android.picturewithai.remote.ServiceInterface;
import com.knowhow.android.picturewithai.utils.FileUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SelectCategory extends AppCompatActivity {

    final int REQUEST_EXTERNAL_STORAGE = 100;

    ServiceInterface serviceInterface;
    List<Uri> files = new ArrayList<>();

    public String type="";

    private SubThread subThread = new SubThread();

    private ProgressBar progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_category);

        progress = findViewById(R.id.progress);

        View personimage = findViewById(R.id.personImage);
        personimage.setOnClickListener(v -> {

            if (ActivityCompat.checkSelfPermission(SelectCategory.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SelectCategory.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);

            } else {
                launchGalleryIntent();
                type="Person";

            }
        });


        View sightimage = findViewById(R.id.sightImage);
        sightimage.setOnClickListener(v -> {

            if (ActivityCompat.checkSelfPermission(SelectCategory.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SelectCategory.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);

            } else {
                launchGalleryIntent();
                type="Background";
            }
        });
    }



    class SubThread extends Thread {
        @Override
        public synchronized void run() {

            if (type.equals("Background")) {
                predictBackground();
            }else {
                predictPerson();
            }



        }

        public void predictPerson() {
            Log.d("type", "person");

            List<MultipartBody.Part> list = new ArrayList<>();

            for (Uri uri : files) {

                list.add(prepareFilePart("image", uri));
            }

            serviceInterface = ApiConstants.getClient().create(ServiceInterface.class);
            Call<ResponseBody> call = serviceInterface.predictPerson(list);
            runOnUiThread(() -> progress.setVisibility(View.VISIBLE));


            call.enqueue(new Callback<ResponseBody>() {


                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                    try {

                        runOnUiThread(() -> progress.setVisibility(View.GONE));


                        JSONObject jObject = new JSONObject(response.body().string());
                        int best_idx = jObject.getInt("index");

                        Intent intent = new Intent(SelectCategory.this, BestPicture.class);
                        intent.putExtra("path", String.valueOf(files.get(best_idx)));


                        startActivity(intent);


                    } catch (Exception e) {
                        Log.d("Exception", "|=>" + e.getMessage());

                    }


                    files.clear();
                    list.clear();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                    Log.i("my", t.getMessage());

                    files.clear();
                    list.clear();
                }
            });
        }


        public void predictBackground() {
            Log.d("type", "background");
            List<MultipartBody.Part> list = new ArrayList<>();

            for (Uri uri : files) {


                list.add(prepareFilePart("image", uri));
            }

            serviceInterface = ApiConstants.getClient().create(ServiceInterface.class);


            Call<ResponseBody> call = serviceInterface.predictBackground(list);

            ProgressBar progress = findViewById(R.id.progress);
            progress.setVisibility(View.VISIBLE);


            call.enqueue(new Callback<ResponseBody>() {


                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                    try {


                        progress.setVisibility(View.GONE);


                        JSONObject jObject = new JSONObject(response.body().string());
                        int best_idx = jObject.getInt("index");

                        Intent intent = new Intent(SelectCategory.this, BestPicture.class);
                        intent.putExtra("path", String.valueOf(files.get(best_idx)));
                        Log.d("files", String.valueOf(best_idx));


                        startActivity(intent);


                    } catch (Exception e) {
                        Log.d("Exception", "|=>" + e.getMessage());
                    }


                    files.clear();
                    list.clear();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                    Log.i("my", t.getMessage());

                    files.clear();
                    list.clear();
                }
            });
        }


        @NonNull
        private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {

            File file = new File(fileUri.getPath());
            Log.i("here is error", file.getAbsolutePath());
            // create RequestBody instance from file

            RequestBody requestFile =
                    RequestBody.create(
                            MediaType.parse("image/*"),
                            file);

            // MultipartBody.Part is used to send also the actual file name
            return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);


        }
    }


    public void launchGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_EXTERNAL_STORAGE);
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EXTERNAL_STORAGE && resultCode == RESULT_OK) {


            final List<Bitmap> bitmaps = new ArrayList<>();
            ClipData clipData = data.getClipData();



            if (clipData != null) {
                //multiple images selecetd
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri img = clipData.getItemAt(i).getUri();
                    String imgPath = FileUtil.getPath(SelectCategory.this,img);
                    files.add(Uri.parse(imgPath));


                    try {
                        InputStream inputStream = getContentResolver().openInputStream(img);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        bitmaps.add(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                //single image selected

                Uri img = data.getData();
                String imgPath = FileUtil.getPath(SelectCategory.this,img);
                files.add(Uri.parse(imgPath));


                try {
                    InputStream inputStream = getContentResolver().openInputStream(img);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmaps.add(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }

            subThread.start();  // sub thread 시작

        }
    }
}
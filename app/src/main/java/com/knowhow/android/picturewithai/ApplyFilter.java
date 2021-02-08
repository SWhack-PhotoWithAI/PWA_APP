package com.knowhow.android.picturewithai;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.knowhow.android.picturewithai.remote.ApiConstants;
import com.knowhow.android.picturewithai.remote.ServiceInterface;
import com.knowhow.android.picturewithai.utils.FileUtil;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApplyFilter extends AppCompatActivity {

    final int REQUEST_EXTERNAL_STORAGE = 100;

    List<Uri> files = new ArrayList<>();

    ServiceInterface serviceInterface;

    ImageView original, filtered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_apply_filter);

        launchGalleryIntent();

        original = findViewById(R.id.orgImage);
        filtered = findViewById(R.id.filteredImage);
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



            Uri img = data.getData();


            String imgPath = FileUtil.getPath(ApplyFilter.this,img);

            applyFilter(Uri.parse(imgPath));

            try {
                InputStream inputStream = getContentResolver().openInputStream(img);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                original.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {

        File file = new File(fileUri.getPath());
        Log.i("here is error",file.getAbsolutePath());
        // create RequestBody instance from file

        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("image/*"),
                        file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);


    }


    //===== Upload files to server
    public void applyFilter(Uri uri){

        serviceInterface = ApiConstants.getClient().create(ServiceInterface.class);


        Call<ResponseBody> call = serviceInterface.applyFilter(prepareFilePart("source", uri));

        ProgressBar progress = findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);


        call.enqueue(new Callback<ResponseBody>() {



            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                try {


                    progress.setVisibility(View.GONE);

                    File downloadedFile = new File(getFilesDir(), "cartoon.jpg");
                    BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
                    sink.writeAll(response.body().source());
                    sink.close();
                    String filePath = downloadedFile.getPath();
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);

                    filtered.setImageBitmap(bitmap);

                }
                catch (Exception e){
                    Log.d("Exception","|=>"+e.getMessage());
//
                }


                files.clear();

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                Log.i("my",t.getMessage());

                files.clear();

            }
        });
    }
}
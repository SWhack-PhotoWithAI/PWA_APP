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

public class ApplyFilter extends AppCompatActivity {

    final int REQUEST_EXTERNAL_STORAGE = 100;

    List<Uri> files = new ArrayList<>();

    ServiceInterface serviceInterface;

    ImageView original;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_apply_filter);

        launchGalleryIntent();

        original = findViewById(R.id.orgImage);
    }


    public void launchGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_EXTERNAL_STORAGE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //launchGalleryIntent();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EXTERNAL_STORAGE && resultCode == RESULT_OK) {

            //final ImageView imageView = findViewById(R.id.imageview);
            final List<Bitmap> bitmaps = new ArrayList<>();
            ClipData clipData = data.getClipData();



            if (clipData != null) {
                //multiple images selecetd
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri img = clipData.getItemAt(i).getUri();


                    String imgPath = FileUtil.getPath(ApplyFilter.this,img);

                    files.add(Uri.parse(imgPath));


                    Log.d("URI", img.toString());
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
                Log.d("uri", String.valueOf(img));

                String imgPath = FileUtil.getPath(ApplyFilter.this,img);

                files.add(Uri.parse(imgPath));

                //uploadImages();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(img);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmaps.add(bitmap);

                    original.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }

            applyFilter();



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
    public void applyFilter(){

        List<MultipartBody.Part> list = new ArrayList<>();

        for (Uri uri:files) {


            list.add(prepareFilePart("source", uri));
        }

        serviceInterface = ApiConstants.getClient().create(ServiceInterface.class);


        Call<ResponseBody> call = serviceInterface.applyFilter(list);

        ProgressBar progress = (ProgressBar) findViewById(R.id.progress) ;
        progress.setVisibility(View.VISIBLE);


        call.enqueue(new Callback<ResponseBody>() {



            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                try {

                    Log.d("response", String.valueOf(response.code()));
                    progress.setVisibility(View.GONE);
//
//
//                    JSONObject jObject = new JSONObject(response.body().string());
//                    int best_idx= jObject.getInt("index");
//
//                    Intent intent = new Intent(SelectCategory.this, BestPicture.class);
//                    intent.putExtra("path", String.valueOf(files.get(best_idx)));
//                    Log.d("files", String.valueOf(best_idx));
//                    //int best_idx=Integer.toInteger(response.body().string());
//
//
//                    startActivity(intent);


                }
                catch (Exception e){
                    Log.d("Exception","|=>"+e.getMessage());
//
                }


                files.clear();
                list.clear();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("testlist", String.valueOf(list.size()));
                Log.d("testlist", String.valueOf(files.size()));
                Log.i("my",t.getMessage());

                files.clear();
                list.clear();
            }
        });
    }
}
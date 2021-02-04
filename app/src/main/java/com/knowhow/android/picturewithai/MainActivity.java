package com.knowhow.android.picturewithai;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.knowhow.android.picturewithai.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    final int REQUEST_EXTERNAL_STORAGE = 100;
    String filePath="/document/image:34";
    HerokuService service;

    //private final String BASE_URL = "http://127.0.0.1:8000";
    //private MyAPI mMyAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://photo-with-ai.herokuapp.com/")
                .build();

        service = retrofit.create(HerokuService.class);


        //uploadImage();
        //uploadMultipleImage();
        View selectimage = findViewById(R.id.mainmenu1);
        selectimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
//                    return;
                } else {
                    launchGalleryIntent();

                    //initMyAPI(BASE_URL);
                }
            }
        });






    }

    private void uploadMultipleImage() {
        MultipartBody.Part image1 = prepareImagePart(filePath, "image1");
        MultipartBody.Part image2 = prepareImagePart(filePath, "image2");

        Retrofit retrofit = NetworkClient.getRetrofit();
        UploadApis uploadApis = retrofit.create(UploadApis.class);
        Call<ResponseBody> uploader= uploadApis.uploadMultiImage(image1, image2);
        uploader.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                System.out.println("hello2");
                System.out.println(response.code());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println("good");
            }
        });


    }

    private MultipartBody.Part prepareImagePart(String path, String partName){
        File file = new File(path);
        RequestBody requestBody = RequestBody.create(MediaType.parse(getContentResolver().getType(Uri.fromFile(file))), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestBody);
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result; Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            // Source is Dropbox or other similar local file path
            result = contentURI.getPath(); }
        else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx); cursor.close();
        } return result;
    }

//    private void initMyAPI(String baseUrl){
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(baseUrl)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        mMyAPI = retrofit.create(MyAPI.class);
//
//        Call<List<PostItem>> getCall = mMyAPI.get_posts();
//        getCall.enqueue(new Callback() {
//
//            @Override
//            public void onResponse(Call call, retrofit2.Response response) {
//
//            }
//
//            @Override
//            public void onFailure(Call call, Throwable t)  {
//
//            }
//
//
//        });
//    }

    private void uploadImage() {
        File file = new File(filePath);
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part parts = MultipartBody.Part.createFormData("newimage", file.getName(), requestBody);

        RequestBody someData = RequestBody.create(MediaType.parse("text/plain"), "This is a new Image");

        Retrofit retrofit = NetworkClient.getRetrofit();

        UploadApis uploadApis = retrofit.create(UploadApis.class);
        Call call = uploadApis.uploadImage(parts, someData);
        call.enqueue(new Callback() {

            @Override
            public void onResponse(Call call, retrofit2.Response response) {
                System.out.println("hello2");
                System.out.println(response.code());
            }

            @Override
            public void onFailure(Call call, Throwable t)  {
                System.out.println("good");
            }


        });

    }

    public void testHeroku(){
        Call<ResponseBody> call = service.hello();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> _,
                                   Response<ResponseBody> response) {
                System.out.println("hello2");
                System.out.println(response.code());
            }

            @Override
            public void onFailure(Call<ResponseBody> _, Throwable t) {
                System.out.println("good");
            }
        });
    }

    public void launchGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_EXTERNAL_STORAGE);
    }



    public static void fileUpload (File file) {



        RequestBody requestBody;
        MultipartBody.Part body;
        LinkedHashMap<String, RequestBody> mapRequestBody = new LinkedHashMap<String, RequestBody>();
        List<MultipartBody.Part> arrBody = new ArrayList<>();


        requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        mapRequestBody.put("file\"; filename=\"" + file.getName(), requestBody);
        mapRequestBody.put("test", RequestBody.create(MediaType.parse("text/plain"), "gogogogogogogog"));


        body = MultipartBody.Part.createFormData("fileName", file.getName(), requestBody);
        arrBody.add(body);


        Call<JsonObject> call = RetrofitImg.getInstance().getService().uploadFile(mapRequestBody, arrBody);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (response.body() != null) {

                }
                System.out.println("hello");
                System.out.println(response.code());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                System.out.println("goodH");
            }
        });


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
                    launchGalleryIntent();
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

            final ImageView imageView = findViewById(R.id.imageview);
            final List<Bitmap> bitmaps = new ArrayList<>();
            ClipData clipData = data.getClipData();

            if (clipData != null) {
                //multiple images selecetd
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    filePath= imageUri.getPath();
                    System.out.println(filePath);

                    Log.d("URI", imageUri.toString());
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        bitmaps.add(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //single image selected
                Uri imageUri = data.getData();
                filePath= imageUri.getPath();

                Log.d("URI", imageUri.toString());
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmaps.add(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (final Bitmap b : bitmaps) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(b);
                                Toast.makeText(getApplicationContext(), filePath, Toast.LENGTH_LONG).show();
                                //uploadImage();
                                //fileUpload(new File(filePath));
                                //uploadImage();
                                testHeroku();

                            }
                        });

                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }
}

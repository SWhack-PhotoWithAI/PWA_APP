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
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SelectCategory extends AppCompatActivity {

    final int REQUEST_EXTERNAL_STORAGE = 100;

    HerokuService service;
    ServiceInterface serviceInterface;
    List<Uri> files = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_category);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://test-photo-with-ai.herokuapp.com/")
                .build();

        service = retrofit.create(HerokuService.class);
        //uploadImage();
        //uploadMultipleImage();
        View personimage = findViewById(R.id.personImage);
        personimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(SelectCategory.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SelectCategory.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
//                    return;
                } else {
                    launchGalleryIntent();


                }
            }
        });


        View sightimage = findViewById(R.id.sightImage);
        sightimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(SelectCategory.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SelectCategory.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
//                    return;
                } else {
                    launchGalleryIntent();


                }
            }
        });
    }


    //===== Upload files to server
    public void uploadImages(){



        List<MultipartBody.Part> list = new ArrayList<>();

        for (Uri uri:files) {

            Log.i("uris",uri.getPath());



            list.add(prepareFilePart("file", uri));
        }

        serviceInterface = ApiConstants.getClient().create(ServiceInterface.class);


        Call<ResponseBody> call = serviceInterface.uploadNewsFeedImages(list);

        ProgressBar progress = (ProgressBar) findViewById(R.id.progress) ;
        progress.setVisibility(View.VISIBLE);


        call.enqueue(new Callback<ResponseBody>() {



            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                System.out.println("hello");
                System.out.println(response.code());
                try {
                    Log.d("nn", String.valueOf(response.code()));
                    Log.i("hh", String.valueOf(response));

                    ResponseBody addMediaModel = response.body();
                    progress.setVisibility(View.GONE);
//                    System.out.println(response.body().string());
//
//                    runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                  }
//                });





                    Intent intent = new Intent(SelectCategory.this, BestPicture.class);
                    intent.putExtra("path", String.valueOf(files.get(0)));
                    Log.d("files", String.valueOf(files.get(0)));


                    startActivity(intent);

//                    File f = new File(String.valueOf(files.get(0)));
//                    Drawable d = Drawable.createFromPath(f.getAbsolutePath());
//
//                    Log.d("ii2", f.getAbsolutePath());




                }
                catch (Exception e){
                    Log.d("Exception","|=>"+e.getMessage());
//
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                Log.i("my",t.getMessage());
            }
        });
    }

    private String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
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

    public void testHeroku(){
        Call<ResponseBody> call = service.hello();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> _,
                                   Response<ResponseBody> response) {

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

            //final ImageView imageView = findViewById(R.id.imageview);
            final List<Bitmap> bitmaps = new ArrayList<>();
            ClipData clipData = data.getClipData();



            if (clipData != null) {
                //multiple images selecetd
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri img = clipData.getItemAt(i).getUri();




                    String imgPath = FileUtil.getPath(SelectCategory.this,img);

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

                String imgPath = FileUtil.getPath(SelectCategory.this,img);

                files.add(Uri.parse(imgPath));

                //uploadImages();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(img);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmaps.add(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
            uploadImages();

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    for (final Bitmap b : bitmaps) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                imageView.setImageBitmap(b);
//
//                                //uploadImage();
//                                //fileUpload(new File(filePath));
//                                //uploadImage();
//                                //testHeroku();
//
//                            }
//                        });
//
//                        try {
//                            Thread.sleep(3000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }).start();
        }
    }
}
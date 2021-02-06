package com.knowhow.android.picturewithai;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.knowhow.android.picturewithai.remote.ApiConstants;
import com.knowhow.android.picturewithai.remote.ServiceInterface;
import com.knowhow.android.picturewithai.remote2.ApiConstants2;
import com.knowhow.android.picturewithai.remote2.ServiceInterface2;
import com.knowhow.android.picturewithai.utils.FileUtil;

import org.json.JSONObject;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.icu.text.DisplayContext.LENGTH_SHORT;
import static android.widget.Toast.LENGTH_LONG;

public class TakePicture extends AppCompatActivity{

    private CameraSurfaceView cameraSurfaceView; // 전면 카메라 surfaceview
    private boolean mPermissionsGranted = false;
    private static final int PERMISSIONS_REQUEST_CAMERA = 0;
    private static final int PERMISSIONS_WRITE_EXTERNAL_STORAGE = 0;
    private static final int PERMISSIONS_READ_EXTERNAL_STORAGE = 0;
    private Button button_check, button_capture;
    private Bitmap nowBitmap=null;

    final int REQUEST_EXTERNAL_STORAGE = 100;

    //HerokuService service;
    ServiceInterface2 serviceInterface;

    List<Uri> files = new ArrayList<>();


    boolean isFirstTime = true;
    long startTime = 0;
    public Boolean isButton=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_take_picture);

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_READ_EXTERNAL_STORAGE);
            }
        }


        mPermissionsGranted = true;

        cameraSurfaceView = findViewById(R.id.camera_preview);

        button_check = findViewById(R.id.button1);



        button_check.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowFrame();

            }
        });

        button_capture = findViewById(R.id.button2);

        button_capture.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureFace();
            }
        });



    }

    public void nowFrame() {
        cameraSurfaceView.camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {


//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inSampleSize = 3;
//
//                Bitmap nowBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Log.d("hh", String.valueOf(data.length));

//                Uri uri = getImageUri(TakePicture.this, nowBitmap);
                //analyzeImage(uri);

            }
        });


    }




    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }



    public void captureFace() {


        cameraSurfaceView.capture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 3;

                Bitmap frontBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                saveImage(frontBitmap);
                camera.startPreview(); // 캡쳐 후 camera.startPreview() 필수!
            }
        });
    }

    public Bitmap byteArrayToBitmap( byte[] $byteArray ) {
        Bitmap bitmap = BitmapFactory.decodeByteArray( $byteArray, 0, $byteArray.length ) ;
        return bitmap ;
    }





    private void saveImage(Bitmap frontBitmap) {

        String root = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root);

        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "Front-" + timeStamp + ".png";
        File file = new File(myDir, fname);


        try {
            FileOutputStream out = new FileOutputStream(file);
            frontBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            // sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
            //     Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        // Tell the media scanner about the new file so that it is
// immediately available to the user.


        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

    }


    //===== Upload files to server
    public void analyzeImage(Uri uri){

        MultipartBody.Part list=prepareFilePart("image", uri);



        serviceInterface = ApiConstants2.getClient().create(ServiceInterface2.class);


        Call<ResponseBody> call = serviceInterface.analyzeImages(list);




        call.enqueue(new Callback<ResponseBody>() {



            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                try {


                    Log.d("vv", String.valueOf(response.code()));


                }
                catch (Exception e){
                    Log.d("Exception","|=>"+e.getMessage());
//
                }




            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                Log.d("vv", "vv");


            }
        });
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



}
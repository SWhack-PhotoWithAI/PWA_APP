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
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;



import com.knowhow.android.picturewithai.remote.ApiConstants;
import com.knowhow.android.picturewithai.remote.ServiceInterface;


import com.knowhow.android.picturewithai.utils.FileUtil;

import org.json.JSONObject;
import org.pytorch.Module;


import java.io.ByteArrayOutputStream;
import java.io.File;
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



public class TakePicture extends AppCompatActivity{

    private CameraSurfaceView cameraSurfaceView; // 카메라 surfaceview
    private static final int PERMISSIONS_REQUEST_CAMERA = 0;
    private static final int PERMISSIONS_WRITE_EXTERNAL_STORAGE = 0;
    private static final int PERMISSIONS_READ_EXTERNAL_STORAGE = 0;
    private static final int REQUEST_EXTERNAL_STORAGE = 100;
    private ServiceInterface serviceInterface;
    private ImageButton button_capture;
    private Bitmap nowBitmap=null;
    public Context mContext;


    private static final double THRESHOLD = 0.95;
    private static final double box_thres1 = 1280 * 0.45;
    private static final double box_thres2 = 1280 * 0.55;
    private static final double topthres_1 = 720 * 0.35;
    private static final double topthres_2 = 720 * 0.35;

    //topthres_2 = img.size[1] * 0.55
    //bottomthres = img.size[1] * 0.9


    private SubThread subThread = new SubThread();


    private ProgressBar progress;
    public Boolean stop=false;

    //Module module;


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

        this.mContext = this;


        cameraSurfaceView = findViewById(R.id.camera_preview);



        button_capture = findViewById(R.id.button);
        button_capture.setOnClickListener(view -> captureFace());


        progress = findViewById(R.id.progress) ;
        subThread.setDaemon(true);
        subThread.start();  // sub thread 시작

//        try {
//            module = Module.load(assetFilePath(this, "model.pt"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }



    class SubThread extends Thread {
        @Override
        public synchronized void run() {
            while(nowBitmap==null){

                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                nowBitmap=cameraSurfaceView.nowBitmap;
            }

            Uri uri = getImageUri(TakePicture.this, nowBitmap);
            String ImgPath = FileUtil.getPath(TakePicture.this,uri);
            analyzeImage(Uri.parse(ImgPath));
        }


        public void analyzeImage(Uri uri){

//        if (Looper.myLooper() == Looper.getMainLooper()){
//            Log.d("looper", "main thread");
//        } else{
//            Log.d("looper", "not main thread");
//        }
            runOnUiThread(() -> progress.setVisibility(View.VISIBLE));


            serviceInterface = ApiConstants.getClient().create(ServiceInterface.class);


            Call<ResponseBody> call = serviceInterface.analyzeImages(prepareFilePart("image", uri));


            call.enqueue(new Callback<ResponseBody>() {



                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                    try {

                        runOnUiThread(() -> progress.setVisibility(View.GONE));


                        if(!stop) {
                            JSONObject jObject = new JSONObject(response.body().string());
                            String message = jObject.getString("sen");

                            Toast toast = Toast.makeText(TakePicture.this, message, Toast.LENGTH_LONG);
                            ViewGroup group = (ViewGroup) toast.getView();
                            TextView messageTextView = (TextView) group.getChildAt(0);
                            messageTextView.setTextSize(30);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();

                            nowBitmap = cameraSurfaceView.nowBitmap;
                            Uri uri = getImageUri(TakePicture.this, nowBitmap);

                            String ImgPath = FileUtil.getPath(TakePicture.this, uri);
                            analyzeImage(Uri.parse(ImgPath));
                        }


                    }
                    catch (Exception e){
                        Log.d("Exception","|=>"+e.getMessage());

                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                    Log.i("my",t.getMessage());


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
    }



    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }




    public void captureFace() {


        cameraSurfaceView.capture((data, camera) -> {
            stop=true;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 3;

            Bitmap frontBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            saveImage(frontBitmap);


            Intent intent = new Intent(TakePicture.this, BestPicture.class);

            Uri uri = getImageUri(TakePicture.this, frontBitmap);
            String ImgPath = FileUtil.getPath(TakePicture.this,uri);
            intent.putExtra("path", String.valueOf(Uri.parse(ImgPath)));



            startActivity(intent);

        });
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

}
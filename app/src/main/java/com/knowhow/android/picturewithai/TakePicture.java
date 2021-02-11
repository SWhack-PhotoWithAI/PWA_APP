package com.knowhow.android.picturewithai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;



import com.knowhow.android.picturewithai.remote.ApiConstants;
import com.knowhow.android.picturewithai.remote.ServiceInterface;



import org.json.JSONObject;
import org.pytorch.Module;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class TakePicture extends AppCompatActivity{

    private CameraSurfaceView cameraSurfaceView; // 카메라 surfaceview


    private Bitmap nowBitmap=null;
    public Context mContext;

    private static final int PERMISSIONS_WRITE_EXTERNAL_STORAGE = 0;
    private static final double THRESHOLD = 0.95;
    private static final double box_thres1 = 1280 * 0.45;
    private static final double box_thres2 = 1280 * 0.55;
    private static final double topthres_1 = 720 * 0.35;
    private static final double topthres_2 = 720 * 0.35;

    //topthres_2 = img.size[1] * 0.55
    //bottomthres = img.size[1] * 0.9

    public SubThread subThread;

    public Boolean stop=false;

    Module module;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_take_picture);

        this.mContext = this;


        cameraSurfaceView = findViewById(R.id.camera_preview);


        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.O) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            }

        }

        ImageButton button_capture = findViewById(R.id.button);
        button_capture.setOnClickListener(view -> captureFace());

        try {
            module = Module.load(assetFilePath(this, "model.pt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            String ImgPath=getImagePathFromUri(uri);
            analyzeImage(Uri.parse(ImgPath));
        }


        public void analyzeImage(Uri uri){

//        if (Looper.myLooper() == Looper.getMainLooper()){
//            Log.d("looper", "main thread");
//        } else{
//            Log.d("looper", "not main thread");
//        }


            ServiceInterface serviceInterface = ApiConstants.getClient().create(ServiceInterface.class);


            Call<ResponseBody> call = serviceInterface.analyzeImages(prepareFilePart("image", uri));


            call.enqueue(new Callback<ResponseBody>() {



                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    Log.d("stop", String.valueOf(stop));
                    try {


                        JSONObject jObject = new JSONObject(response.body().string());
                        String message = jObject.getString("sen");
                        if (!stop) {
                            Toast toast = Toast.makeText(TakePicture.this, message, Toast.LENGTH_SHORT);

                            TextView textView = new TextView(TakePicture.this);
                            textView.setBackgroundResource(R.drawable.rounded_corner_rectangle);
                            textView.setTextColor(Color.WHITE);
                            textView.setTextSize(15);

                            textView.setPadding(20, 20, 20, 20);
                            textView.setText(message);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.setView(textView);


                            toast.show();
                            nowBitmap = cameraSurfaceView.nowBitmap;
                            Uri uri = getImageUri(TakePicture.this, nowBitmap);

                            //String ImgPath = FileUtil.getPath(TakePicture.this, uri);
                            String ImgPath = getImagePathFromUri(uri);
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
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, timeStamp, null);
        return Uri.parse(path);
    }




    public void captureFace() {


        cameraSurfaceView.capture((data, camera) -> {
            stop=true;
            subThread.interrupt();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 3;

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);


            Matrix matrix = new Matrix();
            matrix.postRotate(90);

            // We rotate the same Bitmap
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            saveImage(rotatedBitmap);
            Intent intent = new Intent(TakePicture.this, BestPicture.class);

            Uri uri = getImageUri(TakePicture.this, rotatedBitmap);
            String ImgPath=getImagePathFromUri(uri);
            intent.putExtra("path", String.valueOf(Uri.parse(ImgPath)));



            startActivity(intent);

        });
    }




    private void saveImage(Bitmap frontBitmap) {

        String root = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root);

        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = timeStamp + ".png";
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

    public String getImagePathFromUri(Uri contentUri) {

        String[] proj = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));

        cursor.close();
        return path;
    }



    @Override
    public void onResume(){
        super.onResume();

        stop=false;
        nowBitmap=null;
        cameraSurfaceView.nowBitmap=null;
        subThread = new SubThread();
        subThread.setDaemon(true);
        subThread.start();  // sub thread 시작

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stop=true;
        subThread.interrupt();
    }
}
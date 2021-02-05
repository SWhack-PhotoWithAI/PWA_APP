package com.knowhow.android.picturewithai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TakePicture extends AppCompatActivity {

    private CameraSurfaceView cameraSurfaceView; // 전면 카메라 surfaceview
    private boolean mPermissionsGranted = false;
    private static final int PERMISSIONS_REQUEST_CAMERA = 0;
    private static final int PERMISSIONS_WRITE_EXTERNAL_STORAGE = 0;
    private static final int PERMISSIONS_READ_EXTERNAL_STORAGE = 0;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        button = findViewById(R.id.button);

        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureFace();
            }
        });

    }

    public void captureFace() {

        // 식판인식 -> 얼굴인식 성공한 후 전면 카메라 화면 캡쳐
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

    /* 전면 카메라(얼굴) 이미지, realsense color 이미지 저장 */
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
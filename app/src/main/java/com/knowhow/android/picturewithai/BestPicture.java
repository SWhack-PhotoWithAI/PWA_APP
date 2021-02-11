package com.knowhow.android.picturewithai;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class BestPicture extends AppCompatActivity {

    View saveImage, shareImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_best_picture);

        if(getIntent().getExtras() != null){

            ImageView view = findViewById(R.id.bestPicture);
            Intent signupIntent = getIntent();

            String path=signupIntent.getStringExtra("path");
            Bitmap bitmap = BitmapFactory.decodeFile(path);


            view.setImageBitmap(bitmap);

            //saveImage = findViewById(R.id.saveImage);
            shareImage = findViewById(R.id.shareImage);

//            saveImage.setOnClickListener(v -> {
//
//                Toast toast = Toast.makeText(BestPicture.this,"Completely Saved!", Toast.LENGTH_SHORT);
//
//                TextView textView = new TextView(BestPicture.this);
//                textView.setBackgroundResource(R.drawable.rounded_corner_rectangle);
//                textView.setTextColor(Color.WHITE);
//                textView.setTextSize(30);
//
//                textView.setPadding(10, 10, 10, 10);
//                textView.setText(getString(R.string.saved));
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.setView(textView);
//
//
//                toast.show();
//
//                saveImage(bitmap);
//
//
//            });


            shareImage.setOnClickListener(v -> {

                Intent intent = new Intent(Intent.ACTION_SEND);

                Uri uri = getUriFromPath(path);

                intent.setType("image/*");

                intent.putExtra(Intent.EXTRA_STREAM, uri);

                Intent Sharing = Intent.createChooser(intent, "Share to");
                startActivity(Sharing);
            });


        }
    }

    private Uri getUriFromPath(String filePath) {
        long photoId;
        Uri photoUri = MediaStore.Images.Media.getContentUri("external");
        String[] projection = {MediaStore.Images.ImageColumns._ID};
        Cursor cursor = getContentResolver().query(photoUri, projection, MediaStore.Images.ImageColumns.DATA + " LIKE ?", new String[] { filePath }, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(projection[0]);
        photoId = cursor.getLong(columnIndex);

        cursor.close();
        return Uri.parse(photoUri.toString() + "/" + photoId);
    }
}





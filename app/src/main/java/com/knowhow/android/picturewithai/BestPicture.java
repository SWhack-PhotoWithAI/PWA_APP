package com.knowhow.android.picturewithai;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;



public class BestPicture extends AppCompatActivity {

    View shareImage;

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

            shareImage = findViewById(R.id.shareImage);
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





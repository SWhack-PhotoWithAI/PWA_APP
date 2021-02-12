package com.knowhow.android.picturewithai;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CAMERA = 0;
    private static final int PERMISSIONS_WRITE_EXTERNAL_STORAGE = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            }

        }

        View selectimage = findViewById(R.id.mainmenu1);
        selectimage.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SelectCategory.class);
            startActivity(intent);

        });


        View takePicture = findViewById(R.id.mainmenu2);
        takePicture.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TakePicture.class);
            startActivity(intent);
        });


        View applyFilter = findViewById(R.id.mainmenu3);

        applyFilter.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ApplyFilter.class);
            startActivity(intent);
        });
    }
}

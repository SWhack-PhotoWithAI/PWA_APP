package com.knowhow.android.picturewithai;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;


import org.pytorch.Module;


public class MainActivity extends AppCompatActivity {


    Module module = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);



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

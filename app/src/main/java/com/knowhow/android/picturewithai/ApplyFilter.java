package com.knowhow.android.picturewithai;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.knowhow.android.picturewithai.remote.ApiConstants;
import com.knowhow.android.picturewithai.remote.ServiceInterface;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApplyFilter extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 100;
    private static final int PERMISSIONS_WRITE_EXTERNAL_STORAGE = 0;

    ServiceInterface serviceInterface;

    ImageView original, filtered;
    View saveImage, shareImage;
    Uri resultUri=null;
    Bitmap resultBitmap=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_apply_filter);

        launchGalleryIntent();

        original = findViewById(R.id.orgImage);
        filtered = findViewById(R.id.filteredImage);
        saveImage = findViewById(R.id.saveImage);
        shareImage = findViewById(R.id.shareImage);

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.O) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            }

        }

        saveImage.setOnClickListener(v -> {
            if(resultBitmap!=null){
                saveImage(resultBitmap);

                Toast toast = Toast.makeText(ApplyFilter.this,"Completely Saved!", Toast.LENGTH_SHORT);

                TextView textView = new TextView(ApplyFilter.this);
                textView.setBackgroundResource(R.drawable.rounded_corner_rectangle);
                textView.setTextColor(Color.WHITE);
                textView.setTextSize(30);

                textView.setPadding(10, 10, 10, 10);
                textView.setText(getString(R.string.saved));
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.setView(textView);


                toast.show();

            }
        });


        shareImage.setOnClickListener(v -> {

            if (resultUri!=null) {


                Intent intent = new Intent(Intent.ACTION_SEND);


                intent.setType("image/*");

                intent.putExtra(Intent.EXTRA_STREAM, resultUri);

                Intent Sharing = Intent.createChooser(intent, "Share to");
                startActivity(Sharing);

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EXTERNAL_STORAGE && resultCode == RESULT_OK) {


            assert data != null;
            Uri img = data.getData();

            String imgPath = getImagePathFromUri(img);
            applyFilter(Uri.parse(imgPath));

            try {
                InputStream inputStream = getContentResolver().openInputStream(img);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                original.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }


    public String getImagePathFromUri(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":")+1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
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


    //===== Upload files to server
    public void applyFilter(Uri uri){

        serviceInterface = ApiConstants.getClient().create(ServiceInterface.class);


        Call<ResponseBody> call = serviceInterface.applyFilter(prepareFilePart("source", uri));

        ProgressBar progress = findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);


        call.enqueue(new Callback<ResponseBody>() {



            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                try {

                    progress.setVisibility(View.GONE);

                    File downloadedFile = new File(getFilesDir(), "cartoon.jpg");
                    BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
                    sink.writeAll(response.body().source());
                    sink.close();
                    String filePath = downloadedFile.getPath();
                    resultBitmap = BitmapFactory.decodeFile(filePath);
                    filtered.setImageBitmap(resultBitmap);

                    resultUri = getImageUri(ApplyFilter.this, resultBitmap);

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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);

        return Uri.parse(path);
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
}
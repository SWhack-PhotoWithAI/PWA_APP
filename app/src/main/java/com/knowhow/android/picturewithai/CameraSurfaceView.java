package com.knowhow.android.picturewithai;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.knowhow.android.picturewithai.utils.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback{

    public SurfaceHolder holder;
    public Camera camera=null;
    public Bitmap nowBitmap=null;
    public Context context;


    public CameraSurfaceView(Context context) {
        super(context);
        init(context);
        this.context=context;
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        this.context=context;
    }


    private void init(Context context) {
        holder=getHolder();
        holder.addCallback(this);
        //this.context = context;
    }


    private int findFrontSideCamera() {
        int cameraId=0;
        Camera.CameraInfo cameraInfo=new Camera.CameraInfo();
        cameraId=Camera.getNumberOfCameras();

        for(int i=0;i<cameraId;i++){

            Camera.getCameraInfo(i, cameraInfo);

            if(cameraInfo.facing==Camera.CameraInfo.CAMERA_FACING_FRONT){
                cameraId=i;
                break;
            }
        }
        return cameraId;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {



        camera = Camera.open(0);

        //int cameraId=findFrontSideCamera();

        //camera = Camera.open(cameraId);


        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            camera.setDisplayOrientation(90);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setRotation(90);
            camera.setParameters(parameters);
        }

        try{
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> sizes = parameters.getSupportedPictureSizes();

            Camera.Size mSize = sizes.get(0);
            int RESOLUTION = 640;

            int absMin = RESOLUTION;

            for (Camera.Size size : sizes) {
                if(Math.abs(size.width - RESOLUTION) < absMin) {
                    mSize = size;
                    absMin = Math.abs(size.width - RESOLUTION);
                }
            }


            parameters.setPictureSize(mSize.width, mSize.height);

            //camera.setPreviewDisplay(holder);
            camera.setParameters(parameters);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            //camera.setPreviewCallback(this);
            //camera.stopFaceDetection();

            //startFaceDetection();







        }catch(IOException e){
            e.printStackTrace();
        }
    }







    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (holder.getSurface() == null){
            // preview surface does not exist
            //Log.d(TAG, "holder.getSurface() == null");
            return;
        }

        try {
            camera.stopPreview();

        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
            //Log.d(TAG, "Error stopping camera preview: " + e.getMessage());
        }

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            camera.setPreviewCallback(this);

            //startFaceDetection(); // re-start face detection feature

        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
            //Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //camera.stopFaceDetection();
        camera.stopPreview();
        camera.release();
        camera=null;
    }


    public boolean capture(Camera.PictureCallback callback) {
        if (camera != null) {
            camera.takePicture(null, null, callback);
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

        YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

        byte[] bytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        // We rotate the same Bitmap
        nowBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);





    }
}
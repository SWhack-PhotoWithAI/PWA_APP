package com.knowhow.android.picturewithai;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    public SurfaceHolder holder;
    public Camera camera=null;


    public CameraSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        holder=getHolder();
        holder.addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        int cameraId=findFrontSideCamera();

        camera = Camera.open(cameraId);


        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            camera.setDisplayOrientation(90);
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
            //camera.stopFaceDetection();

            //startFaceDetection();

        }catch(IOException e){
            e.printStackTrace();
        }
    }



    public void startFaceDetection(){
        // Try starting Face Detection
        Camera.Parameters params = camera.getParameters();

        // start face detection only *after* preview has started
        if (params.getMaxNumDetectedFaces() > 0){
            // camera supports face detection, so can start it:
            camera.startFaceDetection();
        }
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
}
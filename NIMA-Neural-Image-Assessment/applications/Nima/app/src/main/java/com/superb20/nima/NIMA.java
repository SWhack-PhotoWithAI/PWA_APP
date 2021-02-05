package com.superb20.nima;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Superb20 on 2019-02-28.
 */

public class NIMA {
    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "NIMA";

    /**
     * A ByteBuffer to hold image data, to be feed into Tensorflow Lite as inputs.
     */
    private static ByteBuffer mImgData = null;
    private static int mTargetImageHeight;
    private static int mTargetImageWidth;

    /** Preallocated buffers for storing image data in. */
    private static int[] intValues = null;

    /**
     * Dimensions of inputs.
     */
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_PIXEL_SIZE = 3;

    /**
     * An instance of the driver class to run model inference with Tensorflow Lite.
     */
    private Interpreter mInterpreter;

    static NIMA create(AssetManager assetManager, String modelPath, int targetImageHeight, int targetImageWidth) throws IOException {
        NIMA nima = new NIMA();
        nima.mInterpreter = new Interpreter(nima.loadModelFile(assetManager, modelPath), new Interpreter.Options());
        nima.mTargetImageHeight = targetImageHeight;
        nima.mTargetImageWidth = targetImageWidth;
        mImgData = ByteBuffer.allocateDirect(DIM_BATCH_SIZE * mTargetImageHeight * mTargetImageWidth * DIM_PIXEL_SIZE * getNumBytesPerChannel());
        mImgData.order(ByteOrder.nativeOrder());
        intValues = new int[mTargetImageHeight * mTargetImageWidth];

        return nima;
    }

    public static int getNumBytesPerChannel() {
        // a 32bit float value requires 4 bytes
        return 4;
    }

    // TensorFlowLite buffer with 602112 bytes and a ByteBuffer with 31610880 bytes.
    float[][] imageAssessment(Bitmap image) {
        Log.d(TAG, "imageAssessment()");
        Bitmap resized_image = getResizedBitmap(image, mTargetImageHeight, mTargetImageWidth);
        convertBitmapToByteBuffer(resized_image);
        float[][] ret = new float[1][10];
        mInterpreter.run(mImgData, ret);

        return ret;
    }

    private void convertBitmapToByteBuffer(Bitmap image) {
        mImgData.rewind();
        image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        // Convert the image to floating point.
        int pixel = 0;
        long startTime = SystemClock.uptimeMillis();
        for (int i = 0; i < mTargetImageHeight; ++i) {
            for (int j = 0; j < mTargetImageWidth; ++j) {
                final int val = intValues[pixel++];
                addPixelValue(val);
            }
        }
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Timecost to put values into ByteBuffer: " + Long.toString(endTime - startTime));
    }

    private Bitmap getResizedBitmap(Bitmap image, int height, int width) {
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public float meanScore(float[][] result) {
        float ret = 0.f;

        for (int i = 1; i <= 10; i++)
            ret += (i * result[0][i - 1]);

        return ret;
    }

    public float stdScore(float[][] result, float mean) {
        float ret = 0.f;

        for (int i = 1; i <= 10; i++)
            ret += ((i - mean) * (i - mean) * result[0][i - 1]);

        return (float)Math.sqrt(ret);
    }

    void close() {
        mInterpreter.close();
        mInterpreter = null;
    }

    /**
     * Memory-map the model file in Assets.
     */
    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        Log.d(TAG, "Created a Tensorflow Lite NIMA.");

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    protected void addPixelValue(int pixelValue) {
        mImgData.putFloat(((pixelValue >> 16) & 0xFF) / 255.f);
        mImgData.putFloat(((pixelValue >> 8) & 0xFF) / 255.f);
        mImgData.putFloat((pixelValue & 0xFF) / 255.f);
    }
}

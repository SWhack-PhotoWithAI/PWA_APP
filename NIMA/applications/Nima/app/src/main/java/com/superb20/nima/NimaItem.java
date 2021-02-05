package com.superb20.nima;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by Superb20 on 2019-03-12.
 */

class NimaItem {
    private Uri mUri;
    private Bitmap mBitmap;
    private float mMeanScore;
    private float mStdScore;

    NimaItem(Uri uri, Bitmap bitmap, float meanScore, float stdScore) {
        mUri = uri;
        mBitmap = bitmap;
        mMeanScore = meanScore;
        mStdScore = stdScore;
    }

    float getMeanScore() {
        return mMeanScore;
    }

    public float getStdScore() {
        return mStdScore;
    }

    public Bitmap getmBitmap() {
        return mBitmap;
    }
}

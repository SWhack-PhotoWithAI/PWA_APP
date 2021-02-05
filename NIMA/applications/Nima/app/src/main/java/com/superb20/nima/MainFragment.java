package com.superb20.nima;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.superb20.nima.Common.PermissionHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Superb20 on 2019-02-14.
 */

public class MainFragment extends Fragment implements View.OnClickListener {
    private final static String TAG = "MainFragment";
    private final static String MODEL_PATH = "mobilenet_model.tflite";
    private final static int REQUEST_GALLERY = 0;
    private final static int IMAGE_RESIZE_WIDTH = 224;
    private final static int IMAGE_RESIZE_HEIGHT = 224;

    private NIMA mNima = null;
    private Executor mExecutor = Executors.newSingleThreadExecutor();
    private RecyclerView mPhotoRecyclerView;
    private List<NimaItem> mItems = new ArrayList<>();

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");

        try {
            initTensorFlowAndLoadModel();
        } catch (IOException e) {
            Log.e(TAG, "onCreate() fail");
            e.printStackTrace();
            getActivity().finish();
        }
    }

    /**
     * Layout the preview and buttons.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        return inflater.inflate(R.layout.fragment_main, parent, false);
    }

    /**
     * Connect the buttons to their event handler.
     */
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated()");
        view.findViewById(R.id.btn_load_images).setOnClickListener(this);
        mPhotoRecyclerView = view.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        //setAdopter();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume()");
        super.onResume();

        if (!PermissionHelper.hasStoragePermission(getActivity())) {
            Log.d(TAG, "has not storage permission");
            PermissionHelper.requestStoragePermission(getActivity());
            return;
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mNima.close();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_load_images:
                loadAlbum();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == REQUEST_GALLERY) {
            new EstimatePhotosTask().execute(data.getClipData());
        }
    }

    private void loadAlbum() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY);
    }

    private void setAdopter() {
        if(isAdded())
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
    }

    private void initTensorFlowAndLoadModel() throws IOException {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mNima = NIMA.create(getActivity().getAssets(), MODEL_PATH, IMAGE_RESIZE_HEIGHT, IMAGE_RESIZE_WIDTH);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView mItemImageView;
        private TextView mItemScoreView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mItemImageView = itemView.findViewById(R.id.fragment_photo_gallery_item_image_view);
            mItemScoreView = itemView.findViewById(R.id.fragment_photo_gallery_item_score_view);
        }

        public void bindItem(NimaItem item) {
            mItemImageView.setImageBitmap(item.getmBitmap());
            mItemScoreView.setText(item.getMeanScore() + " +/- " + item.getStdScore());
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<NimaItem> mNimaItems;

        public PhotoAdapter(List<NimaItem> nimaItems) {
            mNimaItems = nimaItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.photo_item, viewGroup, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            NimaItem nimaItem = mNimaItems.get(position);
            photoHolder.bindItem(nimaItem);
        }

        @Override
        public int getItemCount() {
            return mNimaItems.size();
        }
    }

    private class EstimatePhotosTask extends AsyncTask<ClipData, Void, List<NimaItem>> {
        @Override
        protected List<NimaItem> doInBackground(ClipData... params) {
            return fetchItems(params[0]);
        }

        @Override
        protected void onPostExecute(List<NimaItem> nimaItems) {
            mItems = nimaItems;
            setAdopter();
        }
    }

    List<NimaItem> fetchItems(ClipData images) {
        List<NimaItem> items = new ArrayList<>();

        for (int i = 0; i < images.getItemCount(); i++) {

            Uri selectedImage = images.getItemAt(i).getUri();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                float[][] ret = mNima.imageAssessment(bitmap);
                float meanScore = mNima.meanScore(ret);
                float stdScore = mNima.stdScore(ret, meanScore);
                Log.d(TAG, "imageAssessment(), " + selectedImage.toString() + ", " + meanScore + " +/- " + stdScore);

                NimaItem item = new NimaItem(selectedImage, bitmap, meanScore, stdScore);
                items.add(item);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return items;
    }
}

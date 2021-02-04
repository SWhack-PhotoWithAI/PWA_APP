package com.knowhow.android.picturewithai.remote;

import com.knowhow.android.picturewithai.model.FileInfo;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileService {

    @Multipart
    @POST("upload")
    Call<FileInfo> upload(
            @Part("description") RequestBody description,
            @Part MultipartBody.Part photo
    );

    @Multipart
    @POST("upload")
    Call<FileInfo> uploadPhotos(
            @Part MultipartBody.Part profile,
            @Part MultipartBody.Part panaroma
    );
}

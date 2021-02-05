package com.knowhow.android.picturewithai.remote;



import com.knowhow.android.picturewithai.model.ApiModel;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

import com.knowhow.android.picturewithai.model.ApiModel;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ServiceInterface {

    @Multipart
    @POST(ApiConstants.ENDPOINT+"upload")
    Call<ApiModel> uploadNewsFeedImages(@Part List<MultipartBody.Part> files);
}


package com.knowhow.android.picturewithai.remote;



import com.knowhow.android.picturewithai.model.ApiModel;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
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
    @POST("predict_background")
    Call<ResponseBody> predictBackground(@Part List<MultipartBody.Part> files);

    @Multipart
    @POST("predict_person")
    Call<ResponseBody> predictPerson(@Part List<MultipartBody.Part> files);

    @Multipart
    @POST("predict_person_rt")
    Call<ResponseBody> analyzeImages(@Part MultipartBody.Part file);

    @Multipart
    @POST("cartoonization")
    Call<ResponseBody> applyFilter(@Part List<MultipartBody.Part> files);


}


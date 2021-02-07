package com.knowhow.android.picturewithai.remote2;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ServiceInterface2 {

    @Multipart
    @POST("predict_person_rt")
    Call<ResponseBody> analyzeImages(@Part MultipartBody.Part file);


}

package com.knowhow.android.picturewithai;

import com.knowhow.android.picturewithai.remote.ApiConstants;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface HerokuService {
    @GET("hello")
    Call<ResponseBody> hello();
}

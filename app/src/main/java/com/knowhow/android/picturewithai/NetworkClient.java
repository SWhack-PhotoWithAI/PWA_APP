package com.knowhow.android.picturewithai;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {
    private static Retrofit retrofit;
    private static String BASE_URL = "https://photo-with-ai.herokuapp.com/";

    public static Retrofit getRetrofit() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder().
                    baseUrl(BASE_URL).client(okHttpClient).
                    addConverterFactory(GsonConverterFactory.create()).build();
        }

        return retrofit;



    }
}

package com.batp.logisticbuddy.driverapplication.service;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by kris on 9/10/16. Tokopedia
 */
public class GoogleMapApiService {
    public static final String BASE_URL = "https://maps.googleapis.com/maps/api/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
            okHttpClient.addInterceptor(interceptor);

            retrofit = new Retrofit
                    .Builder().baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient.build())
                    .build();
        }
        return retrofit;
    }
}

package com.batp.logisticbuddy.retrofit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Toped18 on 9/10/2016.
 */
public class RetrofitConnection {

    private static RetrofitConnection instance;
    private RetrofitService service;

    public static RetrofitConnection getInstance(){
        if(instance == null){
            instance = new RetrofitConnection();
        }
        return instance;
    }

    public RetrofitService createService(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        if(service == null){
            service = new Retrofit.Builder()
                    .baseUrl(RetrofitConstant.GOOGLE_MAPS_API)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build().create(RetrofitService.class);
        }
        return service;
    }

}

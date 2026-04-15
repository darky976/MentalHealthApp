package com.example.mentalhealth.api;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class SupabaseClient {
    private static Retrofit retrofit = null;

    public static SupabaseApi getInstance() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request originalRequest = chain.request();
                        Request requestWithHeaders = originalRequest.newBuilder()
                                .header("apikey", SupabaseConfig.getAnonKey())
                                .header("Content-Type", "application/json")
                                .build();
                        return chain.proceed(requestWithHeaders);
                    })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(SupabaseConfig.getBaseUrl() + "/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit.create(SupabaseApi.class);



    }
}

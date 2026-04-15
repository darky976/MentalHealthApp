package com.example.mentalhealth.api;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

public class RetrofitClient {

    public static Retrofit getClient(final String accessToken) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder rb = original.newBuilder()
                                .header("apikey", SupabaseConfig.getAnonKey());
                        if (accessToken != null && !accessToken.isEmpty()) {
                            rb.header("Authorization", "Bearer " + accessToken);
                        }
                        Request req = rb.method(original.method(), original.body()).build();
                        return chain.proceed(req);
                    }
                })
                .build();

        return new Retrofit.Builder()
                    .baseUrl(SupabaseConfig.getBaseUrl() + "/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}

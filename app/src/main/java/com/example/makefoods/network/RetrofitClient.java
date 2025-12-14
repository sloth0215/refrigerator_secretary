package com.example.makefoods.network;

import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit get(String apiKey) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @NonNull @Override
                        public Response intercept(@NonNull Chain chain) throws IOException {
                            Request newReq = chain.request().newBuilder()
                                    .addHeader("Authorization", "Bearer " + apiKey)
                                    .addHeader("Content-Type", "application/json")
                                    .build();
                            return chain.proceed(newReq);
                        }
                    }).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.openai.com/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

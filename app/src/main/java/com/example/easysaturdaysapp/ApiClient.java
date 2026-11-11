package com.example.easysaturdaysapp;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String BASE_URL = "https://easysaturdays-g4a0hhbegyhmbkhz.southafricanorth-01.azurewebsites.net";
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {

            // Create a custom HTTP client to ensure clean requests
            OkHttpClient client = new OkHttpClient.Builder()
                    .followRedirects(false) // Stop the 307 redirect
                    .followSslRedirects(false)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client) // Attach the custom client
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
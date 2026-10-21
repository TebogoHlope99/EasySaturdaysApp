package com.example.easysaturdaysapp;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @GET("api/campaigns")
    Call<List<Campaign>> getCampaigns();

    @POST("api/login")
    Call<User> login(@Body User loginData);

    @POST("api/register")
    Call<ApiResponse> register(@Body User user);

    @GET("api/dashboard/{userId}")
    Call<DashboardData> getDashboard(@Path("userId") int userId);

    @POST("api/profile/update")
    Call<ApiResponse> updateProfile(@Body User user);

    @POST("api/volunteer/signup")
    Call<ApiResponse> signUpShift(@Body ShiftRequest request);

    @POST("api/volunteer/cancel")
    Call<ApiResponse> cancelShift(@Body CancelShiftRequest request);

    @POST("api/donate")
    Call<ApiResponse> donate(@Body DonateRequest request);
}
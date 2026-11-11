package com.example.easysaturdaysapp;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    // --- PUBLIC & USER ENDPOINTS ---
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

    // --- ADMIN ENDPOINTS ---
    @GET("api/admin/users")
    Call<List<AdminUser>> getAllUsers();

    @GET("api/admin/donations")
    Call<List<AdminDonation>> getAllDonations();

    @GET("api/admin/shifts")
    Call<List<AdminShift>> getAllShifts();

    @GET("api/admin/reports")
    Call<AdminReport> getReports();

    @POST("api/admin/campaign/create")
    Call<ApiResponse> createCampaign(@Body Campaign campaign);

    @POST("api/admin/campaign/edit")
    Call<ApiResponse> editCampaign(@Body Campaign campaign);

    @POST("api/admin/campaign/delete/{id}")
    Call<ApiResponse> deleteCampaign(@Path("id") int id);

    @POST("api/admin/user/delete/{id}")
    Call<ApiResponse> deleteUser(@Path("id") int id);

    // --- SUPER ADMIN ENDPOINTS ---
    @POST("api/superadmin/request")
    Call<ApiResponse> createAdminRequest(@Body AdminRequest request);

    @GET("api/superadmin/requests")
    Call<List<AdminRequest>> getAdminRequests();

    @POST("api/superadmin/process")
    Call<ApiResponse> processAdminRequest(@Body ProcessRequest process);
}
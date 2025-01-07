package com.example.ekthacares;


import com.example.ekthacares.model.ApiResponse;
import com.example.ekthacares.model.BloodDonation;
import com.example.ekthacares.model.DonationResponse;
import com.example.ekthacares.model.SentEmail;
import com.example.ekthacares.model.User;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Send OTP to mobile number
    @POST("/api/app login")
    Call<ResponseBody> login(@Query("mobile") String mobile);

    // Validate OTP
    @POST("/api/app/validateOtp")
    Call<String> validateOtp(@Query("mobile") String mobile, @Query("otp") String otp);

    // Resend OTP
    @POST("/resendOtp")
    Call<String> resendOtp(@Query("mobile") String mobile);

    @GET("/api/app/viewProfile")
    Call<User> getUserProfile(@Query("userId") String userId, @Query("sessionToken") String sessionToken);

    @GET("api/app/donorhome")
    Call<User> getDonorHome(@Header("Authorization") String authorization);

    @GET("/api/appuser")
    Call<User> getUserDetails(@Header("Authorization") String token, @Query("id") Long id);

    @POST("/api/app/register")
    Call<ApiResponse> registerUser(
            @Body User user
    );

    // Update user profile
    @POST("/api/app/updateProfile")
    Call<ApiResponse> appupdateProfile(
            @Header("Authorization") String authorization,
            @Body User user
    );

    @GET("api/app/mydonations")  // Ensure this endpoint matches your backend controller's endpoint
    Call<DonationResponse> getDonations(
            @Header("Authorization") String authHeader,
            @Query("userId") Long userId
    );

    @FormUrlEncoded
    @POST("/api/app/addDonation")
    Call<Map<String, Object>> addDonation(
            @Field("userId") Long userId,
            @Field("lastDonationDate") String lastDonationDate, // Format: "yyyy-MM-dd'T'HH:mm:ss"
            @Field("hospitalName") String hospitalName
    );

    @GET("/api/app/sentEmails")
    Call<List<SentEmail>> getSentEmails(@Header("Authorization") String token, @Header("userId") Long userId);
}



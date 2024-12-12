package com.example.ekthacares;


import com.example.ekthacares.model.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // Send OTP to mobile number
    @POST("/api/app login")
    Call<String> login(@Query("mobile") String mobile);

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

    @GET("/api/user")
    Call<User> getUserDetails(@Header("Authorization") String token, @Query("id") Long id);
}

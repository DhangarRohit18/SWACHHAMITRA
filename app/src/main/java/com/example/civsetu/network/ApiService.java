package com.example.civsetu.network;

import com.example.civsetu.model.Issue;
import com.example.civsetu.model.Survey;
import com.example.civsetu.model.User;
import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

        @GET("/api/issues/user")
        Call<List<Issue>> getUserIssues();

        @GET("/api/issues/all")
        Call<List<Issue>> getAllIssues();

        @Multipart
        @POST("/api/issues/submit")
        Call<Issue> submitIssue(
                        @Part("title") RequestBody title,
                        @Part("description") RequestBody description,
                        @Part("category") RequestBody category,
                        @Part("address") RequestBody address,
                        @Part("latitude") RequestBody latitude,
                        @Part("longitude") RequestBody longitude,
                        @Part List<MultipartBody.Part> images,
                        @Part MultipartBody.Part voice);

        @POST("/api/issues/{id}/engage")
        Call<Void> engageIssue(@Path("id") String id, @Body Map<String, Object> body);

        @PUT("/api/issues/{id}/status")
        Call<Void> updateIssueStatus(@Path("id") String id, @Body Map<String, String> body);

        @POST("/api/issues/{id}/resolve")
        Call<Void> resolveIssue(@Path("id") String id, @Body Map<String, Object> body);

        @GET("/api/surveys")
        Call<List<Survey>> getSurveys();

        @POST("/api/surveys/{id}/respond")
        Call<Void> respondToSurvey(@Path("id") String id, @Body Map<String, Object> response);

        // Sustainability & Gamification (SwachhMitra Features)
        @GET("/api/sustainability/stats")
        Call<Map<String, Object>> getSustainabilityStats();

        @GET("/api/sustainability/leaderboard")
        Call<List<User>> getLeaderboard();

        @POST("/api/sustainability/points/award")
        Call<Void> awardPoints(@Body Map<String, Object> body);
}

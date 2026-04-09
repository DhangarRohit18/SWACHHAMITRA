package com.example.civsetu.surveys;

import android.content.Context;
import com.example.civsetu.model.Survey;
import com.example.civsetu.network.ApiClient;
import com.example.civsetu.network.ApiService;
import java.util.List;
import java.util.Map;
import retrofit2.Call;

public class SurveyRepository {
    private ApiService apiService;

    public SurveyRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public Call<List<Survey>> getSurveys() {
        return apiService.getSurveys();
    }

    public Call<Void> respondToSurvey(String id, Map<String, Object> response) {
        return apiService.respondToSurvey(id, response);
    }
}

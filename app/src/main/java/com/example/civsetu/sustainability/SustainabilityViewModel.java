package com.example.civsetu.sustainability;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.civsetu.model.User;
import com.example.civsetu.network.ApiClient;
import com.example.civsetu.network.ApiService;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SustainabilityViewModel extends AndroidViewModel {
    private MutableLiveData<List<User>> leaderboard = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<User> currentUserStats = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private ApiService apiService;

    public SustainabilityViewModel(@NonNull android.app.Application application) {
        super(application);
        apiService = ApiClient.getApiService(application);
    }

    public LiveData<List<User>> getLeaderboard() {
        return leaderboard;
    }

    public LiveData<User> getCurrentUserStats() {
        return currentUserStats;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchLeaderboard() {
        apiService.getLeaderboard().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    leaderboard.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to fetch leaderboard");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                errorMessage.setValue(t.getMessage());
            }
        });
    }

    public void fetchUserStats() {
        // Mocking for now or based on a new endpoint if available
        // Similar to above, but for single user
    }
}

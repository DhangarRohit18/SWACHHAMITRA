package com.example.civsetu.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;

public class AuthViewModel extends AndroidViewModel {
    private AuthRepository repository;
    private MutableLiveData<AuthResult> authResult = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    public LiveData<AuthResult> getAuthResult() {
        return authResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void login(String email, String password) {
        repository.login(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                repository.fetchAndSaveToken();
                authResult.setValue(task.getResult());
            } else {
                errorMessage.setValue(task.getException().getMessage());
            }
        });
    }

    public void register(String email, String password, String role) {
        repository.register(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                repository.setRole(role);
                repository.fetchAndSaveToken();
                authResult.setValue(task.getResult());
            } else {
                errorMessage.setValue(task.getException().getMessage());
            }
        });
    }

    public void loginWithGoogle(AuthCredential credential) {
        repository.loginWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                repository.fetchAndSaveToken();
                authResult.setValue(task.getResult());
            } else {
                errorMessage.setValue(task.getException().getMessage());
            }
        });
    }
}

package com.example.civsetu.auth;

import android.content.Context;
import androidx.annotation.NonNull;
import com.example.civsetu.utils.PreferenceManager;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {
    private FirebaseAuth firebaseAuth;
    private PreferenceManager preferenceManager;

    public AuthRepository(Context context) {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.preferenceManager = PreferenceManager.getInstance(context);
    }

    public Task<AuthResult> login(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> register(String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> loginWithCredential(AuthCredential credential) {
        return firebaseAuth.signInWithCredential(credential);
    }

    public void fetchAndSaveToken() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.getIdToken(true).addOnSuccessListener(result -> {
                preferenceManager.saveToken(result.getToken());
                // In a real app, role would come from backend or Firestore
                // Defaulting to Citizen for demo if not set
                if (preferenceManager.getRole() == null) {
                    preferenceManager.saveRole("Citizen");
                }
            });
        }
    }

    public String getUserRole() {
        return preferenceManager.getRole();
    }

    public void setRole(String role) {
        preferenceManager.saveRole(role);
    }

    public void logout() {
        firebaseAuth.signOut();
        preferenceManager.clear();
    }
}

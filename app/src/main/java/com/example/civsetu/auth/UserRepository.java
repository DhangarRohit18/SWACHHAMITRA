package com.example.civsetu.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.civsetu.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public LiveData<User> getRealTimeUserProfile() {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (uid != null) {
            db.collection("users").document(uid)
                    .addSnapshotListener((value, error) -> {
                        if (error != null)
                            return;
                        if (value != null && value.exists()) {
                            userLiveData.setValue(value.toObject(User.class));
                        }
                    });
        }
        return userLiveData;
    }

    public void updateImpactPoints(int points) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid != null) {
            db.collection("users").document(uid).update("impactPoints",
                    com.google.firebase.firestore.FieldValue.increment(points));
        }
    }
}

package com.example.civsetu.report;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.civsetu.model.AppDatabase;
import com.example.civsetu.model.Comment;
import com.example.civsetu.model.Issue;
import com.example.civsetu.model.IssueDao;
import com.example.civsetu.network.ApiClient;
import com.example.civsetu.network.ApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class IssueRepository {
    private ApiService apiService;
    private FirebaseFirestore db;
    private IssueDao issueDao;
    private ExecutorService executor;
    private Context context;

    public IssueRepository(Context context) {
        this.context = context;
        this.apiService = ApiClient.getApiService(context);
        this.db = FirebaseFirestore.getInstance();
        this.issueDao = AppDatabase.getInstance(context).issueDao();
        this.executor = Executors.newSingleThreadExecutor();

        syncLocalIssues();
    }

    public void syncLocalIssues() {
        executor.execute(() -> {
            try {
                List<Issue> offlineIssues = issueDao.getAllIssues();
                if (offlineIssues == null)
                    return;

                for (Issue issue : offlineIssues) {
                    String id = issue.getId();
                    if (id == null || id.trim().isEmpty())
                        continue;

                    db.collection("issues").document(id).get()
                            .addOnFailureListener(e -> {
                                saveIssueToFirestore(issue, new OnCompleteListener() {
                                    @Override
                                    public void onSuccess(Issue result) {
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                    }
                                });
                            });
                }
            } catch (Exception e) {
                Log.e("IssueRepository", "Sync error", e);
            }
        });
    }

    public LiveData<List<Issue>> getRealTimeUserIssues() {
        MutableLiveData<List<Issue>> issuesLiveData = new MutableLiveData<>();
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId != null) {
            db.collection("issues")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            executor.execute(() -> {
                                List<Issue> localIssues = issueDao.getUserIssues(userId);
                                issuesLiveData.postValue(localIssues);
                            });
                            return;
                        }
                        if (value != null) {
                            List<Issue> issues = value.toObjects(Issue.class);
                            issuesLiveData.setValue(issues);
                            executor.execute(() -> issueDao.insertIssues(issues));
                        }
                    });
        }
        return issuesLiveData;
    }

    public LiveData<List<Issue>> getLocalUserIssues(String userId) {
        return issueDao.getUserIssuesLive(userId);
    }

    public LiveData<List<Issue>> getLocalAllIssues() {
        return issueDao.getAllIssuesLive();
    }

    public LiveData<List<Issue>> getRealTimeAllIssues() {
        MutableLiveData<List<Issue>> issuesLiveData = new MutableLiveData<>();
        db.collection("issues")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        executor.execute(() -> {
                            List<Issue> localIssues = issueDao.getAllIssues();
                            issuesLiveData.postValue(localIssues);
                        });
                        return;
                    }
                    if (value != null) {
                        try {
                            List<Issue> issues = value.toObjects(Issue.class);
                            issuesLiveData.setValue(issues);
                            executor.execute(() -> issueDao.insertIssues(issues));
                        } catch (Exception e) {
                            Log.e("IssueRepository", "Deserialization error", e);
                        }
                    }
                });
        return issuesLiveData;
    }

    public LiveData<Issue> getRealTimeIssue(String id) {
        MutableLiveData<Issue> issueLiveData = new MutableLiveData<>();
        if (id == null || id.isEmpty())
            return issueLiveData;

        db.collection("issues").document(id)
                .addSnapshotListener((value, error) -> {
                    if (error != null)
                        return;
                    if (value != null && value.exists()) {
                        issueLiveData.setValue(value.toObject(Issue.class));
                    }
                });
        return issueLiveData;
    }

    public void submitIssueRealTime(Issue issue, List<File> images, File voice, OnCompleteListener listener) {
        executor.execute(() -> {
            try {
                String id = db.collection("issues").document().getId();
                issue.setId(id);
                issue.setCreatedAt(System.currentTimeMillis());
                issue.setUserId(FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                        : "anonymous");
                issue.setStatus("Pending");
                if (issue.getPriority() == null) {
                    issue.setPriority("Medium");
                }

                List<String> base64Images = new ArrayList<>();
                if (images != null) {
                    for (File file : images) {
                        String encoded = processAndEncodeImage(file);
                        if (encoded != null)
                            base64Images.add(encoded);
                    }
                }
                issue.setImageUrls(base64Images);

                if (voice != null && voice.exists()) {
                    String encodedVoice = encodeVoiceToBase64(voice);
                    issue.setVoiceUrl(encodedVoice);
                }

                saveIssueToFirestore(issue, listener);
            } catch (Exception e) {
                Log.e("IssueRepository", "Submission error", e);
                listener.onFailure("Submission failed: " + e.getMessage());
            }
        });
    }

    private String processAndEncodeImage(File file) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            if (bitmap == null)
                return null;

            int maxWidth = 500;
            int maxHeight = 500;
            float ratio = Math.min((float) maxWidth / bitmap.getWidth(), (float) maxHeight / bitmap.getHeight());
            if (ratio < 1.0f) {
                bitmap = Bitmap.createScaledBitmap(bitmap,
                        Math.round(ratio * bitmap.getWidth()),
                        Math.round(ratio * bitmap.getHeight()), true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
            byte[] b = baos.toByteArray();
            return "data:image/jpeg;base64," + Base64.encodeToString(b, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e("IssueRepository", "Image processing failed", e);
            return null;
        }
    }

    private String encodeVoiceToBase64(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            int bytesRead = fis.read(bytes);
            fis.close();
            if (bytesRead > 0) {
                return "data:audio/m4a;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP);
            }
            return null;
        } catch (Exception e) {
            Log.e("IssueRepository", "Voice encoding failed", e);
            return null;
        }
    }

    private void saveIssueToFirestore(Issue issue, OnCompleteListener listener) {
        if (issue.getId() == null || issue.getId().isEmpty()) {
            listener.onFailure("Invalid Issue ID");
            return;
        }
        db.collection("issues").document(issue.getId()).set(issue)
                .addOnSuccessListener(aVoid -> {
                    executor.execute(() -> issueDao.insertIssue(issue));
                    listener.onSuccess(issue);
                })
                .addOnFailureListener(e -> {
                    executor.execute(() -> issueDao.insertIssue(issue));
                    listener.onFailure("Firestore error: " + e.getMessage());
                });
    }

    public void updateIssueStatusRealTime(String id, String status, String remarks, OnCompleteListener listener) {
        if (id == null || id.isEmpty())
            return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        if (remarks != null)
            updates.put("officialRemarks", remarks);

        if ("Resolved".equalsIgnoreCase(status)) {
            updates.put("resolvedAt", System.currentTimeMillis());
        }

        db.collection("issues").document(id).update(updates)
                .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void deleteIssue(String id, OnCompleteListener listener) {
        if (id == null || id.isEmpty())
            return;
        db.collection("issues").document(id).delete()
                .addOnSuccessListener(aVoid -> {
                    executor.execute(() -> issueDao.deleteIssueById(id));
                    listener.onSuccess(null);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void clearOldData(OnCompleteListener listener) {
        db.collection("issues").get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                batch.delete(doc.getReference());
            }
            batch.commit()
                    .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
        });
    }

    public LiveData<List<Comment>> getRealTimeComments(String issueId) {
        MutableLiveData<List<Comment>> commentsLiveData = new MutableLiveData<>();
        if (issueId == null || issueId.isEmpty())
            return commentsLiveData;

        db.collection("issues").document(issueId).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null)
                        return;
                    if (value != null) {
                        commentsLiveData.setValue(value.toObjects(Comment.class));
                    }
                });
        return commentsLiveData;
    }

    public void addCommentRealTime(String issueId, Comment comment, OnCompleteListener listener) {
        if (issueId == null || issueId.isEmpty())
            return;
        String commentId = db.collection("issues").document(issueId).collection("comments").document().getId();
        db.collection("issues").document(issueId).collection("comments").document(commentId)
                .set(comment)
                .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void upvoteIssue(String issueId, OnCompleteListener listener) {
        if (issueId == null || issueId.isEmpty())
            return;
        db.collection("issues").document(issueId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                long currentUpvotes = (snapshot.get("upvotes") != null) ? (long) snapshot.get("upvotes") : 0;
                db.collection("issues").document(issueId).update("upvotes", currentUpvotes + 1)
                        .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                        .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
            }
        });
    }

    public interface OnCompleteListener {
        void onSuccess(Issue issue);

        void onFailure(String error);
    }

    public void fetchUserIssues(String userId, OnCompleteListener listener) {
        apiService.getUserIssues().enqueue(new retrofit2.Callback<List<Issue>>() {
            @Override
            public void onResponse(Call<List<Issue>> call, retrofit2.Response<List<Issue>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executor.execute(() -> {
                        issueDao.insertIssues(response.body());
                        if (listener != null) listener.onSuccess(null);
                    });
                } else if (listener != null) {
                    listener.onFailure("API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Issue>> call, Throwable t) {
                if (listener != null) listener.onFailure(t.getMessage());
            }
        });
    }

    public Call<List<Issue>> getUserIssues() {
        return apiService.getUserIssues();
    }

    public Call<List<Issue>> getAllIssues() {
        return apiService.getAllIssues();
    }

    public Call<Issue> submitIssue(String title, String description, String category, String address,
            double lat, double lon, List<File> imageFiles, File voiceFile) {

        RequestBody rbTitle = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody rbDesc = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody rbCat = RequestBody.create(MediaType.parse("text/plain"), category);
        RequestBody rbAddr = RequestBody.create(MediaType.parse("text/plain"), address);
        RequestBody rbLat = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(lat));
        RequestBody rbLon = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(lon));

        List<MultipartBody.Part> imageParts = new ArrayList<>();
        if (imageFiles != null) {
            for (File file : imageFiles) {
                RequestBody rbImg = RequestBody.create(MediaType.parse("image/*"), file);
                imageParts.add(MultipartBody.Part.createFormData("images[]", file.getName(), rbImg));
            }
        }

        MultipartBody.Part voicePart = null;
        if (voiceFile != null) {
            RequestBody rbVoice = RequestBody.create(MediaType.parse("audio/*"), voiceFile);
            voicePart = MultipartBody.Part.createFormData("voice", voiceFile.getName(), rbVoice);
        }

        return apiService.submitIssue(rbTitle, rbDesc, rbCat, rbAddr, rbLat, rbLon, imageParts, voicePart);
    }
}

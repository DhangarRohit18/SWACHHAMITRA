package com.example.civsetu.report;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.civsetu.model.Comment;
import com.example.civsetu.model.Issue;
import java.io.File;
import java.util.List;

public class IssueViewModel extends AndroidViewModel {
    private IssueRepository repository;
    private MediatorLiveData<List<Issue>> userIssues = new MediatorLiveData<>();
    private MediatorLiveData<List<Issue>> allIssues = new MediatorLiveData<>();
    private MediatorLiveData<List<Comment>> issueComments = new MediatorLiveData<>();
    private MutableLiveData<Issue> submissionResult = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public IssueViewModel(@NonNull Application application) {
        super(application);
        repository = new IssueRepository(application);

        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonymous";

        // Observe local DB for UI updates (Offline-first)
        userIssues.addSource(repository.getLocalUserIssues(userId), issues -> {
            userIssues.setValue(issues);
        });

        allIssues.addSource(repository.getLocalAllIssues(), issues -> {
            allIssues.setValue(issues);
        });

        // Initialize background listeners in repository (already handled by repository constructor)
    }

    public LiveData<List<Issue>> getUserIssues() {
        return userIssues;
    }

    public LiveData<List<Issue>> getAllIssues() {
        return allIssues;
    }

    public LiveData<Issue> getIssueDetails(String id) {
        return repository.getRealTimeIssue(id);
    }

    public LiveData<List<Comment>> getIssueComments() {
        return issueComments;
    }

    public LiveData<Issue> getSubmissionResult() {
        return submissionResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchUserIssues() {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonymous";
        repository.fetchUserIssues(userId, new IssueRepository.OnCompleteListener() {
            @Override public void onSuccess(Issue result) {}
            @Override public void onFailure(String error) { errorMessage.setValue(error); }
        });
    }

    public void fetchAllIssues() {
        // Fallback or explicit trigger for all issues
    }

    public void observeComments(String issueId) {
        issueComments.addSource(repository.getRealTimeComments(issueId), comments -> {
            issueComments.setValue(comments);
        });
    }

    public void addComment(String issueId, Comment comment) {
        repository.addCommentRealTime(issueId, comment, new IssueRepository.OnCompleteListener() {
            @Override
            public void onSuccess(Issue issue) {
                // Success
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    public void updateIssueStatus(String id, String status, String remarks) {
        repository.updateIssueStatusRealTime(id, status, remarks, new IssueRepository.OnCompleteListener() {
            @Override
            public void onSuccess(Issue issue) {
                // Status updated
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    public void submitIssue(String title, String description, String category, String address,
            double lat, double lon, List<File> imageFiles, File voiceFile) {

        Issue issue = new Issue();
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setCategory(category);
        issue.setAddress(address);
        issue.setLatitude(lat);
        issue.setLongitude(lon);

        repository.submitIssueRealTime(issue, imageFiles, voiceFile, new IssueRepository.OnCompleteListener() {
            @Override
            public void onSuccess(Issue submittedIssue) {
                submissionResult.setValue(submittedIssue);
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    public void upvoteIssue(String issueId) {
        repository.upvoteIssue(issueId, new IssueRepository.OnCompleteListener() {
            @Override public void onSuccess(Issue result) {}
            @Override public void onFailure(String error) { errorMessage.setValue(error); }
        });
    }

    public void resetDatabase() {
        repository.clearOldData(new IssueRepository.OnCompleteListener() {
            @Override
            public void onSuccess(Issue issue) {
                // Database cleared
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue(error);
            }
        });
    }
}

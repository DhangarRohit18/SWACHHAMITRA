package com.example.civsetu.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.civsetu.model.Issue;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class IssueClusterItem implements ClusterItem {
    private final LatLng position;
    private final String title;
    private final String snippet;
    private final Issue issue;

    public IssueClusterItem(Issue issue) {
        this.issue = issue;
        this.position = new LatLng(issue.getLatitude(), issue.getLongitude());
        this.title = issue.getTitle();
        this.snippet = issue.getCategory();
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }

    @Nullable
    @Override
    public Float getZIndex() {
        return 0f;
    }

    public Issue getIssue() {
        return issue;
    }
}

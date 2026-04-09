package com.example.civsetu.map;

import android.content.Context;
import androidx.annotation.Nullable;
import com.example.civsetu.model.Issue;
import com.example.civsetu.utils.Constants;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class ClusterManagerHelper {

    public static class IssueClusterItem implements ClusterItem {
        private final Issue issue;

        public IssueClusterItem(Issue issue) {
            this.issue = issue;
        }

        @Override
        public LatLng getPosition() {
            return new LatLng(issue.getLatitude(), issue.getLongitude());
        }

        @Override
        public String getTitle() {
            return issue.getTitle();
        }

        @Override
        public String getSnippet() {
            return issue.getDescription();
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

    public static class IssueRenderer extends DefaultClusterRenderer<IssueClusterItem> {
        public IssueRenderer(Context context, GoogleMap map, ClusterManager<IssueClusterItem> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(IssueClusterItem item, MarkerOptions markerOptions) {
            String priority = item.getIssue().getPriority();
            float color = BitmapDescriptorFactory.HUE_BLUE;
            if (priority != null) {
                switch (priority.toLowerCase()) {
                    case Constants.PRIORITY_CRITICAL: color = BitmapDescriptorFactory.HUE_RED; break;
                    case Constants.PRIORITY_HIGH: color = BitmapDescriptorFactory.HUE_ORANGE; break;
                    case Constants.PRIORITY_MEDIUM: color = BitmapDescriptorFactory.HUE_BLUE; break;
                    case Constants.PRIORITY_LOW: color = BitmapDescriptorFactory.HUE_GREEN; break;
                }
            }
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(color));
        }
    }
}

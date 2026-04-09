package com.example.civsetu.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.util.List;
import java.util.Map;

@Entity(tableName = "issues")
@TypeConverters(Converters.class)
public class Issue {
    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private String description;
    private String category;
    private String address;
    private double latitude;
    private double longitude;
    private List<String> imageUrls;
    private String voiceUrl;
    private String priority;
    private String aiReasoning;
    private String status;
    private long createdAt;
    private long resolvedAt;
    private String userId;
    private String assignedTo;
    private String department;
    private int upvotes;

    // Standard Fields used across different versions of the DB
    @Ignore
    private String daysRemaining;
    @Ignore
    private String adminEscalatedPriority;
    @Ignore
    private String aiAnalysis;
    @Ignore
    private String verifications;
    @Ignore
    private String slaStatus;
    @Ignore
    private String officialRemarks;
    @Ignore
    private String uid;
    @Ignore
    private String updatedAt;
    @Ignore
    private Object timestamp;
    @Ignore
    private long agreeCount;
    @Ignore
    private long disagreeCount;
    @Ignore
    private List<Map<String, Object>> comments;
    @Ignore
    private boolean isValid;
    @Ignore
    private Map<String, Double> coordinates;
    @Ignore
    private List<String> resolutionProofs;
    @Ignore
    private String resolutionRemarks;
    @Ignore
    private Integer slaDays;
    @Ignore
    private Object slaEndDate;
    @Ignore
    private Object slaStartDate;
    @Ignore
    private List<String> files;
    @Ignore
    private String location;
    @Ignore
    private List<String> staffAllocated;
    @Ignore
    private Object reportedAt;
    @Ignore
    private List<String> planningDocs;
    @Ignore
    private String actionTaken;
    @Ignore
    private String resourcesUsed;

    public Issue() {
        this.id = "";
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAiReasoning() {
        return aiReasoning;
    }

    public void setAiReasoning(String aiReasoning) {
        this.aiReasoning = aiReasoning;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @Exclude
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Ignore
    @PropertyName("createdAt")
    public void setCreatedAtFromObject(Object createdAt) {
        this.createdAt = convertToLong(createdAt);
    }

    public long getResolvedAt() {
        return resolvedAt;
    }

    @Exclude
    public void setResolvedAt(long resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    @Ignore
    @PropertyName("resolvedAt")
    public void setResolvedAtFromObject(Object resolvedAt) {
        this.resolvedAt = convertToLong(resolvedAt);
    }

    private long convertToLong(Object val) {
        if (val instanceof Long)
            return (Long) val;
        if (val instanceof String) {
            try {
                return Long.parseLong((String) val);
            } catch (Exception e) {
                return 0;
            }
        }
        if (val instanceof Double)
            return ((Double) val).longValue();
        return 0;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    // Mock Getters/Setters for ignoring Firestore fields
    public String getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(Object val) {
        this.daysRemaining = String.valueOf(val);
    }

    public String getAdminEscalatedPriority() {
        return adminEscalatedPriority;
    }

    public void setAdminEscalatedPriority(String val) {
        this.adminEscalatedPriority = val;
    }

    public String getAiAnalysis() {
        return aiAnalysis;
    }

    public void setAiAnalysis(String val) {
        this.aiAnalysis = val;
    }

    public String getVerifications() {
        return verifications;
    }

    public void setVerifications(String val) {
        this.verifications = val;
    }

    public String getSlaStatus() {
        return slaStatus;
    }

    public void setSlaStatus(String val) {
        this.slaStatus = val;
    }

    public String getOfficialRemarks() {
        return officialRemarks;
    }

    public void setOfficialRemarks(String val) {
        this.officialRemarks = val;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String val) {
        this.uid = val;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String val) {
        this.updatedAt = val;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object val) {
        this.timestamp = val;
    }

    public long getAgreeCount() {
        return agreeCount;
    }

    public void setAgreeCount(long val) {
        this.agreeCount = val;
    }

    public long getDisagreeCount() {
        return disagreeCount;
    }

    public void setDisagreeCount(long val) {
        this.disagreeCount = val;
    }

    public List<Map<String, Object>> getComments() {
        return comments;
    }

    public void setComments(List<Map<String, Object>> val) {
        this.comments = val;
    }

    public boolean isIsValid() {
        return isValid;
    }

    public void setIsValid(boolean val) {
        isValid = val;
    }

    public Map<String, Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Map<String, Double> val) {
        this.coordinates = val;
    }

    public List<String> getResolutionProofs() {
        return resolutionProofs;
    }

    public void setResolutionProofs(List<String> val) {
        this.resolutionProofs = val;
    }

    public String getResolutionRemarks() {
        return resolutionRemarks;
    }

    public void setResolutionRemarks(String val) {
        this.resolutionRemarks = val;
    }

    public Integer getSlaDays() {
        return slaDays;
    }

    public void setSlaDays(Integer val) {
        this.slaDays = val;
    }

    public Object getSlaEndDate() {
        return slaEndDate;
    }

    public void setSlaEndDate(Object val) {
        this.slaEndDate = val;
    }

    public Object getSlaStartDate() {
        return slaStartDate;
    }

    public void setSlaStartDate(Object val) {
        this.slaStartDate = val;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> val) {
        this.files = val;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String val) {
        this.location = val;
    }

    public List<String> getStaffAllocated() {
        return staffAllocated;
    }

    public void setStaffAllocated(List<String> val) {
        this.staffAllocated = val;
    }

    public Object getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(Object val) {
        this.reportedAt = val;
    }

    public List<String> getPlanningDocs() {
        return planningDocs;
    }

    public void setPlanningDocs(List<String> val) {
        this.planningDocs = val;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String val) {
        this.actionTaken = val;
    }

    public String getResourcesUsed() {
        return resourcesUsed;
    }

    public void setResourcesUsed(String val) {
        this.resourcesUsed = val;
    }
}

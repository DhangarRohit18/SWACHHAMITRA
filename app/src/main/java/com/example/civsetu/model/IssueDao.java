package com.example.civsetu.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface IssueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIssues(List<Issue> issues);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIssue(Issue issue);

    @Query("SELECT * FROM issues ORDER BY createdAt DESC")
    androidx.lifecycle.LiveData<List<Issue>> getAllIssuesLive();

    @Query("SELECT * FROM issues ORDER BY createdAt DESC")
    List<Issue> getAllIssues();

    @Query("SELECT * FROM issues WHERE userId = :userId ORDER BY createdAt DESC")
    androidx.lifecycle.LiveData<List<Issue>> getUserIssuesLive(String userId);

    @Query("SELECT * FROM issues WHERE userId = :userId ORDER BY createdAt DESC")
    List<Issue> getUserIssues(String userId);

    @Query("SELECT * FROM issues WHERE id = :id")
    Issue getIssueById(String id);

    @Update
    void updateIssue(Issue issue);

    @Query("DELETE FROM issues WHERE id = :id")
    void deleteIssueById(String id);
}

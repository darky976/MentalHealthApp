package com.example.mentalhealth.models;

import com.google.gson.annotations.SerializedName;

public class MoodEntry {
    public String id;

    @SerializedName("user_id")
    public String userId;

    public String date;
    public int mood;
    public String comment;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    public MoodEntry(String id, String userId, String date, int mood, String comment, String createdAt, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.mood = mood;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public MoodEntry() {
    }
}

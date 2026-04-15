package com.example.mentalhealth.models;

import com.google.gson.annotations.SerializedName;

public class ChatSession {
    public String id;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("created_at")
    public String createdAt;

    public ChatSession(String id, String userId, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public ChatSession() {
    }
}

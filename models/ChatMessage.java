package com.example.mentalhealth.models;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {
    public String id;

    @SerializedName("chat_session_id")
    public String chatSessionId;

    @SerializedName("user_id")
    public String userId;

    public String role; // "user" или "assistant"
    public String message;

    @SerializedName("created_at")
    public String createdAt;

    public ChatMessage(String id, String chatSessionId, String userId, String role, String message, String createdAt) {
        this.id = id;
        this.chatSessionId = chatSessionId;
        this.userId = userId;
        this.role = role;
        this.message = message;
        this.createdAt = createdAt;
    }

    public ChatMessage() {
    }
}


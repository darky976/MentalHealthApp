package com.example.mentalhealth.models;

import com.google.gson.annotations.SerializedName;

public class Notebook {
    public String id;

    @SerializedName("user_id")
    public String userId;

    public String title;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    public Notebook(String id, String userId, String title, String createdAt, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Notebook() {
    }
}

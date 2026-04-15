package com.example.mentalhealth.models;

import com.google.gson.annotations.SerializedName;

public class Note {
    public String id;

    @SerializedName("notebook_id")
    public String notebookId;

    @SerializedName("user_id")
    public String userId;

    public String content;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    // Local-only fields for notebook UX.
    public boolean pinned;
    public boolean bold;
    public boolean italic;
    public boolean archived;
    public long localCreatedAt;
    public String containerName;

    public Note(String id, String notebookId, String userId, String content, String createdAt, String updatedAt) {
        this.id = id;
        this.notebookId = notebookId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.pinned = false;
        this.bold = false;
        this.italic = false;
        this.archived = false;
        this.localCreatedAt = System.currentTimeMillis();
        this.containerName = "";
    }

    public Note() {
        this.pinned = false;
        this.bold = false;
        this.italic = false;
        this.archived = false;
        this.localCreatedAt = System.currentTimeMillis();
        this.containerName = "";
    }
}

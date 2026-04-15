package com.example.mentalhealth.models;

public class User {
    public String id;
    public String email;
    public long createdAt;

    public User(String id, String email, long createdAt) {
        this.id = id;
        this.email = email;
        this.createdAt = createdAt;
    }
}

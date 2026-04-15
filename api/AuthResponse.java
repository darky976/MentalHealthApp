package com.example.mentalhealth.api;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("user")
    public User user;

    @SerializedName("session")
    public Session session;

    @SerializedName("access_token")
    public String access_token;

    @SerializedName("token_type")
    public String token_type;

    @SerializedName("expires_in")
    public long expires_in;

    @SerializedName("error")
    public String error;

    @SerializedName("error_description")
    public String error_description;

    public static class User {
        public String id;
        public String email;
        //  Изменено с String на Object (или используем Map/JsonObject)
        public Object user_metadata;
    }

    public static class Session {
        public String access_token;
        public String token_type;
        public long expires_in;
        public String refresh_token;
    }
}

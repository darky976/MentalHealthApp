package com.example.mentalhealth.api;

import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;
import com.example.mentalhealth.models.*;

public interface SupabaseApi {

    // --- АВТОРИЗАЦИЯ ---
    @POST("auth/v1/signup")
    Call<AuthResponse> signup(@Body AuthRequest request);

    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body AuthRequest request);

    // --- БЛОКНОТЫ (Notebooks) ---
    @GET("rest/v1/notebooks")
    Call<List<Notebook>> getNotebooks(@Header("Authorization") String token);

    @Headers("Prefer: return=representation")
    @POST("rest/v1/notebooks")
    Call<Notebook> createNotebook(@Header("Authorization") String token, @Body Notebook notebook);

    // --- ЗАМЕТКИ (Notes) ---
    @GET("rest/v1/notes")
    Call<List<Note>> getNotes(@Header("Authorization") String token, @Query("user_id") String userId);

    @Headers("Prefer: return=representation")
    @POST("rest/v1/notes")
    Call<Note> createNote(@Header("Authorization") String token, @Body Note note);

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/notes")
    Call<List<Note>> updateNote(@Header("Authorization") String token, @Query("id") String id, @Body Note note);

    @DELETE("rest/v1/notes")
    Call<Void> deleteNote(@Header("Authorization") String token, @Query("id") String id);

    // --- КАЛЕНДАРЬ И НАСТРОЕНИЕ (Mood) ---
    @GET("rest/v1/mood_entries")
    Call<List<MoodEntry>> getMoodEntries(
            @Header("Authorization") String token,
            @Query("user_id") String userId,
            @Query("order") String order
    );

    @GET("rest/v1/mood_entries")
    Call<List<MoodEntry>> getMoodEntryByDate(@Header("Authorization") String token, @Query("user_id") String userId, @Query("date") String date);

    @Headers("Prefer: return=representation")
    @POST("rest/v1/mood_entries")
    Call<List<MoodEntry>> createMoodEntry(@Header("Authorization") String token, @Body MoodEntry moodEntry);

    @Headers({
            "Prefer: resolution=merge-duplicates",
            "Prefer: return=representation"
    })
    @POST("rest/v1/mood_entries")
    Call<List<MoodEntry>> upsertMoodEntry(
            @Header("Authorization") String token,
            @Query("on_conflict") String onConflict,
            @Body MoodEntry moodEntry
    );

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/mood_entries")
    Call<List<MoodEntry>> updateMoodEntry(@Header("Authorization") String token, @Query("id") String id, @Body MoodEntry moodEntry);

    // --- ЧАТ (Sessions & Messages) ---
    @GET("rest/v1/chat_sessions")
    Call<List<ChatSession>> getChatSessions(@Header("Authorization") String token, @Query("user_id") String userId);

    @Headers("Prefer: return=representation")
    @POST("rest/v1/chat_sessions")
    Call<ChatSession> createChatSession(@Header("Authorization") String token, @Body ChatSession session);

    @GET("rest/v1/chat_messages")
    Call<List<ChatMessage>> getChatMessages(@Header("Authorization") String token, @Query("chat_session_id") String sessionId);

    @Headers("Prefer: return=representation")
    @POST("rest/v1/chat_messages")
    Call<ChatMessage> createChatMessage(@Header("Authorization") String token, @Body ChatMessage message);
}

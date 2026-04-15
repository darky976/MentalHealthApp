package com.example.mentalhealth.repository;

import androidx.lifecycle.MutableLiveData;
import com.example.mentalhealth.api.SupabaseApi;
import com.example.mentalhealth.api.SupabaseClient;
import com.example.mentalhealth.models.ChatMessage;
import com.example.mentalhealth.models.ChatSession;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class ChatRepository {
    private SupabaseApi api = SupabaseClient.getInstance();
    private String token;

    public ChatRepository(String token) {
        this.token = "Bearer " + token;
    }

    public MutableLiveData<List<ChatSession>> getChatSessions(String userId) {
        MutableLiveData<List<ChatSession>> result = new MutableLiveData<>();
        api.getChatSessions(token, "eq." + userId).enqueue(new Callback<List<ChatSession>>() {
            @Override
            public void onResponse(Call<List<ChatSession>> call, Response<List<ChatSession>> response) {
                if (response.isSuccessful()) result.setValue(response.body());
            }
            @Override
            public void onFailure(Call<List<ChatSession>> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    public MutableLiveData<ChatSession> createChatSession(ChatSession session) {
        MutableLiveData<ChatSession> result = new MutableLiveData<>();
        api.createChatSession(token, session).enqueue(new Callback<ChatSession>() {
            @Override
            public void onResponse(Call<ChatSession> call, Response<ChatSession> response) {
                if (response.isSuccessful()) result.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ChatSession> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    public MutableLiveData<List<ChatMessage>> getChatMessages(String sessionId) {
        MutableLiveData<List<ChatMessage>> result = new MutableLiveData<>();
        api.getChatMessages(token, "eq." + sessionId).enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful()) result.setValue(response.body());
            }
            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    public MutableLiveData<ChatMessage> createChatMessage(ChatMessage message) {
        MutableLiveData<ChatMessage> result = new MutableLiveData<>();
        api.createChatMessage(token, message).enqueue(new Callback<ChatMessage>() {
            @Override
            public void onResponse(Call<ChatMessage> call, Response<ChatMessage> response) {
                if (response.isSuccessful()) result.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ChatMessage> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }
}

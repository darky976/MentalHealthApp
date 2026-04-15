package com.example.mentalhealth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.mentalhealth.models.ChatMessage;
import com.example.mentalhealth.models.ChatSession;
import com.example.mentalhealth.repository.ChatRepository;
import java.util.List;

public class ChatViewModel extends ViewModel {
    private ChatRepository repository;
    private MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>();

    public ChatViewModel(String token) {
        this.repository = new ChatRepository(token);
    }

    public LiveData<List<ChatSession>> getChatSessions(String userId) {
        return repository.getChatSessions(userId);
    }

    public LiveData<ChatSession> createChatSession(ChatSession session) {
        return repository.createChatSession(session);
    }

    public LiveData<List<ChatMessage>> getChatMessages(String sessionId) {
        return repository.getChatMessages(sessionId);
    }

    public void sendMessage(ChatMessage message) {
        repository.createChatMessage(message);
    }
}

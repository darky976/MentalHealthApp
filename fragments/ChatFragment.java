package com.example.mentalhealth.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mentalhealth.R;
import com.example.mentalhealth.adapters.ChatAdapter;
import com.example.mentalhealth.models.ChatMessage;
import com.example.mentalhealth.models.ChatSession;
import com.example.mentalhealth.viewmodel.ChatViewModel;
import java.util.ArrayList;
import java.util.UUID;

public class ChatFragment extends Fragment {
    private ChatViewModel viewModel;
    private RecyclerView rvChat;
    private ChatAdapter adapter;
    private EditText editChatMessage;
    private ImageButton btnSendMessage;
    private String userId;
    private String currentSessionId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences preferences = requireActivity().getSharedPreferences("app_prefs", 0);
        String token = preferences.getString("access_token", null);
        userId = preferences.getString("user_id", null);

        viewModel = new ViewModelProvider(this, new androidx.lifecycle.ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new ChatViewModel(token);
            }
        }).get(ChatViewModel.class);

        rvChat = view.findViewById(R.id.rv_chat);
        editChatMessage = view.findViewById(R.id.edit_chat_message);
        btnSendMessage = view.findViewById(R.id.btn_send_message);

        rvChat.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatAdapter(new ArrayList<>());
        rvChat.setAdapter(adapter);

        initChat();

        btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void initChat() {
        // Загружаем последнюю сессию или создаем новую
        viewModel.getChatSessions(userId).observe(getViewLifecycleOwner(), sessions -> {
            if (sessions != null && !sessions.isEmpty()) {
                currentSessionId = sessions.get(0).id;
                loadMessages();
            } else {
                createNewSession();
            }
        });
    }

    private void createNewSession() {
        ChatSession session = new ChatSession();
        session.id = UUID.randomUUID().toString();
        session.userId = userId;
        viewModel.createChatSession(session).observe(getViewLifecycleOwner(), newSession -> {
            if (newSession != null) {
                currentSessionId = newSession.id;
                addAssistantMessage("Hello! I'm your AI mental health assistant. How can I help you today?");
            }
        });
    }

    private void loadMessages() {
        viewModel.getChatMessages(currentSessionId).observe(getViewLifecycleOwner(), messages -> {
            if (messages != null) {
                adapter.setMessages(new ArrayList<>(messages));
                rvChat.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
    }

    private void sendMessage() {
        String text = editChatMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        ChatMessage message = new ChatMessage();
        message.id = UUID.randomUUID().toString();
        message.chatSessionId = currentSessionId;
        message.userId = userId;
        message.role = "user";
        message.message = text;

        adapter.addMessage(message);
        rvChat.scrollToPosition(adapter.getItemCount() - 1);
        editChatMessage.setText("");

        viewModel.sendMessage(message);

        // Имитация ответа нейросети
        new Handler().postDelayed(() -> {
            String aiResponse = getAIResponse(text);
            addAssistantMessage(aiResponse);
        }, 1500);
    }

    private void addAssistantMessage(String text) {
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.id = UUID.randomUUID().toString();
        assistantMsg.chatSessionId = currentSessionId;
        assistantMsg.userId = userId;
        assistantMsg.role = "assistant";
        assistantMsg.message = text;

        adapter.addMessage(assistantMsg);
        rvChat.scrollToPosition(adapter.getItemCount() - 1);
        viewModel.sendMessage(assistantMsg);
    }

    private String getAIResponse(String userText) {
        userText = userText.toLowerCase();
        if (userText.contains("sad") || userText.contains("bad")) {
            return "I'm sorry to hear that. Remember that it's okay to feel this way. Would you like to write about it in your notes?";
        } else if (userText.contains("hello") || userText.contains("hi")) {
            return "Hello! How are you feeling today?";
        } else if (userText.contains("help")) {
            return "I can help you track your mood or just listen to your thoughts. What's on your mind?";
        } else {
            return "I understand. Tell me more about that. How does it make you feel?";
        }
    }
}

package com.example.mentalhealth.repository;

import androidx.lifecycle.MutableLiveData;
import com.example.mentalhealth.api.SupabaseApi;
import com.example.mentalhealth.api.SupabaseClient;
import com.example.mentalhealth.models.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class NotesRepository {
    private SupabaseApi api = SupabaseClient.getInstance();
    private String token;

    public NotesRepository(String token) {
        this.token = "Bearer " + token;
    }

    // --- НОВОЕ: Метод, на который ругалась ViewModel ---
    public MutableLiveData<List<Notebook>> getNotebooks() {
        MutableLiveData<List<Notebook>> result = new MutableLiveData<>();
        api.getNotebooks(token).enqueue(new Callback<List<Notebook>>() {
            @Override
            public void onResponse(Call<List<Notebook>> call, Response<List<Notebook>> response) {
                if (response.isSuccessful()) {
                    result.setValue(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<Notebook>> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    // Получить все заметки
    public MutableLiveData<List<Note>> getNotesByUser(String userId) {
        MutableLiveData<List<Note>> result = new MutableLiveData<>();
        api.getNotes(token, "eq." + userId).enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                if (response.isSuccessful()) result.setValue(response.body());
            }
            @Override public void onFailure(Call<List<Note>> call, Throwable t) { result.setValue(null); }
        });
        return result;
    }

    // Создать заметку
    public MutableLiveData<Note> createNote(Note note) {
        MutableLiveData<Note> result = new MutableLiveData<>();
        api.createNote(token, note).enqueue(new Callback<Note>() {
            @Override
            public void onResponse(Call<Note> call, Response<Note> response) {
                if (response.isSuccessful()) result.setValue(response.body());
            }
            @Override public void onFailure(Call<Note> call, Throwable t) { result.setValue(null); }
        });
        return result;
    }

    // УДАЛИТЬ заметку
    public MutableLiveData<Boolean> deleteNote(String noteId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        api.deleteNote(token, "eq." + noteId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                result.setValue(response.isSuccessful());
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { result.setValue(false); }
        });
        return result;
    }

    // ОТРЕДАКТИРОВАТЬ заметку
    public MutableLiveData<Boolean> updateNote(String noteId, Note note) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        api.updateNote(token, "eq." + noteId, note).enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                result.setValue(response.isSuccessful());
            }
            @Override public void onFailure(Call<List<Note>> call, Throwable t) { result.setValue(false); }
        });
        return result;
    }
}

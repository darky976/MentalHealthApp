package com.example.mentalhealth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.mentalhealth.api.RetrofitClient;
import com.example.mentalhealth.api.SupabaseApi;
import com.example.mentalhealth.models.Note;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import java.util.List;

public class NotesViewModel extends ViewModel {
    private final SupabaseApi api;

    public NotesViewModel(String token) {
        Retrofit r = RetrofitClient.getClient(token);
        api = r.create(SupabaseApi.class);
    }

    // Возвращает LiveData списка заметок. userId должен быть plain id — внутри формируем eq.
    public LiveData<List<Note>> getNotesByUser(String userId) {
        MutableLiveData<List<Note>> live = new MutableLiveData<>();
        api.getNotes(/*Authorization header not needed because interceptor adds it*/ null, "eq." + userId)
                .enqueue(new Callback<List<Note>>() {
                    @Override
                    public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                        if (response.isSuccessful()) live.postValue(response.body());
                        else live.postValue(null);
                    }
                    @Override public void onFailure(Call<List<Note>> call, Throwable t) { live.postValue(null); }
                });
        return live;
    }

    public LiveData<Note> createNote(Note note) {
        MutableLiveData<Note> live = new MutableLiveData<>();
        api.createNote(null, note).enqueue(new Callback<Note>() {
            @Override public void onResponse(Call<Note> call, Response<Note> response) {
                if (response.isSuccessful()) live.postValue(response.body()); else live.postValue(null);
            }
            @Override public void onFailure(Call<Note> call, Throwable t) { live.postValue(null); }
        });
        return live;
    }

    public LiveData<Boolean> updateNote(String id, Note note) {
        MutableLiveData<Boolean> live = new MutableLiveData<>(false);
        api.updateNote(null, "eq." + id, note).enqueue(new Callback<List<Note>>() {
            @Override public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                live.postValue(response.isSuccessful());
            }
            @Override public void onFailure(Call<List<Note>> call, Throwable t) { live.postValue(false); }
        });
        return live;
    }

    public LiveData<Boolean> deleteNote(String id) {
        MutableLiveData<Boolean> live = new MutableLiveData<>(false);
        api.deleteNote(null, "eq." + id).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) { live.postValue(response.isSuccessful()); }
            @Override public void onFailure(Call<Void> call, Throwable t) { live.postValue(false); }
        });
        return live;
    }
}

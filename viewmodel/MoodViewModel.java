package com.example.mentalhealth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.mentalhealth.api.RetrofitClient;
import com.example.mentalhealth.api.SupabaseApi;
import com.example.mentalhealth.models.MoodEntry;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;

public class MoodViewModel extends ViewModel {
    private final SupabaseApi api;
    private final MutableLiveData<String> lastError = new MutableLiveData<>();

    public MoodViewModel(String token) {
        Retrofit r = RetrofitClient.getClient(token);
        api = r.create(SupabaseApi.class);
    }

    public LiveData<List<MoodEntry>> getMoodEntries(String userId) {
        MutableLiveData<List<MoodEntry>> live = new MutableLiveData<>();
        api.getMoodEntries(null, "eq." + userId, "date.asc").enqueue(new Callback<List<MoodEntry>>() {
            @Override public void onResponse(Call<List<MoodEntry>> call, Response<List<MoodEntry>> response) {
                if (response.isSuccessful()) {
                    lastError.postValue(null);
                    live.postValue(response.body());
                } else {
                    lastError.postValue(httpErrorLabel(response, "getMoodEntries"));
                    live.postValue(null);
                }
            }
            @Override public void onFailure(Call<List<MoodEntry>> call, Throwable t) {
                lastError.postValue("getMoodEntries error: " + t.getMessage());
                live.postValue(null);
            }
        });
        return live;
    }

    // Получаем одну запись по дате — вернёт список; для удобства вернём первый элемент или null
    public LiveData<MoodEntry> getMoodByDate(String userId, String date) {
        MutableLiveData<MoodEntry> live = new MutableLiveData<>();
        api.getMoodEntryByDate(null, "eq." + userId, "eq." + date).enqueue(new Callback<List<MoodEntry>>() {
            @Override public void onResponse(Call<List<MoodEntry>> call, Response<List<MoodEntry>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    lastError.postValue(null);
                    live.postValue(response.body().get(0));
                } else if (response.isSuccessful()) {
                    live.postValue(null);
                } else {
                    lastError.postValue(httpErrorLabel(response, "getMoodByDate"));
                    live.postValue(null);
                }
            }
            @Override public void onFailure(Call<List<MoodEntry>> call, Throwable t) {
                lastError.postValue("getMoodByDate error: " + t.getMessage());
                live.postValue(null);
            }
        });
        return live;
    }

    public LiveData<MoodEntry> createMoodEntry(MoodEntry entry) {
        MutableLiveData<MoodEntry> live = new MutableLiveData<>();
        api.createMoodEntry(null, entry).enqueue(new Callback<List<MoodEntry>>() {
            @Override public void onResponse(Call<List<MoodEntry>> call, Response<List<MoodEntry>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    lastError.postValue(null);
                    live.postValue(response.body().get(0));
                } else {
                    lastError.postValue(httpErrorLabel(response, "createMoodEntry"));
                    live.postValue(null);
                }
            }
            @Override public void onFailure(Call<List<MoodEntry>> call, Throwable t) {
                lastError.postValue("createMoodEntry error: " + t.getMessage());
                live.postValue(null);
            }
        });
        return live;
    }

    public LiveData<Boolean> updateMoodEntry(String id, MoodEntry entry) {
        MutableLiveData<Boolean> live = new MutableLiveData<>(false);
        api.updateMoodEntry(null, "eq." + id, entry).enqueue(new Callback<List<MoodEntry>>() {
            @Override public void onResponse(Call<List<MoodEntry>> call, Response<List<MoodEntry>> response) {
                if (response.isSuccessful()) {
                    lastError.postValue(null);
                    live.postValue(true);
                } else {
                    lastError.postValue(httpErrorLabel(response, "updateMoodEntry"));
                    live.postValue(false);
                }
            }
            @Override public void onFailure(Call<List<MoodEntry>> call, Throwable t) {
                lastError.postValue("updateMoodEntry error: " + t.getMessage());
                live.postValue(false);
            }
        });
        return live;
    }

    public LiveData<MoodEntry> upsertMoodEntry(MoodEntry entry) {
        MutableLiveData<MoodEntry> live = new MutableLiveData<>();
        api.upsertMoodEntry(null, "user_id,date", entry).enqueue(new Callback<List<MoodEntry>>() {
            @Override public void onResponse(Call<List<MoodEntry>> call, Response<List<MoodEntry>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    lastError.postValue(null);
                    live.postValue(response.body().get(0));
                } else {
                    lastError.postValue(httpErrorLabel(response, "upsertMoodEntry"));
                    live.postValue(null);
                }
            }

            @Override public void onFailure(Call<List<MoodEntry>> call, Throwable t) {
                lastError.postValue("upsertMoodEntry error: " + t.getMessage());
                live.postValue(null);
            }
        });
        return live;
    }

    public LiveData<String> getLastError() {
        return lastError;
    }

    private static String httpErrorLabel(Response<?> response, String prefix) {
        String body = "";
        try {
            ResponseBody eb = response.errorBody();
            if (eb != null) {
                body = eb.string();
                if (body.length() > 200) {
                    body = body.substring(0, 200) + "...";
                }
            }
        } catch (IOException ignored) {
        }
        return prefix + ": HTTP " + response.code() + (body.isEmpty() ? "" : " — " + body);
    }
}

package com.example.mentalhealth.repository;

import androidx.lifecycle.MutableLiveData;
import com.example.mentalhealth.api.SupabaseApi;
import com.example.mentalhealth.api.SupabaseClient;
import com.example.mentalhealth.models.MoodEntry;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class MoodRepository {
    private SupabaseApi api = SupabaseClient.getInstance();
    private String token;

    public MoodRepository(String token) {
        this.token = "Bearer " + token;
    }

    public MutableLiveData<List<MoodEntry>> getMoodEntries(String userId) {
        MutableLiveData<List<MoodEntry>> result = new MutableLiveData<>();
        api.getMoodEntries(token, "eq." + userId, "date.asc").enqueue(new Callback<List<MoodEntry>>() {
            @Override
            public void onResponse(Call<List<MoodEntry>> call, Response<List<MoodEntry>> response) {
                if (response.isSuccessful()) {
                    result.setValue(response.body());
                } else {
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<MoodEntry>> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    public MutableLiveData<MoodEntry> getMoodByDate(String userId, String date) {
        MutableLiveData<MoodEntry> result = new MutableLiveData<>();
        api.getMoodEntryByDate(token, "eq." + userId, "eq." + date).enqueue(new Callback<List<MoodEntry>>() {
            @Override
            public void onResponse(Call<List<MoodEntry>> call, Response<List<MoodEntry>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    result.setValue(response.body().get(0));
                } else {
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<MoodEntry>> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    public MutableLiveData<MoodEntry> createMoodEntry(MoodEntry entry) {
        MutableLiveData<MoodEntry> result = new MutableLiveData<>();
        api.createMoodEntry(token, entry).enqueue(new Callback<List<MoodEntry>>() {
            @Override
            public void onResponse(Call<List<MoodEntry>> call, Response<List<MoodEntry>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    result.setValue(response.body().get(0));
                } else {
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<MoodEntry>> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    public MutableLiveData<MoodEntry> updateMoodEntry(String entryId, MoodEntry entry) {
        MutableLiveData<MoodEntry> result = new MutableLiveData<>();

        // Мы говорим: "Supabase, обнови запись с этим ID"
        // Важно: в Callback пишем List, потому что база возвращает список
        api.updateMoodEntry(token, "eq." + entryId, entry).enqueue(new Callback<List<MoodEntry>>() {
            @Override
            public void onResponse(Call<List<MoodEntry>> call, Response<List<MoodEntry>> response) {
                // Если база ответила "ОК" и прислала нам список
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Мы берем из этого списка первую (единственную) запись
                    result.setValue(response.body().get(0));
                } else {
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<MoodEntry>> call, Throwable t) {
                // Если интернет отвалился
                result.setValue(null);
            }
        });
        return result;
    }
}

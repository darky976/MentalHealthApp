package com.example.mentalhealth.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.mentalhealth.BuildConfig;
import com.example.mentalhealth.MainActivity;
import com.example.mentalhealth.R;
import com.example.mentalhealth.api.SupabaseConfig;
import com.example.mentalhealth.models.MoodEntry;
import com.example.mentalhealth.viewmodel.MoodViewModel;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private static final String[] MOOD_NAMES = {"Angry", "Sad", "Neutral", "Happy", "Amazing"};
    private static final String[] MOOD_EMOJI = {"😠", "😢", "😐", "🙂", "🤩"};

    private MoodViewModel viewModel;
    private CalendarView calendarView;
    private Button btnSaveMood;
    private TextView tvSelectedMood;
    private TextView tvDateMoodLine;
    private TextView tvCommentPreview;
    private TextView tvCalendarDebug;
    private ImageButton btnOpenComment;
    private String selectedDate;
    private String userId;
    private MoodEntry currentEntry;
    private int selectedMoodValue = 3;
    private String draftComment = "";

    private ImageButton[] moodButtons = new ImageButton[5];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        userId = prefs.getString("user_id", null);
        String token = ((MainActivity) requireActivity()).getToken();
        if (!isValidUuid(userId)) {
            String tokenUserId = extractUserIdFromToken(token);
            if (isValidUuid(tokenUserId)) {
                userId = tokenUserId;
                prefs.edit().putString("user_id", userId).apply();
            }
        }

        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new MoodViewModel(token);
            }
        }).get(MoodViewModel.class);

        calendarView = view.findViewById(R.id.calendar_view);
        btnSaveMood = view.findViewById(R.id.btn_save_mood);
        tvSelectedMood = view.findViewById(R.id.tv_selected_mood);
        tvDateMoodLine = view.findViewById(R.id.tv_date_mood_line);
        tvCommentPreview = view.findViewById(R.id.tv_comment_preview);
        tvCalendarDebug = view.findViewById(R.id.tv_calendar_debug);
        btnOpenComment = view.findViewById(R.id.btn_open_comment);

        if (tvCalendarDebug != null) {
            tvCalendarDebug.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        }

        if (!SupabaseConfig.hasValidAnonKey()) {
            Toast.makeText(getContext(),
                    "В local.properties добавь supabase.anon.key= (JWT anon из Supabase Dashboard → API)",
                    Toast.LENGTH_LONG).show();
        }

        initMoodButtons(view);

        if (userId == null || userId.trim().isEmpty()) {
            tvSelectedMood.setText("Не найден user id. Перелогиньтесь.");
            btnSaveMood.setEnabled(false);
            updateDebugInfo("missing user_id");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = dateFormat.format(Calendar.getInstance().getTime());
        updateDateMoodLine();
        updateDebugInfo("init");

        loadMoodForDate(selectedDate);

        calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            selectedDate = dateFormat.format(cal.getTime());
            updateDateMoodLine();
            updateDebugInfo("date changed");
            loadMoodForDate(selectedDate);
        });

        btnOpenComment.setOnClickListener(v -> openCommentDialog());
        tvCommentPreview.setOnClickListener(v -> openCommentDialog());

        btnSaveMood.setOnClickListener(v -> saveMood());
    }

    private void initMoodButtons(View view) {
        int[] ids = {R.id.btn_mood_1, R.id.btn_mood_2, R.id.btn_mood_3, R.id.btn_mood_4, R.id.btn_mood_5};
        for (int i = 0; i < 5; i++) {
            final int value = i + 1;
            moodButtons[i] = view.findViewById(ids[i]);
            moodButtons[i].setOnClickListener(v -> selectMood(value));
        }
    }

    private void selectMood(int value) {
        selectedMoodValue = value;
        String name = MOOD_NAMES[value - 1];
        tvSelectedMood.setText("Selected: " + name);
        for (int i = 0; i < 5; i++) {
            moodButtons[i].setAlpha(i == (value - 1) ? 1.0f : 0.4f);
        }
        updateDateMoodLine();
    }

    private void updateDateMoodLine() {
        if (tvDateMoodLine == null || selectedDate == null) {
            return;
        }
        int idx = Math.max(1, Math.min(5, selectedMoodValue)) - 1;
        String emoji = MOOD_EMOJI[idx];
        String name = MOOD_NAMES[idx];
        tvDateMoodLine.setText(selectedDate + "  ·  " + emoji + "  " + name);
    }

    private void updateCommentPreview() {
        if (tvCommentPreview == null) {
            return;
        }
        if (draftComment == null || draftComment.trim().isEmpty()) {
            tvCommentPreview.setText("No note yet — tap the note icon");
            tvCommentPreview.setTextColor(ContextCompat.getColor(requireContext(), R.color.textPink));
        } else {
            tvCommentPreview.setText(draftComment.trim());
            tvCommentPreview.setTextColor(ContextCompat.getColor(requireContext(), R.color.textWhite));
        }
    }

    private void openCommentDialog() {
        final EditText input = new EditText(requireContext());
        input.setMinLines(4);
        input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        input.setText(draftComment);
        input.setHint("Write here…");
        input.setTextColor(getResources().getColor(R.color.textWhite));
        input.setHintTextColor(getResources().getColor(R.color.textPink));
        input.setBackgroundResource(R.drawable.edit_text_bg);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(requireContext())
                .setTitle("Note for this day")
                .setView(input)
                .setPositiveButton("Save note", (d, w) -> {
                    draftComment = input.getText().toString();
                    updateCommentPreview();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadMoodForDate(String date) {
        updateDebugInfo("loadMoodForDate");
        viewModel.getMoodByDate(userId, date).observe(getViewLifecycleOwner(), entry -> {
            currentEntry = entry;
            if (entry != null) {
                selectMood(entry.mood);
                draftComment = entry.comment != null ? entry.comment : "";
                btnSaveMood.setText("Update for " + date);
            } else {
                selectMood(3);
                draftComment = "";
                btnSaveMood.setText("Save for " + date);
            }
            updateCommentPreview();
            updateDateMoodLine();
            updateDebugInfo("load complete");
        });
    }

    private void saveMood() {
        String comment = draftComment != null ? draftComment.trim() : "";
        updateDebugInfo("save clicked");
        if (currentEntry == null) {
            MoodEntry entry = new MoodEntry();
            entry.userId = userId;
            entry.date = selectedDate;
            entry.mood = selectedMoodValue;
            entry.comment = comment.isEmpty() ? null : comment;
            viewModel.createMoodEntry(entry).observe(getViewLifecycleOwner(), createdEntry -> {
                if (createdEntry != null) {
                    currentEntry = createdEntry;
                    Toast.makeText(getContext(), "Mood saved!", Toast.LENGTH_SHORT).show();
                    loadMoodForDate(selectedDate);
                    updateDebugInfo("create success");
                } else {
                    viewModel.getMoodByDate(userId, selectedDate).observe(getViewLifecycleOwner(), existing -> {
                        if (existing != null && existing.id != null) {
                            existing.mood = selectedMoodValue;
                            existing.comment = comment.isEmpty() ? null : comment;
                            viewModel.updateMoodEntry(existing.id, existing).observe(getViewLifecycleOwner(), success -> {
                                if (Boolean.TRUE.equals(success)) {
                                    currentEntry = existing;
                                    Toast.makeText(getContext(), "Mood updated!", Toast.LENGTH_SHORT).show();
                                    loadMoodForDate(selectedDate);
                                    updateDebugInfo("fallback update success");
                                } else {
                                    String error = viewModel.getLastError().getValue();
                                    Toast.makeText(getContext(), error != null ? error : "Save failed", Toast.LENGTH_LONG).show();
                                    updateDebugInfo("fallback update failed");
                                }
                            });
                        } else {
                            String error = viewModel.getLastError().getValue();
                            Toast.makeText(getContext(), error != null ? error : "Save failed", Toast.LENGTH_LONG).show();
                            updateDebugInfo("create failed, no existing row");
                        }
                    });
                }
            });
        } else {
            currentEntry.mood = selectedMoodValue;
            currentEntry.comment = comment.isEmpty() ? null : comment;
            viewModel.updateMoodEntry(currentEntry.id, currentEntry).observe(getViewLifecycleOwner(), success -> {
                if (Boolean.TRUE.equals(success)) {
                    Toast.makeText(getContext(), "Mood updated!", Toast.LENGTH_SHORT).show();
                    loadMoodForDate(selectedDate);
                    updateDebugInfo("update success");
                } else {
                    String error = viewModel.getLastError().getValue();
                    Toast.makeText(getContext(), error != null ? error : "Update failed", Toast.LENGTH_LONG).show();
                    updateDebugInfo("update failed");
                }
            });
        }
    }

    private void updateDebugInfo(String stage) {
        if (tvCalendarDebug == null || tvCalendarDebug.getVisibility() != View.VISIBLE) {
            return;
        }
        String error = viewModel != null && viewModel.getLastError().getValue() != null
                ? viewModel.getLastError().getValue()
                : "-";
        String comment = draftComment != null ? draftComment : "";
        String debug = "stage=" + stage
                + " | uid=" + (userId != null ? userId : "null")
                + " | date=" + (selectedDate != null ? selectedDate : "null")
                + " | mood=" + selectedMoodValue
                + " | comment_len=" + comment.length()
                + " | err=" + error;
        tvCalendarDebug.setText(debug);
    }

    private boolean isValidUuid(String value) {
        return value != null && value.matches("^[0-9a-fA-F-]{36}$");
    }

    private String extractUserIdFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            byte[] decoded = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            JSONObject payload = new JSONObject(new String(decoded));
            return payload.optString("sub", null);
        } catch (Exception ignored) {
            return null;
        }
    }
}

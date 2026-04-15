package com.example.mentalhealth.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.mentalhealth.R;
import com.example.mentalhealth.models.MoodEntry;
import com.example.mentalhealth.viewmodel.MoodViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChartFragment extends Fragment {
    private MoodViewModel viewModel;
    private BarChart barChart;
    private TextView tvMoodDescription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences preferences = requireActivity().getSharedPreferences("app_prefs", 0);
        String token = preferences.getString("access_token", null);
        String userId = preferences.getString("user_id", null);

        viewModel = new ViewModelProvider(this, new androidx.lifecycle.ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new MoodViewModel(token);
            }
        }).get(MoodViewModel.class);

        barChart = view.findViewById(R.id.bar_chart);
        tvMoodDescription = view.findViewById(R.id.tv_mood_description);

        setupChart();

        if (userId == null || userId.trim().isEmpty()) {
            tvMoodDescription.setText("Не найден user id. Перелогиньтесь, чтобы увидеть статистику.");
            barChart.clear();
            return;
        }

        viewModel.getMoodEntries(userId).observe(getViewLifecycleOwner(), moodEntries -> {
            if (moodEntries != null && !moodEntries.isEmpty()) {
                displayChart(moodEntries);
                updateDescription(moodEntries);
            } else {
                tvMoodDescription.setText("No data yet. Start tracking your mood in the calendar!");
                barChart.clear();
            }
        });
    }

    private void setupChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getLegend().setTextColor(getResources().getColor(R.color.textWhite));
        
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(getResources().getColor(R.color.textWhite));
        xAxis.setDrawGridLines(false);

        barChart.getAxisLeft().setTextColor(getResources().getColor(R.color.textWhite));
        barChart.getAxisRight().setEnabled(false);
    }

    private void displayChart(List<MoodEntry> moodEntries) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        // Берем последние 7 записей
        int start = Math.max(0, moodEntries.size() - 7);
        for (int i = start; i < moodEntries.size(); i++) {
            MoodEntry entry = moodEntries.get(i);
            entries.add(new BarEntry(i - start, entry.mood));
            labels.add(formatDateLabel(entry.date));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Mood Level (1-5)");
        dataSet.setColor(getResources().getColor(R.color.accentTurquoise));
        dataSet.setValueTextColor(getResources().getColor(R.color.textWhite));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        barChart.setData(barData);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size(), false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private String formatDateLabel(String date) {
        if (date == null || date.length() < 10) return "-";
        // yyyy-MM-dd -> MM/dd
        return date.substring(5, 7) + "/" + date.substring(8, 10);
    }

    private void updateDescription(List<MoodEntry> moodEntries) {
        double average = 0;
        for (MoodEntry entry : moodEntries) {
            average += entry.mood;
        }
        average /= moodEntries.size();

        String status;
        if (average >= 4) {
            status = "You've been feeling great lately! Your emotional state is very positive.";
        } else if (average >= 3) {
            status = "Your mood is stable. Keep practicing mindfulness and self-care.";
        } else {
            status = "You've been feeling a bit down. Remember to take breaks and talk to someone if needed.";
        }

        tvMoodDescription.setText("Average mood: " + String.format(Locale.US, "%.1f", average) + "/5\n\n" + status);
    }
}

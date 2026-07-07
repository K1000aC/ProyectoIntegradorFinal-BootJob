package com.example.proyectointegradorfinalbootjob;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DashboardActivity extends AppCompatActivity {

    private LineChart lineChart;
    private RadarChart radarChart;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState, layoutContent;
    private TextView tvSimulations, tvAvgScore, tvBestScore, tvTotalTime, tvGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        MaterialButton btnNewSimulation = findViewById(R.id.btn_new_simulation);
        lineChart = findViewById(R.id.lineChart_progress);
        radarChart = findViewById(R.id.radarChart_skills);
        progressBar = findViewById(R.id.progress_bar_dashboard);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        layoutContent = findViewById(R.id.layout_dashboard_content);
        tvGreeting = findViewById(R.id.tv_greeting);

        tvSimulations = findViewById(R.id.tv_stat_simulations);
        tvAvgScore = findViewById(R.id.tv_stat_avg_score);
        tvBestScore = findViewById(R.id.tv_stat_best_score);
        tvTotalTime = findViewById(R.id.tv_stat_total_time);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", "");
        String token = prefs.getString("token", "");
        String nombre = prefs.getString("nombre", "Usuario");

        tvGreeting.setText("¡Hola, " + nombre + "! 👋");
        btnNewSimulation.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, InterviewSimulatorActivity.class)));

        if (!userId.isEmpty()) {
            loadUserStats(userId, token);
            fetchHabilidades(userId, token);
        } else {
            loadLocalStats();
        }
        setupBottomNavigation();
    }

    private void loadUserStats(String userId, String token) {
        progressBar.setVisibility(View.VISIBLE);
        SupabaseManager.getStatistics(userId, token, new Callback() {
            @Override public void onFailure(Call call, IOException e) { runOnUiThread(() -> { progressBar.setVisibility(View.GONE); loadLocalStats(); }); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONArray jsonArray = new JSONArray(body);
                        if (jsonArray.length() > 0) {
                            layoutContent.setVisibility(View.VISIBLE);
                            layoutEmptyState.setVisibility(View.GONE);
                            updateStats(jsonArray);
                            setupLineChart(jsonArray);
                        } else { loadLocalStats(); }
                    } catch (Exception e) { loadLocalStats(); }
                });
            }
        });
    }

    private void loadLocalStats() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int simCount = prefs.getInt("sim_count_local", 0);
        String simScoresStr = prefs.getString("sim_scores_local", "");

        if (simCount > 0 && !simScoresStr.isEmpty()) {
            layoutContent.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);

            String[] scores = simScoresStr.split(",");
            int totalScore = 0;
            int bestScore = 0;
            List<Entry> lineEntries = new ArrayList<>();

            for (int i = 0; i < scores.length; i++) {
                int score = Integer.parseInt(scores[i]);
                totalScore += score;
                if (score > bestScore) bestScore = score;
                lineEntries.add(new Entry(i, (float) score));
            }

            tvSimulations.setText(String.valueOf(simCount));
            tvAvgScore.setText(String.valueOf(totalScore / simCount));
            tvBestScore.setText(String.valueOf(bestScore));
            tvTotalTime.setText((simCount * 5) + "m");

            LineDataSet lineDataSet = new LineDataSet(lineEntries, "Score");
            lineDataSet.setColor(Color.parseColor("#16A34A"));
            lineDataSet.setCircleColor(Color.parseColor("#16A34A"));
            lineDataSet.setLineWidth(3f);
            lineChart.setData(new LineData(lineDataSet));
            lineChart.invalidate();
        } else {
            showEmptyState();
        }
    }

    private void fetchHabilidades(String userId, String token) {
        SupabaseManager.getHabilidades(userId, token, new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                runOnUiThread(() -> {
                    try {
                        JSONArray arr = new JSONArray(body);
                        if (arr.length() > 0) {
                            JSONObject obj = arr.getJSONObject(0);
                            updateRadarChart(obj.optInt("algoritmos"), obj.optInt("bd"), obj.optInt("arquitectura"),
                                    obj.optInt("soft_skills"), obj.optInt("poo"), 0);
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                });
            }
        });
    }

    private void updateRadarChart(int alg, int bd, int arq, int soft, int poo, int red) {
        List<RadarEntry> entries = new ArrayList<>();
        entries.add(new RadarEntry(alg)); entries.add(new RadarEntry(bd));
        entries.add(new RadarEntry(arq)); entries.add(new RadarEntry(soft));
        entries.add(new RadarEntry(poo)); entries.add(new RadarEntry(red));

        RadarDataSet dataSet = new RadarDataSet(entries, "Habilidades");
        dataSet.setColor(Color.parseColor("#16A34A"));
        dataSet.setFillColor(Color.parseColor("#16A34A"));
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(100);

        radarChart.setData(new RadarData(dataSet));
        radarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"Alg", "BD", "Arq", "Soft", "POO", "Red"}));
        radarChart.invalidate();
    }

    private void showEmptyState() { layoutContent.setVisibility(View.GONE); layoutEmptyState.setVisibility(View.VISIBLE); }

    private void updateStats(JSONArray data) throws Exception {
        int count = data.length();
        int totalScore = 0;
        int bestScore = 0;
        for (int i = 0; i < count; i++) {
            int score = data.getJSONObject(i).optInt("score", 0);
            totalScore += score;
            if (score > bestScore) bestScore = score;
        }
        tvSimulations.setText(String.valueOf(count));
        tvAvgScore.setText(String.valueOf(count > 0 ? totalScore / count : 0));
        tvBestScore.setText(String.valueOf(bestScore));
        tvTotalTime.setText((count * 5) + "m");
    }

    private void setupLineChart(JSONArray data) throws Exception {
        List<Entry> lineEntries = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            float score = (float) data.getJSONObject(i).optDouble("score", 0);
            lineEntries.add(new Entry(i, score));
        }
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Score");
        lineDataSet.setColor(Color.parseColor("#16A34A"));
        lineDataSet.setCircleColor(Color.parseColor("#16A34A"));
        lineDataSet.setLineWidth(3f);
        lineChart.setData(new LineData(lineDataSet));
        lineChart.invalidate();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bnv = findViewById(R.id.bottom_navigation);
        bnv.setSelectedItemId(R.id.nav_dashboard);
        bnv.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_simulator) startActivity(new Intent(this, InterviewSimulatorActivity.class));
            else if (itemId == R.id.nav_cv) startActivity(new Intent(this, CvReviewerActivity.class));
            else if (itemId == R.id.nav_forum) startActivity(new Intent(this, ForumActivity.class));
            else if (itemId == R.id.nav_profile) startActivity(new Intent(this, UserProfileActivity.class));
            overridePendingTransition(0, 0);
            return true;
        });
    }
}
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
        String nombre = prefs.getString("nombre", "Usuario");

        tvGreeting.setText("¡Hola, " + nombre + "! 👋");

        btnNewSimulation.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, InterviewSimulatorActivity.class)));

        if (!userId.isEmpty()) {
            loadUserStats(userId);
        } else {
            showEmptyState();
        }

        setupBottomNavigation();
    }

    private void loadUserStats(String userId) {
        progressBar.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");

        SupabaseManager.getStatistics(userId, token, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(DashboardActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONArray jsonArray = new JSONArray(body);
                        if (jsonArray.length() > 0) {
                            layoutContent.setVisibility(View.VISIBLE);
                            updateStats(jsonArray);
                            setupLineChart(jsonArray);
                            setupRadarChart(jsonArray);
                        } else {
                            showEmptyState();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showEmptyState();
                    }
                });
            }
        });
    }

    private void showEmptyState() {
        layoutContent.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }

    private void updateStats(JSONArray data) throws Exception {
        int count = data.length();
        int totalScore = 0;
        int bestScore = 0;
        // Asumiendo que cada simulación dura aprox 5 min para el ejemplo, o podrías guardarlo en DB
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
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        lineChart.setData(new LineData(lineDataSet));
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.invalidate();
    }

    private void setupRadarChart(JSONArray data) {
        List<RadarEntry> radarEntries = new ArrayList<>();
        // En una app real, estos valores vendrían de un análisis de las respuestas del usuario
        radarEntries.add(new RadarEntry(80f)); // Algoritmos
        radarEntries.add(new RadarEntry(75f)); // Bases de Datos
        radarEntries.add(new RadarEntry(85f)); // Sistemas
        radarEntries.add(new RadarEntry(70f)); // Soft Skills
        radarEntries.add(new RadarEntry(65f)); // POO
        radarEntries.add(new RadarEntry(78f)); // Redes

        RadarDataSet radarDataSet = new RadarDataSet(radarEntries, "Habilidades");
        radarDataSet.setColor(Color.parseColor("#16A34A"));
        radarDataSet.setFillColor(Color.parseColor("#16A34A"));
        radarDataSet.setDrawFilled(true);
        radarDataSet.setFillAlpha(100);
        radarDataSet.setLineWidth(2f);
        radarDataSet.setDrawHighlightCircleEnabled(true);
        radarDataSet.setDrawHighlightIndicators(false);

        RadarData radarData = new RadarData(radarDataSet);
        radarData.setValueTextSize(8f);
        radarData.setDrawValues(false);
        
        radarChart.setData(radarData);
        radarChart.getDescription().setEnabled(false);
        radarChart.setWebLineWidth(1f);
        radarChart.setWebColor(Color.LTGRAY);
        radarChart.setWebLineWidthInner(1f);
        radarChart.setWebColorInner(Color.LTGRAY);
        radarChart.setWebAlpha(100);

        XAxis xAxis = radarChart.getXAxis();
        xAxis.setTextSize(9f);
        xAxis.setYOffset(0f);
        xAxis.setXOffset(0f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{
                "Algoritmos", "Bases de Datos", "Sistemas", "Soft Skills", "POO", "Redes"
        }));
        xAxis.setTextColor(Color.parseColor("#374151"));

        YAxis yAxis = radarChart.getYAxis();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);
        yAxis.setLabelCount(5, false);
        yAxis.setDrawLabels(false);

        radarChart.invalidate();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_simulator) startActivity(new Intent(this, InterviewSimulatorActivity.class));
            else if (itemId == R.id.nav_cv) startActivity(new Intent(this, CvReviewerActivity.class));
            else if (itemId == R.id.nav_forum) startActivity(new Intent(this, ForumActivity.class));
            else if (itemId == R.id.nav_profile) startActivity(new Intent(this, UserProfileActivity.class));
            else if (itemId == R.id.nav_dashboard) return true;
            overridePendingTransition(0, 0);
            return true;
        });
    }
}
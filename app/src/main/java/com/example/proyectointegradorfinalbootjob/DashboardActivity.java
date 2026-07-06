package com.example.proyectointegradorfinalbootjob;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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
    private List<Float> scoresList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        MaterialButton btnNewSimulation = findViewById(R.id.btn_new_simulation);
        lineChart = findViewById(R.id.lineChart_progress);
        radarChart = findViewById(R.id.radarChart_skills);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", "");
        String nombre = prefs.getString("nombre", "Usuario");

        // Saludo personalizado
        customizeGreeting((android.view.ViewGroup) findViewById(android.R.id.content), nombre);

        btnNewSimulation.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, InterviewSimulatorActivity.class)));

        // Cargar datos reales
        if (!userId.isEmpty()) {
            loadUserStats(userId);
        } else {
            // Si no hay usuario, cargar datos por defecto
            setupLineChart(null);
            setupRadarChart(null);
        }

        setupBottomNavigation();
    }

    private void loadUserStats(String userId) {
        SupabaseManager.getStatistics(userId, SupabaseManager.SUPABASE_KEY, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this, "Error de red", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            JSONArray jsonArray = new JSONArray(body);
                            setupLineChart(jsonArray);
                            setupRadarChart(jsonArray);
                        } catch (Exception e) { e.printStackTrace(); }
                    });
                }
            }
        });
    }

    private void setupLineChart(JSONArray data) {
        List<Entry> lineEntries = new ArrayList<>();
        // Lógica: Si hay datos de Supabase, usarlos, si no, usar valores hardcodeados para que no se vea vacío
        if (data != null && data.length() > 0) {
            // Ejemplo de parseo: lineEntries.add(new Entry(i, (float) data.getJSONObject(i).getDouble("score")));
        } else {
            lineEntries.add(new Entry(0, 48f)); lineEntries.add(new Entry(1, 55f));
            lineEntries.add(new Entry(2, 62f)); lineEntries.add(new Entry(3, 58f));
        }

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Score");
        lineDataSet.setColor(Color.parseColor("#16A34A"));
        lineDataSet.setLineWidth(3f);
        lineDataSet.setDrawCircleHole(false);

        lineChart.setData(new LineData(lineDataSet));
        lineChart.invalidate();
    }

    private void setupRadarChart(JSONArray data) {
        List<RadarEntry> radarEntries = new ArrayList<>();
        radarEntries.add(new RadarEntry(72f)); // Valores placeholder

        RadarDataSet radarDataSet = new RadarDataSet(radarEntries, "Habilidades");
        radarDataSet.setColor(Color.parseColor("#16A34A"));
        radarDataSet.setDrawFilled(true);

        radarChart.setData(new RadarData(radarDataSet));
        radarChart.invalidate();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_simulator) startActivity(new Intent(this, InterviewSimulatorActivity.class));
            else if (itemId == R.id.nav_profile) startActivity(new Intent(this, UserProfileActivity.class));
            overridePendingTransition(0, 0);
            return true;
        });
    }

    private void customizeGreeting(android.view.ViewGroup viewGroup, String name) {
        if (viewGroup == null) return;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            android.view.View child = viewGroup.getChildAt(i);
            if (child instanceof android.widget.TextView) {
                android.widget.TextView tv = (android.widget.TextView) child;
                if (tv.getText().toString().contains("Hola") || tv.getText().toString().contains("Usuario")) {
                    tv.setText("¡Hola, " + name + "! 👋");
                    return;
                }
            } else if (child instanceof android.view.ViewGroup) {
                customizeGreeting((android.view.ViewGroup) child, name);
            }
        }
    }
}
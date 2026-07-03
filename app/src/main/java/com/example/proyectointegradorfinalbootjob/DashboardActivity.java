package com.example.proyectointegradorfinalbootjob;

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

import android.content.Intent;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private LineChart lineChart;
    private RadarChart radarChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Enlazar componentes de UI
        MaterialButton btnNewSimulation = findViewById(R.id.btn_new_simulation);
        lineChart = findViewById(R.id.lineChart_progress);
        radarChart = findViewById(R.id.radarChart_skills);

        // Evento del botón de nueva simulación
        btnNewSimulation.setOnClickListener(v -> {
            Toast.makeText(this, "Redirigiendo al Simulador...", Toast.LENGTH_SHORT).show();
        });

        // Inicializar y rellenar las gráficas con los datos de Figma
        setupLineChart();
        setupRadarChart();

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                return true;
            } else if (itemId == R.id.nav_simulator) {
                startActivity(new Intent(getApplicationContext(), InterviewSimulatorActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_cv) {
                startActivity(new Intent(getApplicationContext(), CvReviewerActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_forum) {
                startActivity(new Intent(getApplicationContext(), ForumActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), UserProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void setupLineChart() {
        // 1. Clonar el 'progressData' de tu TSX
        List<Entry> lineEntries = new ArrayList<>();
        lineEntries.add(new Entry(0, 48f)); // S1
        lineEntries.add(new Entry(1, 55f)); // S2
        lineEntries.add(new Entry(2, 62f)); // S3
        lineEntries.add(new Entry(3, 58f)); // S4
        lineEntries.add(new Entry(4, 71f)); // S5
        lineEntries.add(new Entry(5, 79f)); // S6
        lineEntries.add(new Entry(6, 84f)); // S7

        // 2. Configurar el set de datos (Estilo idéntico a Tailwind)
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Score");
        lineDataSet.setColor(Color.parseColor("#16A34A")); // stroke="#16a34a"
        lineDataSet.setCircleColor(Color.parseColor("#16A34A"));
        lineDataSet.setLineWidth(3f); // strokeWidth={3}
        lineDataSet.setCircleRadius(4f); // dot={{ r: 4 }}
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setValueTextColor(Color.parseColor("#4B7A5D"));

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        // 3. Estilizar los Ejes (XAxis e YAxis)
        String[] sessions = new String[]{"S1", "S2", "S3", "S4", "S5", "S6", "S7"};
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(sessions));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.parseColor("#F0FDF4")); // stroke="#f0fdf4"
        xAxis.setTextColor(Color.parseColor("#4B7A5D"));

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f); // domain={[0, 100]}
        leftAxis.setGridColor(Color.parseColor("#F0FDF4"));
        leftAxis.setTextColor(Color.parseColor("#4B7A5D"));

        lineChart.getAxisRight().setEnabled(false); // Ocultar eje derecho innecesario
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.animateX(1000); // Animación suave al cargar
        lineChart.invalidate(); // Refrescar
    }

    private void setupRadarChart() {
        // 1. Clonar el 'radarData' de tu TSX
        List<RadarEntry> radarEntries = new ArrayList<>();
        radarEntries.add(new RadarEntry(72f)); // Algoritmos
        radarEntries.add(new RadarEntry(85f)); // Bases de Datos
        radarEntries.add(new RadarEntry(60f)); // Sistemas
        radarEntries.add(new RadarEntry(88f)); // Soft Skills
        radarEntries.add(new RadarEntry(78f)); // POO
        radarEntries.add(new RadarEntry(55f)); // Redes

        // 2. Configurar el set de datos (Simulando fillOpacity={0.2})
        RadarDataSet radarDataSet = new RadarDataSet(radarEntries, "Habilidades");
        radarDataSet.setColor(Color.parseColor("#16A34A"));
        radarDataSet.setLineWidth(2f);
        radarDataSet.setFillColor(Color.parseColor("#16A34A"));
        radarDataSet.setDrawFilled(true);
        radarDataSet.setFillAlpha(51); // 51 sobre 255 equivale al 20% de opacidad (0.2)

        RadarData radarData = new RadarData(radarDataSet);
        radarData.setDrawValues(false); // No saturar la gráfica con números flotantes
        radarChart.setData(radarData);

        // 3. Configurar etiquetas periféricas del Radar
        String[] skills = new String[]{"Algoritmos", "Bases de Datos", "Sistemas", "Soft Skills", "POO", "Redes"};
        XAxis xAxis = radarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(skills));
        xAxis.setTextSize(9f);
        xAxis.setTextColor(Color.parseColor("#4B7A5D"));

        YAxis yAxis = radarChart.getYAxis();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);
        yAxis.setDrawLabels(false); // Ocultar los números del eje central para estética limpia

        radarChart.setWebColor(Color.parseColor("#DCFCE7")); // Redes internas stroke="#dcfce7"
        radarChart.setWebColorInner(Color.parseColor("#DCFCE7"));
        radarChart.getDescription().setEnabled(false);
        radarChart.getLegend().setEnabled(false);
        radarChart.animateXY(1200, 1200);
        radarChart.invalidate();
    }
}
package com.example.proyectointegradorfinalbootjob;

import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class CvReviewerActivity extends AppCompatActivity {

    // Contenedores de las 3 fases
    private LinearLayout layoutUpload;
    private LinearLayout layoutAnalyzing;
    private LinearLayout layoutResults;

    // Elementos de la fase de análisis
    private ProgressBar progressAnalysis;
    private TextView tvProgressText;

    private int currentProgress = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cv_reviewer);

        // Enlazar vistas
        layoutUpload = findViewById(R.id.layout_phase_upload);
        layoutAnalyzing = findViewById(R.id.layout_phase_analyzing);
        layoutResults = findViewById(R.id.layout_phase_results);

        progressAnalysis = findViewById(R.id.progress_analysis);
        tvProgressText = findViewById(R.id.tv_progress_text);

        MaterialCardView btnUpload = findViewById(R.id.btn_upload_cv);
        Button btnReset = findViewById(R.id.btn_reset_analysis);

        // Configurar clics
        btnUpload.setOnClickListener(v -> startAnalysisSimulation());
        btnReset.setOnClickListener(v -> resetToUploadPhase());

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_cv);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_simulator) {
                startActivity(new Intent(getApplicationContext(), InterviewSimulatorActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_cv) {
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

    private void startAnalysisSimulation() {
        // Cambiar a fase "Analyzing"
        layoutUpload.setVisibility(View.GONE);
        layoutAnalyzing.setVisibility(View.VISIBLE);
        layoutResults.setVisibility(View.GONE);

        currentProgress = 0;
        progressAnalysis.setProgress(0);

        // Simular el proceso (Equivalente al setInterval de React)
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                currentProgress += (Math.random() * 15);

                if (currentProgress >= 100) {
                    currentProgress = 100;
                    progressAnalysis.setProgress(currentProgress);

                    // Transición a la fase de Resultados con un ligero delay
                    handler.postDelayed(() -> showResultsPhase(), 400);
                } else {
                    progressAnalysis.setProgress(currentProgress);
                    updateLoadingText(currentProgress);
                    handler.postDelayed(this, 200); // Se repite cada 200ms
                }
            }
        };
        handler.post(runnable);
    }

    private void updateLoadingText(int progress) {
        if (progress < 30) {
            tvProgressText.setText("Extrayendo contenido...");
        } else if (progress < 60) {
            tvProgressText.setText("Verificando compatibilidad ATS...");
        } else {
            tvProgressText.setText("Generando recomendaciones...");
        }
    }

    private void showResultsPhase() {
        layoutAnalyzing.setVisibility(View.GONE);
        layoutResults.setVisibility(View.VISIBLE);
    }

    private void resetToUploadPhase() {
        layoutResults.setVisibility(View.GONE);
        layoutUpload.setVisibility(View.VISIBLE);
        currentProgress = 0;
    }
}

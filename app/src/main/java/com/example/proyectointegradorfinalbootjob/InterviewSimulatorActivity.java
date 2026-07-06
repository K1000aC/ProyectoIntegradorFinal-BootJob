package com.example.proyectointegradorfinalbootjob;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class InterviewSimulatorActivity extends AppCompatActivity {

    private View phaseSetup, phaseInterview;
    private AutoCompleteTextView spinnerCompany, spinnerRole;
    private MaterialButton btnStart;
    private TextView tvTimer, tvCounter, tvAiQuestion, tvAiCategory;
    private EditText etUserAnswer;
    private MaterialButton btnSend, btnNext;
    private MaterialCardView cardFeedback;
    private TextView tvFeedbackTitle, tvFeedbackMessage;
    private LinearLayout layoutPhaseInterview;
    private android.widget.RadioGroup rgResponseMode;

    private int currentQuestionIndex = 0;
    private int timerSeconds = 0;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private int totalScore = 0;

    private String[] questions = {
            "Cuéntame un poco sobre ti y por qué estás interesado en este puesto.",
            "¿Cuál ha sido el proyecto técnico más desafiante que has desarrollado?",
            "Describe cómo diseñarías un sistema de gestión de inventarios."
    };
    private String[] categories = {"Introducción", "Experiencia técnica", "Diseño de sistemas"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview_simulator);

        phaseSetup = findViewById(R.id.layout_phase_setup);
        phaseInterview = findViewById(R.id.layout_phase_interview);
        layoutPhaseInterview = findViewById(R.id.layout_phase_interview);

        spinnerCompany = findViewById(R.id.spinner_company);
        spinnerRole = findViewById(R.id.spinner_role);
        btnStart = findViewById(R.id.btn_start_simulation);
        rgResponseMode = findViewById(R.id.rg_response_mode);

        tvTimer = findViewById(R.id.tv_timer);
        tvCounter = findViewById(R.id.tv_question_counter);
        tvAiCategory = findViewById(R.id.tv_question_category);
        tvAiQuestion = findViewById(R.id.tv_ai_question);
        etUserAnswer = findViewById(R.id.et_user_answer);
        btnSend = findViewById(R.id.btn_send_answer);
        btnNext = findViewById(R.id.btn_next_question);

        cardFeedback = findViewById(R.id.card_feedback);
        tvFeedbackTitle = findViewById(R.id.tv_feedback_title);
        tvFeedbackMessage = findViewById(R.id.tv_feedback_message);

        setupSpinners();

        btnStart.setOnClickListener(v -> startInterview());
        btnSend.setOnClickListener(v -> simulateFeedback());
        btnNext.setOnClickListener(v -> loadNextQuestion());

        setupTimerLogic();
        setupBottomNavigation();
    }

    private void setupSpinners() {
        String[] companies = {"Google", "Microsoft", "CEMEX", "Bimbo"};
        String[] roles = {"Software Engineer", "Desarrollador Backend", "Data Engineer"};

        spinnerCompany.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, companies));
        spinnerRole.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles));
        spinnerRole.setOnItemClickListener((parent, view, position, id) -> btnStart.setEnabled(true));
    }

    private void loadCustomQuestions() {
        String company = spinnerCompany.getText().toString().trim();
        String role = spinnerRole.getText().toString().trim();
        if (company.isEmpty()) company = "Google";
        if (role.isEmpty()) role = "Software Engineer";

        if (role.contains("Backend")) {
            questions = new String[]{"¿Cómo manejarías la consistencia de datos eventual en una arquitectura de microservicios?", "¿Qué consideraciones de escalabilidad e indexación tomarías al diseñar una base de datos PostgreSQL de gran volumen?", "Explica el funcionamiento de un token JWT y cómo securizarías tus endpoints REST de forma eficiente."};
            categories = new String[]{"Arquitectura", "Bases de Datos", "Seguridad"};
        } else if (role.contains("Data")) {
            questions = new String[]{"¿Cuál es la diferencia técnica entre procesos ETL y ELT y en qué escenarios recomendarías usar cada uno?", "¿Cómo optimizarías una consulta SQL lenta que involucra múltiples JOINs en tablas de millones de filas?", "Describe tu experiencia diseñando pipelines de datos en batch (lotes) comparado con procesamiento en tiempo real."};
            categories = new String[]{"Procesamiento de datos", "Optimización SQL", "Pipelines"};
        } else {
            questions = new String[]{"Cuéntame sobre una ocasión en la que tuviste que resolver un problema de rendimiento complejo en producción.", "¿Cómo diseñarías un sistema acortador de URLs tipo Bit.ly que soporte alta concurrencia?", "Explica las ventajas de la Programación Orientada a Objetos frente a la programación funcional en un gran proyecto."};
            categories = new String[]{"Resolución de problemas", "Diseño de Sistemas", "Paradigma de código"};
        }
    }

    private void startInterview() {
        loadCustomQuestions();
        phaseSetup.setVisibility(View.GONE);
        phaseInterview.setVisibility(View.VISIBLE);
        currentQuestionIndex = 0;
        totalScore = 0;
        loadQuestionData();
        startTimer();
    }

    private void loadQuestionData() {
        layoutPhaseInterview.setBackgroundColor(android.graphics.Color.parseColor("#FAFAFA"));
        tvAiCategory.setText("ENTREVISTADOR · " + categories[currentQuestionIndex].toUpperCase());
        tvAiQuestion.setText(questions[currentQuestionIndex]);
        tvCounter.setText((currentQuestionIndex + 1) + "/" + questions.length);
        etUserAnswer.setText("");
        cardFeedback.setVisibility(View.GONE);
        btnSend.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);
    }

    private void simulateFeedback() {
        String answer = etUserAnswer.getText().toString().trim();
        if(answer.isEmpty()) { Toast.makeText(this, "Escribe una respuesta", Toast.LENGTH_SHORT).show(); return; }
        stopTimer();
        btnSend.setVisibility(View.GONE);

        int score = (answer.length() < 15) ? 45 : 75 + (int)(Math.random() * 20);
        totalScore += score;

        // Semáforo de fondo
        if (score >= 85) {
            layoutPhaseInterview.setBackgroundColor(android.graphics.Color.parseColor("#DCFCE7")); // Verde
        } else if (score >= 60) {
            layoutPhaseInterview.setBackgroundColor(android.graphics.Color.parseColor("#FEF9C3")); // Amarillo
        } else {
            layoutPhaseInterview.setBackgroundColor(android.graphics.Color.parseColor("#FEE2E2")); // Rojo
        }

        tvFeedbackTitle.setText("Puntuación: " + score + "/100");
        tvFeedbackMessage.setText(score >= 70 ? "Excelente respuesta técnica." : "Podrías profundizar más en los detalles.");
        cardFeedback.setStrokeColor(score >= 70 ? android.graphics.Color.parseColor("#16A34A") : android.graphics.Color.parseColor("#EF4444"));
        cardFeedback.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
    }

    private void loadNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.length) {
            loadQuestionData();
            startTimer();
        } else {
            int averageScore = totalScore / questions.length;
            guardarResultadoEnBaseDeDatos(averageScore);
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("¡Simulación Finalizada!")
                    .setMessage("Puntuación Promedio: " + averageScore + "/100")
                    .setPositiveButton("Regresar al Dashboard", (dialog, which) -> finish())
                    .show();
        }
    }

    private void guardarResultadoEnBaseDeDatos(int score) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", "");
        String token = prefs.getString("token", SupabaseManager.SUPABASE_KEY);
        String empresa = spinnerCompany.getText().toString().trim();
        String puesto = spinnerRole.getText().toString().trim();

        if (!userId.isEmpty()) {
            SupabaseManager.saveSimulation(userId, empresa, puesto, score, token, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(InterviewSimulatorActivity.this, "Error al guardar", Toast.LENGTH_SHORT).show());
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    runOnUiThread(() -> Toast.makeText(InterviewSimulatorActivity.this, "Resultado guardado", Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    private void setupTimerLogic() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                timerSeconds++;
                tvTimer.setText(String.format("%02d:%02d", timerSeconds / 60, timerSeconds % 60));
                timerHandler.postDelayed(this, 1000);
            }
        };
    }

    private void startTimer() { timerSeconds = 0; timerHandler.post(timerRunnable); }
    private void stopTimer() { timerHandler.removeCallbacks(timerRunnable); }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_simulator);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(this, DashboardActivity.class));
            } else if (itemId == R.id.nav_cv) {
                startActivity(new Intent(this, CvReviewerActivity.class));
            } else if (itemId == R.id.nav_forum) {
                startActivity(new Intent(this, ForumActivity.class));
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, UserProfileActivity.class));
            } else if (itemId == R.id.nav_simulator) {
                return true;
            }
            overridePendingTransition(0, 0);
            return true;
        });
    }
}
package com.example.proyectointegradorfinalbootjob;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class InterviewSimulatorActivity extends AppCompatActivity {

    // Fases
    private View phaseSetup, phaseInterview;

    // Controles de Configuración
    private AutoCompleteTextView spinnerCompany, spinnerRole;
    private MaterialButton btnStart;

    // Controles de Entrevista
    private TextView tvTimer, tvCounter, tvAiQuestion, tvAiCategory;
    private EditText etUserAnswer;
    private MaterialButton btnSend, btnNext;
    private MaterialCardView cardFeedback;
    private TextView tvFeedbackTitle, tvFeedbackMessage;
    private LinearLayout layoutPhaseInterview;

    // Estado lógico
    private int currentQuestionIndex = 0;
    private int timerSeconds = 0;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    // Banco de Preguntas (Estático para simulación)
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

        // Enlazar vistas
        phaseSetup = findViewById(R.id.layout_phase_setup);
        phaseInterview = findViewById(R.id.layout_phase_interview);
        layoutPhaseInterview = findViewById(R.id.layout_phase_interview);

        spinnerCompany = findViewById(R.id.spinner_company);
        spinnerRole = findViewById(R.id.spinner_role);
        btnStart = findViewById(R.id.btn_start_simulation);

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

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_simulator);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_simulator) {
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

    private void setupSpinners() {
        String[] companies = {"Google", "Microsoft", "CEMEX", "Bimbo"};
        String[] roles = {"Software Engineer", "Desarrollador Backend", "Data Engineer"};

        spinnerCompany.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, companies));
        spinnerRole.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles));

        // Habilitar botón si hay rol (Simulación básica)
        spinnerRole.setOnItemClickListener((parent, view, position, id) -> btnStart.setEnabled(true));
    }

    private void startInterview() {
        phaseSetup.setVisibility(View.GONE);
        phaseInterview.setVisibility(View.VISIBLE);

        currentQuestionIndex = 0;
        loadQuestionData();
        startTimer();
    }

    private void loadQuestionData() {
        tvAiCategory.setText("ENTREVISTADOR · " + categories[currentQuestionIndex].toUpperCase());
        tvAiQuestion.setText(questions[currentQuestionIndex]);
        tvCounter.setText((currentQuestionIndex + 1) + "/" + questions.length);
        etUserAnswer.setText("");
        cardFeedback.setVisibility(View.GONE);
        btnSend.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);
    }

    private void simulateFeedback() {
        if(etUserAnswer.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Escribe una respuesta", Toast.LENGTH_SHORT).show();
            return;
        }

        stopTimer();
        btnSend.setVisibility(View.GONE);

        // Simulación: Cambiamos el color de fondo para emular el semáforo y mostramos el feedback
        layoutPhaseInterview.setBackgroundResource(R.color.interview_bg_yellow_end); // Semáforo amarillo
        tvFeedbackTitle.setText("Respuesta parcial - 65/100");
        tvFeedbackMessage.setText("Buen inicio, pero faltó profundidad. Incluye ejemplos concretos.");

        cardFeedback.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
    }

    private void loadNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.length) {
            layoutPhaseInterview.setBackgroundResource(R.color.interview_bg_green_end); // Restablecer fondo
            loadQuestionData();
            startTimer();
        } else {
            Toast.makeText(this, "¡Simulación Finalizada! Ir a resultados.", Toast.LENGTH_LONG).show();
            // Aquí agregarías el código para mostrar la fase 3 (Resultados)
            finish();
        }
    }

    private void setupTimerLogic() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                timerSeconds++;
                int min = timerSeconds / 60;
                int sec = timerSeconds % 60;
                tvTimer.setText(String.format("%02d:%02d", min, sec));
                timerHandler.postDelayed(this, 1000);
            }
        };
    }

    private void startTimer() {
        timerSeconds = 0;
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }
}
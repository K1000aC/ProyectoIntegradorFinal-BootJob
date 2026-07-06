package com.example.proyectointegradorfinalbootjob;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class InterviewSimulatorActivity extends AppCompatActivity {

    private View phaseSetup, phaseInterview;
    private AutoCompleteTextView spinnerCompany, spinnerRole;
    private MaterialButton btnStart, btnSend, btnNext;
    private ImageButton btnVoice, btnCamera, btnIdea;
    private TextView tvTimer, tvCounter, tvAiQuestion, tvAiCategory;
    private EditText etUserAnswer;
    private MaterialCardView cardFeedback;
    private TextView tvFeedbackTitle, tvFeedbackMessage;
    private LinearLayout layoutPhaseInterview;

    private int currentQuestionIndex = 0;
    private int timerSeconds = 0;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private int totalScore = 0;

    private String[] questions = {"Cuéntame sobre ti y por qué este puesto.", "¿Cuál ha sido tu mayor desafío técnico?", "¿Cómo diseñarías un sistema escalable?"};
    private String[] categories = {"Introducción", "Experiencia técnica", "Diseño de sistemas"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview_simulator);

        // Inicialización de vistas
        phaseSetup = findViewById(R.id.layout_phase_setup);
        phaseInterview = findViewById(R.id.layout_phase_interview);
        layoutPhaseInterview = findViewById(R.id.layout_phase_interview);
        spinnerCompany = findViewById(R.id.spinner_company);
        spinnerRole = findViewById(R.id.spinner_role);
        btnStart = findViewById(R.id.btn_start_simulation);

        btnVoice = findViewById(R.id.btn_voice);
        btnCamera = findViewById(R.id.btn_camera);
        btnIdea = findViewById(R.id.btn_idea);

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

        btnSend.setGravity(Gravity.CENTER);

        // Listeners Multimedia
        btnStart.setOnClickListener(v -> startInterview());
        btnSend.setOnClickListener(v -> simulateFeedback());
        btnNext.setOnClickListener(v -> loadNextQuestion());

        btnVoice.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            startActivityForResult(intent, 101);
        });
        btnCamera.setOnClickListener(v -> startActivityForResult(new Intent(MediaStore.ACTION_VIDEO_CAPTURE), 102));
        btnIdea.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Tip").setMessage("Usa el método STAR (Situación, Tarea, Acción, Resultado).").setPositiveButton("OK", null).show());

        setupSpinners();
        setupTimerLogic();
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bnv = findViewById(R.id.bottom_navigation);
        bnv.setSelectedItemId(R.id.nav_simulator);
        bnv.setOnItemSelectedListener(item -> {
            if (phaseInterview.getVisibility() == View.VISIBLE) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("¿Salir de la simulación?")
                        .setMessage("Si sales ahora, se borrará tu progreso. ¿Continuar?")
                        .setPositiveButton("Salir", (d, w) -> navigateTo(item.getItemId()))
                        .setNegativeButton("Quedarme", null)
                        .show();
                return false;
            }
            navigateTo(item.getItemId());
            return true;
        });
    }

    private void navigateTo(int itemId) {
        if (itemId == R.id.nav_dashboard) startActivity(new Intent(this, DashboardActivity.class));
        else if (itemId == R.id.nav_profile) startActivity(new Intent(this, UserProfileActivity.class));
        else if (itemId == R.id.nav_cv) startActivity(new Intent(this, CvReviewerActivity.class));
        else if (itemId == R.id.nav_forum) startActivity(new Intent(this, ForumActivity.class));
        overridePendingTransition(0, 0);
    }

    private void setupSpinners() {
        String[] companies = {"Google", "Microsoft", "CEMEX", "Bimbo"};
        String[] roles = {"Software Engineer", "Desarrollador Backend", "Data Engineer"};
        spinnerCompany.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, companies));
        spinnerRole.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles));
    }

    private void startInterview() {
        phaseSetup.setVisibility(View.GONE);
        phaseInterview.setVisibility(View.VISIBLE);
        currentQuestionIndex = 0;
        totalScore = 0;
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
        String answer = etUserAnswer.getText().toString().trim();
        if(answer.isEmpty()) { Toast.makeText(this, "Escribe algo", Toast.LENGTH_SHORT).show(); return; }
        stopTimer();
        btnSend.setVisibility(View.GONE);
        int score = (answer.length() < 15) ? 45 : 80;
        totalScore += score;
        tvFeedbackTitle.setText("Puntuación: " + score + "/100");
        cardFeedback.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
    }

    private void loadNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.length) {
            loadQuestionData();
            startTimer();
        } else {
            finish();
        }
    }

    private void setupTimerLogic() {
        timerRunnable = () -> {
            timerSeconds++;
            tvTimer.setText(String.format("%02d:%02d", timerSeconds / 60, timerSeconds % 60));
            timerHandler.postDelayed(timerRunnable, 1000);
        };
    }

    private void startTimer() { timerSeconds = 0; timerHandler.post(timerRunnable); }
    private void stopTimer() { timerHandler.removeCallbacks(timerRunnable); }
}
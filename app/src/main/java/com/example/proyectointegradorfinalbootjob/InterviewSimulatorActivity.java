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
    private android.widget.RadioGroup rgResponseMode;

    // Estado lógico
    private int currentQuestionIndex = 0;
    private int timerSeconds = 0;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private int totalScore = 0;

    // Banco de Preguntas (Se adaptará dinámicamente)
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

    private void loadCustomQuestions() {
        String company = spinnerCompany.getText().toString().trim();
        String role = spinnerRole.getText().toString().trim();

        if (company.isEmpty()) company = "Google";
        if (role.isEmpty()) role = "Software Engineer";

        if (role.contains("Backend")) {
            questions = new String[]{
                "¿Cómo manejarías la consistencia de datos eventual en una arquitectura de microservicios?",
                "¿Qué consideraciones de escalabilidad e indexación tomarías al diseñar una base de datos PostgreSQL de gran volumen?",
                "Explica el funcionamiento de un token JWT y cómo securizarías tus endpoints REST de forma eficiente."
            };
            categories = new String[]{"Arquitectura", "Bases de Datos", "Seguridad"};
        } else if (role.contains("Data")) {
            questions = new String[]{
                "¿Cuál es la diferencia técnica entre procesos ETL y ELT y en qué escenarios recomendarías usar cada uno?",
                "¿Cómo optimizarías una consulta SQL lenta que involucra múltiples JOINs en tablas de millones de filas?",
                "Describe tu experiencia diseñando pipelines de datos en batch (lotes) comparado con procesamiento en tiempo real."
            };
            categories = new String[]{"Procesamiento de datos", "Optimización SQL", "Pipelines"};
        } else { // Software Engineer
            questions = new String[]{
                "Cuéntame sobre una ocasión en la que tuviste que resolver un problema de rendimiento complejo en producción.",
                "¿Cómo diseñarías un sistema acortador de URLs tipo Bit.ly que soporte alta concurrencia?",
                "Explica las ventajas de la Programación Orientada a Objetos frente a la programación funcional en un gran proyecto."
            };
            categories = new String[]{"Resolución de problemas", "Diseño de Sistemas", "Paradigma de código"};
        }

        // Adaptación específica a la empresa objetivo
        if (company.equals("CEMEX") || company.equals("Bimbo")) {
            questions[0] = "¿Cómo aplicarías tecnologías de software para optimizar la logística o la cadena de suministro inteligente en una empresa global como " + company + "?";
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
        if(answer.isEmpty()) {
            Toast.makeText(this, "Por favor escribe o graba una respuesta", Toast.LENGTH_SHORT).show();
            return;
        }

        stopTimer();
        btnSend.setVisibility(View.GONE);

        // Lógica de evaluación dinámica
        int score = 0;
        String title = "";
        String msg = "";

        boolean isVoice = rgResponseMode != null && rgResponseMode.getCheckedRadioButtonId() == R.id.rb_voice;

        if (answer.length() < 15) {
            score = 45;
            title = "Respuesta muy breve - " + score + "/100";
            msg = "Tu respuesta es demasiado corta. En una entrevista técnica, intenta estructurar tu respuesta con más detalle utilizando el método STAR (Situación, Tarea, Acción, Resultado).";
            layoutPhaseInterview.setBackgroundResource(R.color.destructive); // Rojo
        } else {
            String answerLower = answer.toLowerCase();
            int countKeywords = 0;
            if (answerLower.contains("star") || answerLower.contains("situación") || answerLower.contains("resultado")) countKeywords++;
            if (answerLower.contains("diseñ") || answerLower.contains("arquitectura") || answerLower.contains("estructura")) countKeywords++;
            if (answerLower.contains("optimiza") || answerLower.contains("escalab") || answerLower.contains("rendimiento") || answerLower.contains("sql")) countKeywords++;
            if (answerLower.contains("ejemplo") || answerLower.contains("proyecto") || answerLower.contains("caso")) countKeywords++;

            if (countKeywords >= 3) {
                score = 88 + (int)(Math.random() * 10);
                title = "Excelente respuesta - " + score + "/100";
                msg = "¡Fantástico! Has estructurado tu respuesta con gran claridad, mencionando aspectos técnicos clave y proporcionando contexto sólido de manera profesional.";
                layoutPhaseInterview.setBackgroundResource(R.color.interview_bg_green_end); // Verde
            } else if (countKeywords >= 1) {
                score = 68 + (int)(Math.random() * 15);
                title = "Respuesta adecuada - " + score + "/100";
                msg = "Buen intento. Incluyes detalles relevantes, pero podrías profundizar más en las decisiones técnicas tomadas y el impacto cuantitativo de tus soluciones.";
                layoutPhaseInterview.setBackgroundResource(R.color.interview_bg_yellow_end); // Amarillo
            } else {
                score = 52 + (int)(Math.random() * 15);
                title = "Respuesta imprecisa - " + score + "/100";
                msg = "Respuesta aceptable, pero carece de tecnicismos y estructura. Trata de enfocar la respuesta hacia tus contribuciones directas y el stack tecnológico utilizado.";
                layoutPhaseInterview.setBackgroundResource(R.color.interview_bg_yellow_end); // Amarillo
            }
        }

        if (isVoice) {
            Toast.makeText(this, "🎙️ Audio procesado correctamente (Simulación de voz a texto)", Toast.LENGTH_SHORT).show();
        }

        totalScore += score;

        tvFeedbackTitle.setText(title);
        tvFeedbackMessage.setText(msg);

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
            int averageScore = totalScore / questions.length;
            String performanceText;
            if (averageScore >= 85) {
                performanceText = "¡Excelente preparación! Estás listo para postularte.";
            } else if (averageScore >= 70) {
                performanceText = "Buen rendimiento, pero hay detalles que pulir antes de tu entrevista.";
            } else {
                performanceText = "Te sugerimos practicar más usando nuestro simulador y revisando las áreas de mejora.";
            }

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("¡Simulación Finalizada!")
                .setMessage(String.format("Puntuación Promedio: %d/100\n\n%s\n\nTe recomendamos practicar más en: %s.", 
                    averageScore, performanceText, categories[(int)(Math.random() * categories.length)]))
                .setCancelable(false)
                .setPositiveButton("Regresar al Dashboard", (dialog, which) -> finish())
                .show();
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
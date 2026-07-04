package com.example.proyectointegradorfinalbootjob;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        // Enlace de componentes XML con variables Locales (Impecable)
        MaterialButton btnIngresar = findViewById(R.id.btn_ingresar);
        MaterialButton btnRegistro = findViewById(R.id.btn_registro);
        MaterialButton btnHeroStart = findViewById(R.id.btn_hero_start);
        MaterialButton btnBottomCTA = findViewById(R.id.btn_bottom_cta);
        MaterialButton btnHeroDemo = findViewById(R.id.btnHeroDemo);

        // CONFIGURACIÓN DE NAVEGACIÓN (Eventos Click con Lambdas)
        // Al pulsar "Ingresar"
        btnIngresar.setOnClickListener(v -> navigateToAuth("login"));

        // Al pulsar los botones destinados a Registro
        View.OnClickListener registerListener = v -> navigateToAuth("register");

        btnRegistro.setOnClickListener(registerListener);
        btnHeroStart.setOnClickListener(registerListener);
        btnBottomCTA.setOnClickListener(registerListener);

        // Al pulsar "Ver Demo"
        btnHeroDemo.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Demo de BootJob")
                .setMessage("¡Bienvenido a BootJob! Con esta aplicación podrás simular entrevistas de trabajo técnicas, recibir feedback instantáneo de IA para tu currículum (ATS) y compartir tus experiencias en el foro. ¿Deseas crear una cuenta para comenzar?")
                .setPositiveButton("Registrarse", (dialog, which) -> navigateToAuth("register"))
                .setNegativeButton("Ver más tarde", null)
                .show();
        });
    }

    /**
     * Centraliza la navegación hacia la pantalla de Autenticación pasándole un parámetro.
     * @param targetTab "login" o "register"
     */
    private void navigateToAuth(String targetTab) {
        // Apuntamos hacia la clase AuthActivity
        Intent intent = new Intent(LandingActivity.this, AuthActivity.class);
        intent.putExtra("TARGET_TAB", targetTab);
        startActivity(intent);
    }
}

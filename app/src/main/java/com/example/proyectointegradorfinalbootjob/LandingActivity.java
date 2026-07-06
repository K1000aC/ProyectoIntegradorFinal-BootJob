package com.example.proyectointegradorfinalbootjob;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        // Enlace de componentes XML con variables Locales
        MaterialButton btnIngresar = findViewById(R.id.btn_ingresar);
        MaterialButton btnRegistro = findViewById(R.id.btn_registro);
        MaterialButton btnHeroStart = findViewById(R.id.btn_hero_start);
        MaterialButton btnBottomCTA = findViewById(R.id.btn_bottom_cta);

        // CONFIGURACIÓN DE NAVEGACIÓN
        // Al pulsar "Ingresar"
        btnIngresar.setOnClickListener(v -> navigateToAuth("login"));

        // Al pulsar los botones destinados a Registro
        View.OnClickListener registerListener = v -> navigateToAuth("register");

        btnRegistro.setOnClickListener(registerListener);
        btnHeroStart.setOnClickListener(registerListener);
        btnBottomCTA.setOnClickListener(registerListener);
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
package com.example.proyectointegradorfinalbootjob;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

public class AuthActivity extends AppCompatActivity {

    private boolean isLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // 1. Enlaces con las Vistas del XML
        TabLayout tabLayout = findViewById(R.id.tab_layout_auth);
        LinearLayout layoutRegisterFields = findViewById(R.id.layout_register_fields);
        TextInputLayout layoutConfirmPassword = findViewById(R.id.layout_confirm_password);
        Button btnForgotPassword = findViewById(R.id.btn_forgot_password);
        Button btnSubmit = findViewById(R.id.btn_submit_auth);
        ProgressBar progressBar = findViewById(R.id.progress_bar_auth);
        Spinner spinnerCarrera = findViewById(R.id.spinner_carrera);

        // 2. Poblar el Spinner de Carreras desde el arrays.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.careers_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCarrera.setAdapter(adapter);

        // 3. Recibir el parámetro "TARGET_TAB" desde LandingActivity
        String targetTab = getIntent().getStringExtra("TARGET_TAB");
        if (targetTab != null && targetTab.equals("register")) {
            isLogin = false;
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            if (tab != null) tab.select();
            updateUI(layoutRegisterFields, layoutConfirmPassword, btnForgotPassword, btnSubmit);
        } else {
            isLogin = true;
            updateUI(layoutRegisterFields, layoutConfirmPassword, btnForgotPassword, btnSubmit);
        }

        // 4. Cambiar dinámicamente entre Login y Registro al pulsar las pestañas
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isLogin = (tab.getPosition() == 0);
                updateUI(layoutRegisterFields, layoutConfirmPassword, btnForgotPassword, btnSubmit);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 5. Botón Volver Atrás
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 6. Simulación de Submit (handleSubmit de React)
        btnSubmit.setOnClickListener(v -> {
            btnSubmit.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            // Simulamos retraso de red de 1.5 segundos (como en tu setTimeout)
            new Handler().postDelayed(() -> {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);

                if (isLogin) {
                    Toast.makeText(AuthActivity.this, "¡Bienvenido de vuelta!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AuthActivity.this, "¡Cuenta creada exitosamente!", Toast.LENGTH_SHORT).show();
                }

                // Aquí hay que lanzar el DashboardActivity cuando lo tengamos creado:
                // startActivity(new Intent(AuthActivity.this, DashboardActivity.class));
                // finish();
            }, 1500);
        });
    }

    // Metodo auxiliar para ocultar/mostrar elementos según la pestaña activa
    private void updateUI(LinearLayout regFields, TextInputLayout confirmPass, Button forgotPass, Button submitBtn) {
        if (isLogin) {
            regFields.setVisibility(View.GONE);
            confirmPass.setVisibility(View.GONE);
            forgotPass.setVisibility(View.VISIBLE);
            submitBtn.setText("Iniciar sesión");
        } else {
            regFields.setVisibility(View.VISIBLE);
            confirmPass.setVisibility(View.VISIBLE);
            forgotPass.setVisibility(View.GONE);
            submitBtn.setText("Crear mi cuenta");
        }
    }
}
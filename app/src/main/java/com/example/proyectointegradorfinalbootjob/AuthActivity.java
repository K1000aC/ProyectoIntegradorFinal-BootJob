package com.example.proyectointegradorfinalbootjob;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class AuthActivity extends AppCompatActivity {

    private boolean isLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // 1. Enlaces con las Vistas del XML
        TabLayout tabLayout = findViewById(R.id.tab_layout_auth);
        
        // Add tabs programmatically to avoid XML rendering issues with TabItem
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_login));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_register));

        LinearLayout layoutRegisterFields = findViewById(R.id.layout_register_fields);
        TextInputLayout layoutConfirmPassword = findViewById(R.id.layout_confirm_password);
        Button btnForgotPassword = findViewById(R.id.btn_forgot_password);
        Button btnSubmit = findViewById(R.id.btn_submit_auth);
        ProgressBar progressBar = findViewById(R.id.progress_bar_auth);
        AutoCompleteTextView spinnerCarrera = findViewById(R.id.spinner_carrera);

        // 2. Poblar el Spinner de Carreras desde el arrays.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.careers_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCarrera.setAdapter(adapter);

        // 3. Recibir el parámetro "TARGET_TAB" desde LandingActivity
        String targetTab = getIntent().getStringExtra("TARGET_TAB");
        if (Objects.equals(targetTab, "register")) {
            isLogin = false;
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            if (tab != null) tab.select();
        } else {
            isLogin = true;
        }
        updateUI(layoutRegisterFields, layoutConfirmPassword, btnForgotPassword, btnSubmit);

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

            // Simulamos retraso de red de 1.5 segundos
            new Handler().postDelayed(() -> {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);

                if (isLogin) {
                    Toast.makeText(AuthActivity.this, getString(R.string.toast_login_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AuthActivity.this, getString(R.string.toast_register_success), Toast.LENGTH_SHORT).show();
                }

                // Navegación al Dashboard
                startActivity(new Intent(AuthActivity.this, DashboardActivity.class));
                finish();
            }, 1500);
        });
    }

    /**
     * Método auxiliar para ocultar/mostrar elementos según la pestaña activa
     */
    private void updateUI(LinearLayout regFields, TextInputLayout confirmPass, Button forgotPass, Button submitBtn) {
        if (isLogin) {
            regFields.setVisibility(View.GONE);
            confirmPass.setVisibility(View.GONE);
            forgotPass.setVisibility(View.VISIBLE);
            submitBtn.setText(R.string.btn_login_submit);
        } else {
            regFields.setVisibility(View.VISIBLE);
            confirmPass.setVisibility(View.VISIBLE);
            forgotPass.setVisibility(View.GONE);
            submitBtn.setText(R.string.btn_register_submit);
        }
    }
}

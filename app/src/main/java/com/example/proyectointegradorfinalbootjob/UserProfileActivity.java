package com.example.proyectointegradorfinalbootjob;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserProfileActivity extends AppCompatActivity {

    private LinearLayout layoutPersonal, layoutSecurity, layoutNotifications;
    private boolean isEditMode = false;

    // Campos del formulario personal
    private EditText etNombre, etApellido, etCarrera, etBio;
    private Button btnEditProfile, btnSaveProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Enlazar Contenedores
        layoutPersonal = findViewById(R.id.layout_tab_personal);
        layoutSecurity = findViewById(R.id.layout_tab_security);
        layoutNotifications = findViewById(R.id.layout_tab_notifications);

        // Enlazar Campos de Texto
        etNombre = findViewById(R.id.et_nombre);
        etApellido = findViewById(R.id.et_apellido);
        etCarrera = findViewById(R.id.et_carrera);
        etBio = findViewById(R.id.et_bio);

        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnSaveProfile = findViewById(R.id.btn_save_profile);

        // Configurar Pestañas (Tabs)
        TabLayout tabLayout = findViewById(R.id.tab_layout_profile);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Ocultar todos primero
                layoutPersonal.setVisibility(View.GONE);
                layoutSecurity.setVisibility(View.GONE);
                layoutNotifications.setVisibility(View.GONE);

                // Mostrar el correcto según la posición
                switch (tab.getPosition()) {
                    case 0: layoutPersonal.setVisibility(View.VISIBLE); break;
                    case 1: layoutSecurity.setVisibility(View.VISIBLE); break;
                    case 2: layoutNotifications.setVisibility(View.VISIBLE); break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Lógica del Botón Editar
        btnEditProfile.setOnClickListener(v -> toggleEditMode(true));

        // Lógica del Botón Guardar
        btnSaveProfile.setOnClickListener(v -> {
            toggleEditMode(false);
            Toast.makeText(UserProfileActivity.this, "Cambios guardados correctamente.", Toast.LENGTH_SHORT).show();
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

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
                startActivity(new Intent(getApplicationContext(), CvReviewerActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_forum) {
                startActivity(new Intent(getApplicationContext(), ForumActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    private void toggleEditMode(boolean enable) {
        isEditMode = enable;

        // Habilitar o deshabilitar los campos
        etNombre.setEnabled(enable);
        etApellido.setEnabled(enable);
        etCarrera.setEnabled(enable);
        etBio.setEnabled(enable);

        // Alternar visibilidad de botones
        if (enable) {
            btnEditProfile.setVisibility(View.GONE);
            btnSaveProfile.setVisibility(View.VISIBLE);
        } else {
            btnEditProfile.setVisibility(View.VISIBLE);
            btnSaveProfile.setVisibility(View.GONE);
        }
    }
}
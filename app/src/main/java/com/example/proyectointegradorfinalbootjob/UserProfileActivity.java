package com.example.proyectointegradorfinalbootjob;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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

        // Cargar datos guardados de SharedPreferences o usar valores predeterminados
        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String nombre = prefs.getString("nombre", "Camila");
        String apellido = prefs.getString("apellido", "Suárez");
        String carrera = prefs.getString("carrera", "Ingeniería en Sistemas Computacionales");
        String bio = prefs.getString("bio", "Estudiante de CUCEI apasionada por el desarrollo de software.");
        String username = prefs.getString("username", "camila.sc");

        etNombre.setText(nombre);
        etApellido.setText(apellido);
        etCarrera.setText(carrera);
        etBio.setText(bio);

        // Actualizar vistas del encabezado del perfil
        TextView tvProfileName = findViewById(R.id.tv_profile_name);
        TextView tvProfileUsername = findViewById(R.id.tv_profile_username);
        TextView tvProfileInitials = findViewById(R.id.tv_profile_initials);

        if (tvProfileName != null) tvProfileName.setText(nombre + " " + apellido);
        if (tvProfileUsername != null) tvProfileUsername.setText("@" + username);
        if (tvProfileInitials != null) {
            String initials = "";
            if (!nombre.isEmpty()) initials += nombre.substring(0, 1).toUpperCase();
            if (!apellido.isEmpty()) initials += apellido.substring(0, 1).toUpperCase();
            if (initials.isEmpty()) initials = "CS";
            tvProfileInitials.setText(initials);
        }

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

            String nuevoNombre = etNombre.getText().toString().trim();
            String nuevoApellido = etApellido.getText().toString().trim();
            String nuevaCarrera = etCarrera.getText().toString().trim();
            String nuevaBio = etBio.getText().toString().trim();

            // Guardar en SharedPreferences
            android.content.SharedPreferences.Editor editor = prefs.edit();
            editor.putString("nombre", nuevoNombre);
            editor.putString("apellido", nuevoApellido);
            editor.putString("carrera", nuevaCarrera);
            editor.putString("bio", nuevaBio);
            editor.apply();

            Toast.makeText(UserProfileActivity.this, "Cambios guardados correctamente.", Toast.LENGTH_SHORT).show();

            // Simular actualizar los textos superiores en base a los editTexts
            if (tvProfileName != null) {
                tvProfileName.setText(nuevoNombre + " " + nuevoApellido);
            }
            if (tvProfileInitials != null) {
                String initials = "";
                if (!nuevoNombre.isEmpty()) initials += nuevoNombre.substring(0, 1).toUpperCase();
                if (!nuevoApellido.isEmpty()) initials += nuevoApellido.substring(0, 1).toUpperCase();
                if (initials.isEmpty()) initials = "CS";
                tvProfileInitials.setText(initials);
            }
        });

        bindSecurityAndNotificationButtons();

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) {
            return; // Evita el crash en layouts que no contienen la barra de navegación
        }
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

    private void bindSecurityAndNotificationButtons() {
        // Encontrar y enlazar el botón de actualizar contraseña en layoutSecurity
        findAndBindButton(layoutSecurity, "Actualizar contraseña", v -> {
            Toast.makeText(this, "Contraseña actualizada exitosamente.", Toast.LENGTH_SHORT).show();
        });

        // Encontrar y enlazar el botón de guardar preferencias en layoutNotifications
        findAndBindButton(layoutNotifications, "Guardar preferencias", v -> {
            Toast.makeText(this, "Preferencias de notificación guardadas.", Toast.LENGTH_SHORT).show();
        });
    }

    private void findAndBindButton(android.view.ViewGroup viewGroup, String buttonText, android.view.View.OnClickListener listener) {
        if (viewGroup == null) return;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            android.view.View child = viewGroup.getChildAt(i);
            if (child instanceof android.widget.Button) {
                android.widget.Button btn = (android.widget.Button) child;
                if (buttonText == null || btn.getText().toString().equalsIgnoreCase(buttonText)) {
                    btn.setOnClickListener(listener);
                }
            } else if (child instanceof android.view.ViewGroup) {
                findAndBindButton((android.view.ViewGroup) child, buttonText, listener);
            }
        }
    }
}
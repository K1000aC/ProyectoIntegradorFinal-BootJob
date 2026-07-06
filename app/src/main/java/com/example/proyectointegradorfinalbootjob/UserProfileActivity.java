package com.example.proyectointegradorfinalbootjob;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AppCompatActivity;

public class UserProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Vincular vistas
        TextInputEditText etNombre = findViewById(R.id.et_nombre);
        TextInputEditText etApellido = findViewById(R.id.et_apellido);
        TextInputEditText etEmail = findViewById(R.id.et_email);
        TextInputEditText etPhone = findViewById(R.id.et_phone);
        TextInputEditText etCarrera = findViewById(R.id.et_carrera);
        TextView tvNombre = findViewById(R.id.tv_profile_name);
        TextView tvUsername = findViewById(R.id.tv_profile_username);
        TextView tvInitials = findViewById(R.id.tv_profile_initials);
        MaterialButton btnLogout = findViewById(R.id.btn_logout);

        // Cargar datos reales desde la sesión
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        String nombre = prefs.getString("nombre", "");
        String apellido = prefs.getString("apellido", "");
        String email = prefs.getString("email", "");
        String phone = prefs.getString("celular", "");
        String carrera = prefs.getString("carrera", "");
        String username = prefs.getString("username", "");

        // Rellenar campos dinámicamente
        etNombre.setText(nombre);
        etApellido.setText(apellido);
        etEmail.setText(email);
        etPhone.setText(phone);
        etCarrera.setText(carrera);

        tvNombre.setText(nombre + " " + apellido);
        tvUsername.setText("@" + username);

        // Poner iniciales automáticas
        if (nombre.length() > 0 && apellido.length() > 0) {
            tvInitials.setText(nombre.substring(0,1).toUpperCase() + apellido.substring(0,1).toUpperCase());
        }

        // Configurar Logout
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(UserProfileActivity.this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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
            } else if (itemId == R.id.nav_simulator) {
                startActivity(new Intent(getApplicationContext(), InterviewSimulatorActivity.class));
            } else if (itemId == R.id.nav_cv) {
                startActivity(new Intent(getApplicationContext(), CvReviewerActivity.class));
            } else if (itemId == R.id.nav_forum) {
                startActivity(new Intent(getApplicationContext(), ForumActivity.class));
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            overridePendingTransition(0, 0);
            return true;
        });
    }
}
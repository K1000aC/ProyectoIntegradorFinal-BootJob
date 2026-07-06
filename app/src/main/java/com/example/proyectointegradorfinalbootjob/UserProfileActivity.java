package com.example.proyectointegradorfinalbootjob;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UserProfileActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etApellido, etEmail, etPhone, etCarrera;
    private TextView tvNombre, tvUsername, tvInitials;
    private MaterialButton btnLogout, btnEditProfile;
    private boolean isEditing = false;
    private SharedPreferences prefs;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // Aquí se subiría a Supabase Storage en una implementación real
                    Toast.makeText(this, "Foto seleccionada (Simulado)", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        etNombre = findViewById(R.id.et_nombre);
        etApellido = findViewById(R.id.et_apellido);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etCarrera = findViewById(R.id.et_carrera);
        tvNombre = findViewById(R.id.tv_profile_name);
        tvUsername = findViewById(R.id.tv_profile_username);
        tvInitials = findViewById(R.id.tv_profile_initials);
        btnLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Intent intent = new Intent(UserProfileActivity.this, LandingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        btnEditProfile = findViewById(R.id.btn_edit_profile);

        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        loadUserData();

        btnEditProfile.setOnClickListener(v -> toggleEditMode());
        findViewById(R.id.btn_change_photo).setOnClickListener(v -> getContent.launch("image/*"));

        btnLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Intent intent = new Intent(UserProfileActivity.this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        setupBottomNavigation();
    }

    private void loadUserData() {
        String nombre = prefs.getString("nombre", "");
        String apellido = prefs.getString("apellido", "");
        String email = prefs.getString("email", "");
        String phone = prefs.getString("celular", "");
        String carrera = prefs.getString("carrera", "");
        String username = prefs.getString("username", "");

        etNombre.setText(nombre);
        etApellido.setText(apellido);
        etEmail.setText(email);
        etPhone.setText(phone);
        etCarrera.setText(carrera);

        tvNombre.setText(nombre + " " + apellido);
        tvUsername.setText("@" + username);

        if (nombre.length() > 0 && apellido.length() > 0) {
            tvInitials.setText(nombre.substring(0, 1).toUpperCase() + apellido.substring(0, 1).toUpperCase());
        }
    }

    private void toggleEditMode() {
        if (isEditing) {
            saveProfileChanges();
        } else {
            isEditing = true;
            btnEditProfile.setText("Guardar Cambios");
            setFieldsEnabled(true);
        }
    }

    private void setFieldsEnabled(boolean enabled) {
        etNombre.setEnabled(enabled);
        etApellido.setEnabled(enabled);
        etPhone.setEnabled(enabled);
        etCarrera.setEnabled(enabled);
    }

    private void saveProfileChanges() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String carrera = etCarrera.getText().toString().trim();
        String userId = prefs.getString("userId", "");
        String token = prefs.getString("token", "");
        String username = prefs.getString("username", "");

        SupabaseManager.updateUser(userId, nombre, apellido, username, phone, carrera, token, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(UserProfileActivity.this, "Error de red", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("nombre", nombre);
                        editor.putString("apellido", apellido);
                        editor.putString("celular", phone);
                        editor.putString("carrera", carrera);
                        editor.apply();

                        isEditing = false;
                        btnEditProfile.setText("Editar Perfil");
                        setFieldsEnabled(false);
                        loadUserData();
                        Toast.makeText(UserProfileActivity.this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserProfileActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) startActivity(new Intent(this, DashboardActivity.class));
            else if (itemId == R.id.nav_simulator) startActivity(new Intent(this, InterviewSimulatorActivity.class));
            else if (itemId == R.id.nav_cv) startActivity(new Intent(this, CvReviewerActivity.class));
            else if (itemId == R.id.nav_forum) startActivity(new Intent(this, ForumActivity.class));
            else if (itemId == R.id.nav_profile) return true;
            overridePendingTransition(0, 0);
            return true;
        });
    }
}
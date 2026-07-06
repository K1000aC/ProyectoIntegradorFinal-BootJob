package com.example.proyectointegradorfinalbootjob;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;
import java.util.Objects;

public class AuthActivity extends AppCompatActivity {

    private boolean isLogin = true;
    private ProgressBar progressBar;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        TabLayout tabLayout = findViewById(R.id.tab_layout_auth);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_login));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_register));

        LinearLayout layoutRegisterFields = findViewById(R.id.layout_register_fields);
        TextInputLayout layoutConfirmPassword = findViewById(R.id.layout_confirm_password);
        Button btnForgotPassword = findViewById(R.id.btn_forgot_password);

        btnSubmit = findViewById(R.id.btn_submit_auth);
        progressBar = findViewById(R.id.progress_bar_auth);
        AutoCompleteTextView spinnerCarrera = findViewById(R.id.spinner_carrera);

        com.google.android.material.textfield.TextInputEditText edtNombre = findViewById(R.id.edt_nombre);
        com.google.android.material.textfield.TextInputEditText edtApellido = findViewById(R.id.edt_apellido);
        com.google.android.material.textfield.TextInputEditText edtUsername = findViewById(R.id.edt_username);
        com.google.android.material.textfield.TextInputEditText edtCelular = findViewById(R.id.edt_celular);
        com.google.android.material.textfield.TextInputEditText edtCorreo = findViewById(R.id.edt_correo);
        com.google.android.material.textfield.TextInputEditText edtPassword = findViewById(R.id.edt_password);
        com.google.android.material.textfield.TextInputEditText edtConfirmPassword = findViewById(R.id.edt_confirm_password);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.careers_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCarrera.setAdapter(adapter);

        String targetTab = getIntent().getStringExtra("TARGET_TAB");
        if (Objects.equals(targetTab, "register")) {
            isLogin = false;
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            if (tab != null) tab.select();
        }
        updateUI(layoutRegisterFields, layoutConfirmPassword, btnForgotPassword, btnSubmit);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isLogin = (tab.getPosition() == 0);
                clearAllTextInputLayoutErrors((ViewGroup) findViewById(android.R.id.content));
                updateUI(layoutRegisterFields, layoutConfirmPassword, btnForgotPassword, btnSubmit);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            clearAllTextInputLayoutErrors((ViewGroup) findViewById(android.R.id.content));

            boolean hasError = false;
            String email = edtCorreo.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                setErrorOnEditText(edtCorreo, "Formato de correo no válido");
                hasError = true;
            }
            if (password.length() < 6) {
                setErrorOnEditText(edtPassword, "La contraseña debe tener al menos 6 caracteres");
                hasError = true;
            }

            if (!isLogin) {
                if (edtNombre.getText().toString().trim().isEmpty()) { setErrorOnEditText(edtNombre, "Requerido"); hasError = true; }
                if (edtApellido.getText().toString().trim().isEmpty()) { setErrorOnEditText(edtApellido, "Requerido"); hasError = true; }
                if (edtUsername.getText().toString().trim().isEmpty()) { setErrorOnEditText(edtUsername, "Requerido"); hasError = true; }
                if (spinnerCarrera.getText().toString().trim().isEmpty()) { setErrorOnEditText(spinnerCarrera, "Requerido"); hasError = true; }
                if (!password.equals(edtConfirmPassword.getText().toString().trim())) {
                    setErrorOnEditText(edtConfirmPassword, "Las contraseñas no coinciden");
                    hasError = true;
                }
            }

            if (hasError) return;

            btnSubmit.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            if (isLogin) {
                // PETICIÓN REAL DE INICIO DE SESIÓN
                SupabaseManager.loginUser(email, password, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> resetUIWithError("Fallo de red: " + e.getMessage()));
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            runOnUiThread(() -> {
                                Toast.makeText(AuthActivity.this, "¡Bienvenido de vuelta!", Toast.LENGTH_SHORT).show();
                                navigateToDashboard();
                            });
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "Error desconocido";
                            runOnUiThread(() -> resetUIWithError("Error login: " + errorBody));
                        }
                    }
                });
            } else {
                // PETICIÓN REAL DE REGISTRO
                SupabaseManager.registerUser(
                        email, password,
                        edtNombre.getText().toString().trim(),
                        edtApellido.getText().toString().trim(),
                        edtUsername.getText().toString().trim(),
                        edtCelular.getText().toString().trim(),
                        spinnerCarrera.getText().toString().trim(),
                        new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                runOnUiThread(() -> resetUIWithError("Fallo de red: " + e.getMessage()));
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    // Guardar temporalmente los datos en el celular para el Perfil
                                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("nombre", edtNombre.getText().toString().trim());
                                    editor.putString("apellido", edtApellido.getText().toString().trim());
                                    editor.putString("username", edtUsername.getText().toString().trim());
                                    editor.putString("celular", edtCelular.getText().toString().trim());
                                    editor.putString("carrera", spinnerCarrera.getText().toString().trim());
                                    editor.putString("email", email);
                                    editor.apply();

                                    runOnUiThread(() -> {
                                        Toast.makeText(AuthActivity.this, "Cuenta creada con éxito.", Toast.LENGTH_LONG).show();
                                        navigateToDashboard();
                                    });
                                } else {
                                    String errorBody = response.body() != null ? response.body().string() : "Error desconocido";
                                    runOnUiThread(() -> resetUIWithError("Rechazado por Supabase: " + errorBody));
                                }
                            }
                        }
                );
            }
        });
    }

    private void navigateToDashboard() {
        progressBar.setVisibility(View.GONE);
        btnSubmit.setEnabled(true);
        startActivity(new Intent(AuthActivity.this, DashboardActivity.class));
        finishAffinity();
    }

    private void resetUIWithError(String message) {
        progressBar.setVisibility(View.GONE);
        btnSubmit.setEnabled(true);
        Toast.makeText(AuthActivity.this, message, Toast.LENGTH_LONG).show();
    }

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

    private void setErrorOnEditText(EditText editText, String error) {
        if (editText == null) return;
        android.view.ViewParent parent = editText.getParent();
        while (parent != null) {
            if (parent instanceof TextInputLayout) {
                ((TextInputLayout) parent).setError(error);
                ((TextInputLayout) parent).setErrorEnabled(true);
                return;
            }
            parent = parent.getParent();
        }
        editText.setError(error);
    }

    private void clearAllTextInputLayoutErrors(ViewGroup viewGroup) {
        if (viewGroup == null) return;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof TextInputLayout) {
                ((TextInputLayout) child).setError(null);
                ((TextInputLayout) child).setErrorEnabled(false);
            } else if (child instanceof ViewGroup) {
                clearAllTextInputLayoutErrors((ViewGroup) child);
            }
        }
    }
}
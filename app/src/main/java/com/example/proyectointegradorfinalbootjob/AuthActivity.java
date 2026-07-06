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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;
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

        TextInputEditText edtNombre = findViewById(R.id.edt_nombre);
        TextInputEditText edtApellido = findViewById(R.id.edt_apellido);
        TextInputEditText edtUsername = findViewById(R.id.edt_username);
        TextInputEditText edtCelular = findViewById(R.id.edt_celular);
        TextInputEditText edtCorreo = findViewById(R.id.edt_correo);
        TextInputEditText edtPassword = findViewById(R.id.edt_password);
        TextInputEditText edtConfirmPassword = findViewById(R.id.edt_confirm_password);

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

            String email = Objects.requireNonNull(edtCorreo.getText()).toString().trim();
            String password = Objects.requireNonNull(edtPassword.getText()).toString().trim();

            if (!validateInputs(email, password, edtNombre, edtApellido, edtUsername, spinnerCarrera, edtConfirmPassword)) return;

            btnSubmit.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            if (isLogin) {
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
                            String errorBody = response.body() != null ? response.body().string() : "Error";
                            runOnUiThread(() -> resetUIWithError("Error login: " + errorBody));
                        }
                    }
                });
            } else
                SupabaseManager.registerUser(email, password, edtNombre.getText().toString().trim(), edtApellido.getText().toString().trim(), edtUsername.getText().toString().trim(), Objects.requireNonNull(edtCelular.getText()).toString().trim(), spinnerCarrera.getText().toString().trim(), new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> resetUIWithError("Fallo de red: " + e.getMessage()));
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            try {
                                String responseBody = response.body() != null ? response.body().string() : "";
                                JSONObject jsonResponse = new JSONObject(responseBody);
                                String userId = jsonResponse.optString("id", "");

                                SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                                editor.putString("userId", userId);
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
                            } catch (Exception e) {
                                runOnUiThread(() -> resetUIWithError("Error procesando sesión."));
                            }
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "Error";
                            runOnUiThread(() -> resetUIWithError("Rechazado por Supabase: " + errorBody));
                        }
                    }
                });
        });
    }

    private boolean validateInputs(String email, String password, EditText n, EditText a, EditText u, AutoCompleteTextView c, EditText cp) {
        boolean hasError = false;
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setErrorOnEditText(findViewById(R.id.edt_correo), "Correo no válido");
            hasError = true;
        }
        if (password.length() < 6) {
            setErrorOnEditText(findViewById(R.id.edt_password), "Mínimo 6 caracteres");
            hasError = true;
        }
        if (!isLogin) {
            if (n.getText().toString().trim().isEmpty()) { setErrorOnEditText(n, "Requerido"); hasError = true; }
            if (a.getText().toString().trim().isEmpty()) { setErrorOnEditText(a, "Requerido"); hasError = true; }
            if (u.getText().toString().trim().isEmpty()) { setErrorOnEditText(u, "Requerido"); hasError = true; }
            if (c.getText().toString().trim().isEmpty()) { setErrorOnEditText(c, "Requerido"); hasError = true; }
            if (!password.equals(cp.getText().toString().trim())) {
                setErrorOnEditText(cp, "No coinciden");
                hasError = true;
            }
        }
        return !hasError;
    }

    private void navigateToDashboard() {
        progressBar.setVisibility(View.GONE);
        startActivity(new Intent(AuthActivity.this, DashboardActivity.class));
        finishAffinity();
    }

    private void resetUIWithError(String message) {
        progressBar.setVisibility(View.GONE);
        btnSubmit.setEnabled(true);
        Toast.makeText(AuthActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void updateUI(LinearLayout reg, TextInputLayout cp, Button fp, Button sub) {
        if (isLogin) {
            reg.setVisibility(View.GONE);
            cp.setVisibility(View.GONE);
            fp.setVisibility(View.VISIBLE);
            sub.setText(R.string.btn_login_submit);
        } else {
            reg.setVisibility(View.VISIBLE);
            cp.setVisibility(View.VISIBLE);
            fp.setVisibility(View.GONE);
            sub.setText(R.string.btn_register_submit);
        }
    }

    private void setErrorOnEditText(EditText et, String error) {
        if (et != null && et.getParent() instanceof TextInputLayout) {
            ((TextInputLayout) et.getParent()).setError(error);
        }
    }

    private void clearAllTextInputLayoutErrors(ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            if (child instanceof TextInputLayout) ((TextInputLayout) child).setError(null);
            else if (child instanceof ViewGroup) clearAllTextInputLayoutErrors((ViewGroup) child);
        }
    }
}
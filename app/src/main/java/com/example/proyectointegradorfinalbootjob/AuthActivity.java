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

        // Campos adicionales del registro
        com.google.android.material.textfield.TextInputEditText edtNombre = findViewById(R.id.edt_nombre);
        com.google.android.material.textfield.TextInputEditText edtApellido = findViewById(R.id.edt_apellido);
        com.google.android.material.textfield.TextInputEditText edtUsername = findViewById(R.id.edt_username);
        com.google.android.material.textfield.TextInputEditText edtCelular = findViewById(R.id.edt_celular);
        com.google.android.material.textfield.TextInputEditText edtCorreo = findViewById(R.id.edt_correo);
        com.google.android.material.textfield.TextInputEditText edtPassword = findViewById(R.id.edt_password);
        com.google.android.material.textfield.TextInputEditText edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        android.widget.RadioGroup rgEstadoCarrera = findViewById(R.id.rg_estado_carrera);

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
                clearAllTextInputLayoutErrors((android.view.ViewGroup) findViewById(android.R.id.content));
                updateUI(layoutRegisterFields, layoutConfirmPassword, btnForgotPassword, btnSubmit);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 5. Botón Volver Atrás
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Recuperar Contraseña
        btnForgotPassword.setOnClickListener(v -> {
            clearAllTextInputLayoutErrors((android.view.ViewGroup) findViewById(android.R.id.content));
            String email = edtCorreo.getText().toString().trim();
            if (email.isEmpty()) {
                setErrorOnEditText(edtCorreo, "Introduce tu correo electrónico para restablecer la contraseña");
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                setErrorOnEditText(edtCorreo, "Formato de correo no válido");
            } else {
                Toast.makeText(this, "Enlace de recuperación enviado a: " + email, Toast.LENGTH_LONG).show();
            }
        });

        // 6. Simulación de Submit (handleSubmit de React) con Validaciones
        btnSubmit.setOnClickListener(v -> {
            clearAllTextInputLayoutErrors((android.view.ViewGroup) findViewById(android.R.id.content));

            boolean hasError = false;
            String email = edtCorreo.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty()) {
                setErrorOnEditText(edtCorreo, "El correo electrónico es obligatorio");
                hasError = true;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                setErrorOnEditText(edtCorreo, "Formato de correo no válido");
                hasError = true;
            }

            if (password.isEmpty()) {
                setErrorOnEditText(edtPassword, "La contraseña es obligatoria");
                hasError = true;
            } else if (password.length() < 6) {
                setErrorOnEditText(edtPassword, "La contraseña debe tener al menos 6 caracteres");
                hasError = true;
            }

            if (isLogin && !hasError) {
                // Verificar si hay credenciales guardadas en SharedPreferences
                android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String registeredEmail = prefs.getString("email", "");
                String registeredPassword = prefs.getString("password", "");
                if (!registeredEmail.isEmpty()) {
                    if (!registeredEmail.equalsIgnoreCase(email) || !registeredPassword.equals(password)) {
                        setErrorOnEditText(edtCorreo, "El correo o la contraseña son incorrectos");
                        hasError = true;
                    }
                }
            }

            if (!isLogin) {
                // Validaciones para Registro
                String nombre = edtNombre.getText().toString().trim();
                String apellido = edtApellido.getText().toString().trim();
                String username = edtUsername.getText().toString().trim();
                String celular = edtCelular.getText().toString().trim();
                String carrera = spinnerCarrera.getText().toString().trim();
                String confirmPass = edtConfirmPassword.getText().toString().trim();

                if (nombre.isEmpty()) {
                    setErrorOnEditText(edtNombre, "El nombre es obligatorio");
                    hasError = true;
                }
                if (apellido.isEmpty()) {
                    setErrorOnEditText(edtApellido, "El apellido es obligatorio");
                    hasError = true;
                }
                if (username.isEmpty()) {
                    setErrorOnEditText(edtUsername, "El nombre de usuario es obligatorio");
                    hasError = true;
                }
                if (celular.isEmpty()) {
                    setErrorOnEditText(edtCelular, "El número celular es obligatorio");
                    hasError = true;
                }
                if (carrera.isEmpty()) {
                    setErrorOnEditText(spinnerCarrera, "Debe seleccionar una carrera");
                    hasError = true;
                }
                if (confirmPass.isEmpty()) {
                    setErrorOnEditText(edtConfirmPassword, "Debe confirmar su contraseña");
                    hasError = true;
                } else if (!password.equals(confirmPass)) {
                    setErrorOnEditText(edtConfirmPassword, "Las contraseñas no coinciden");
                    hasError = true;
                }
            }

            if (hasError) {
                return;
            }

            btnSubmit.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            // Simulamos retraso de red de 1.5 segundos
            new Handler().postDelayed(() -> {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);

                if (isLogin) {
                    Toast.makeText(AuthActivity.this, getString(R.string.toast_login_success), Toast.LENGTH_SHORT).show();
                } else {
                    // Guardar datos en SharedPreferences
                    android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    android.content.SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("nombre", edtNombre.getText().toString().trim());
                    editor.putString("apellido", edtApellido.getText().toString().trim());
                    editor.putString("username", edtUsername.getText().toString().trim());
                    editor.putString("celular", edtCelular.getText().toString().trim());
                    editor.putString("carrera", spinnerCarrera.getText().toString().trim());
                    editor.putString("email", email);
                    editor.putString("password", password);
                    editor.apply();

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

    /**
     * Establece un mensaje de error en el TextInputLayout contenedor del EditText si existe.
     */
    private void setErrorOnEditText(android.widget.EditText editText, String error) {
        if (editText == null) return;
        android.view.ViewParent parent = editText.getParent();
        while (parent != null) {
            if (parent instanceof com.google.android.material.textfield.TextInputLayout) {
                ((com.google.android.material.textfield.TextInputLayout) parent).setError(error);
                ((com.google.android.material.textfield.TextInputLayout) parent).setErrorEnabled(true);
                return;
            }
            parent = parent.getParent();
        }
        editText.setError(error);
    }

    /**
     * Limpia recursivamente todos los errores de TextInputLayout en el layout.
     */
    private void clearAllTextInputLayoutErrors(android.view.ViewGroup viewGroup) {
        if (viewGroup == null) return;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            android.view.View child = viewGroup.getChildAt(i);
            if (child instanceof com.google.android.material.textfield.TextInputLayout) {
                ((com.google.android.material.textfield.TextInputLayout) child).setError(null);
                ((com.google.android.material.textfield.TextInputLayout) child).setErrorEnabled(false);
            } else if (child instanceof android.view.ViewGroup) {
                clearAllTextInputLayoutErrors((android.view.ViewGroup) child);
            }
        }
    }
}

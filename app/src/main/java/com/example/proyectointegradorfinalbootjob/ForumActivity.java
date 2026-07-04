package com.example.proyectointegradorfinalbootjob;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ForumActivity extends AppCompatActivity {

    private RecyclerView recyclerForum;
    private PostAdapter postAdapter;
    private List<Post> postList;

    private String currentSearchQuery = "";
    private String currentSelectedTag = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        // Enlazar vistas
        recyclerForum = findViewById(R.id.recycler_forum);
        ChipGroup chipGroupTags = findViewById(R.id.chip_group_tags);
        ExtendedFloatingActionButton fabNewPost = findViewById(R.id.fab_new_post);
        android.widget.EditText etSearchForum = findViewById(R.id.et_search_forum);

        // Configurar RecyclerView
        recyclerForum.setLayoutManager(new LinearLayoutManager(this));
        loadInitialPosts();
        postAdapter = new PostAdapter(new ArrayList<>(postList));
        recyclerForum.setAdapter(postAdapter);

        // Configurar búsqueda dinámica
        if (etSearchForum != null) {
            etSearchForum.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString().toLowerCase().trim();
                    filterPosts();
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        // Cargar Chips de categorías
        String[] tags = {"Todos", "algoritmos", "HR", "backend", "técnica", "manufactura"};
        for (String tag : tags) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCheckable(true);
            if (tag.equals("Todos")) chip.setChecked(true);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    currentSelectedTag = tag;
                    filterPosts();
                }
            });
            chipGroupTags.addView(chip);
        }

        // Clic en compartir experiencia (Diálogo personalizado programático)
        fabNewPost.setOnClickListener(v -> {
            android.widget.LinearLayout dialogLayout = new android.widget.LinearLayout(this);
            dialogLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
            dialogLayout.setPadding(48, 24, 48, 24);

            android.widget.EditText edtAuthor = new android.widget.EditText(this);
            edtAuthor.setHint("Tu Nombre (ej: Juan Pérez)");
            edtAuthor.setSingleLine(true);
            dialogLayout.addView(edtAuthor);

            android.widget.EditText edtCompany = new android.widget.EditText(this);
            edtCompany.setHint("Empresa (ej: Google)");
            edtCompany.setSingleLine(true);
            dialogLayout.addView(edtCompany);

            android.widget.EditText edtRole = new android.widget.EditText(this);
            edtRole.setHint("Puesto (ej: Backend Developer)");
            edtRole.setSingleLine(true);
            dialogLayout.addView(edtRole);

            android.widget.EditText edtTitle = new android.widget.EditText(this);
            edtTitle.setHint("Título del relato");
            edtTitle.setSingleLine(true);
            dialogLayout.addView(edtTitle);

            android.widget.EditText edtContent = new android.widget.EditText(this);
            edtContent.setHint("Cuéntanos tu experiencia...");
            edtContent.setLines(4);
            dialogLayout.addView(edtContent);

            // Márgenes para los inputs
            for (int i = 0; i < dialogLayout.getChildCount(); i++) {
                android.view.View child = dialogLayout.getChildAt(i);
                android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );
                lp.setMargins(0, 0, 0, 24);
                child.setLayoutParams(lp);
            }

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Compartir Experiencia")
                .setView(dialogLayout)
                .setPositiveButton("Publicar", (dialog, which) -> {
                    String author = edtAuthor.getText().toString().trim();
                    String company = edtCompany.getText().toString().trim();
                    String role = edtRole.getText().toString().trim();
                    String title = edtTitle.getText().toString().trim();
                    String content = edtContent.getText().toString().trim();

                    if (author.isEmpty() || company.isEmpty() || role.isEmpty() || title.isEmpty() || content.isEmpty()) {
                        Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String initials = "";
                    String[] parts = author.split(" ");
                    if (parts.length > 0 && !parts[0].isEmpty()) initials += parts[0].substring(0, 1).toUpperCase();
                    if (parts.length > 1 && !parts[1].isEmpty()) initials += parts[1].substring(0, 1).toUpperCase();
                    if (initials.isEmpty()) initials = "U";

                    Post newPost = new Post(
                        postList.size() + 1,
                        author,
                        initials,
                        "Estudiante · Reciente",
                        company,
                        role,
                        "Hace un momento",
                        title,
                        content,
                        0,
                        0
                    );

                    postList.add(0, newPost);
                    filterPosts();
                    Toast.makeText(this, "¡Experiencia publicada con éxito!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_forum);

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
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), UserProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadInitialPosts() {
        postList = new ArrayList<>();
        postList.add(new Post(1, "Carlos Mendoza", "CM", "Ing. Sistemas — TEC", "Google", "Software Engineer II", "Hace 2 horas", "Entrevista en Google — 5 rondas, logré la oferta 🎉", "Quiero compartir mi experiencia completa con Google LATAM para SWE II. El proceso fue 5 rondas de 45 min cada una...", 48, 12));
        postList.add(new Post(2, "Valeria Ríos", "VR", "Ing. Mecatrónica — UNAM", "Bimbo", "Ing. de Manufactura", "Hace 1 día", "Proceso de Bimbo para Manufactura", "El proceso en Bimbo fue: 1) Entrevista HR por Teams (30 min, preguntas de valores y motivación), 2) Caso de negocio...", 33, 8));
        postList.add(new Post(3, "Diego Fuentes", "DF", "Ing. en Software — UANL", "Clip", "Backend Developer", "Hace 3 días", "Clip — entrevista técnica enfocada en APIs", "Proceso rápido: screening de 20 min con recruiter, prueba técnica de 90 min (diseñar e implementar una API REST)...", 27, 5));
    }

    private void filterPosts() {
        List<Post> filteredList = new ArrayList<>();
        for (Post post : postList) {
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    post.author.toLowerCase().contains(currentSearchQuery) ||
                    post.company.toLowerCase().contains(currentSearchQuery) ||
                    post.role.toLowerCase().contains(currentSearchQuery) ||
                    post.title.toLowerCase().contains(currentSearchQuery) ||
                    post.content.toLowerCase().contains(currentSearchQuery);

            boolean matchesTag = currentSelectedTag.equalsIgnoreCase("Todos") ||
                    post.company.toLowerCase().contains(currentSelectedTag.toLowerCase()) ||
                    post.role.toLowerCase().contains(currentSelectedTag.toLowerCase()) ||
                    post.title.toLowerCase().contains(currentSelectedTag.toLowerCase()) ||
                    post.content.toLowerCase().contains(currentSelectedTag.toLowerCase());

            if (matchesSearch && matchesTag) {
                filteredList.add(post);
            }
        }
        postAdapter.updateList(filteredList);
    }
}
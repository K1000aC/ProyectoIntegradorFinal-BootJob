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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        // Enlazar vistas
        recyclerForum = findViewById(R.id.recycler_forum);
        ChipGroup chipGroupTags = findViewById(R.id.chip_group_tags);
        ExtendedFloatingActionButton fabNewPost = findViewById(R.id.fab_new_post);

        // Configurar RecyclerView
        recyclerForum.setLayoutManager(new LinearLayoutManager(this));
        loadInitialPosts();
        postAdapter = new PostAdapter(postList);
        recyclerForum.setAdapter(postAdapter);

        // Cargar Chips de categorías
        String[] tags = {"Todos", "algoritmos", "HR", "backend", "técnica", "manufactura"};
        for (String tag : tags) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCheckable(true);
            if (tag.equals("Todos")) chip.setChecked(true);
            chipGroupTags.addView(chip);
        }

        // Clic en compartir experiencia (Simulación del Modal)
        fabNewPost.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo modal de nueva experiencia...", Toast.LENGTH_SHORT).show();
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
}
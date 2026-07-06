package com.example.proyectointegradorfinalbootjob;

import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ForumActivity extends AppCompatActivity {

    private RecyclerView recyclerForum;
    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    private ProgressBar progressBar;

    private String currentSearchQuery = "";
    private String currentSelectedTag = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        recyclerForum = findViewById(R.id.recycler_forum);
        ChipGroup chipGroupTags = findViewById(R.id.chip_group_tags);
        ExtendedFloatingActionButton fabNewPost = findViewById(R.id.fab_new_post);
        android.widget.EditText etSearchForum = findViewById(R.id.et_search_forum);
        progressBar = new ProgressBar(this); // O buscarlo en el XML si existe

        recyclerForum.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(new ArrayList<>());
        recyclerForum.setAdapter(postAdapter);

        loadForumData();

        if (etSearchForum != null) {
            etSearchForum.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString().toLowerCase().trim();
                    filterPosts();
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        String[] tags = {"Todos", "algoritmos", "HR", "backend", "técnica", "manufactura"};
        for (String tag : tags) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCheckable(true);
            if (tag.equals("Todos")) chip.setChecked(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> { if (isChecked) { currentSelectedTag = tag; filterPosts(); } });
            chipGroupTags.addView(chip);
        }

        fabNewPost.setOnClickListener(v -> showNewPostDialog());

        setupBottomNavigation();
    }

    private void loadForumData() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");

        SupabaseManager.getForumPosts(token, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(ForumActivity.this, "Error al cargar foro", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                runOnUiThread(() -> {
                    try {
                        JSONArray array = new JSONArray(body);
                        postList.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            postList.add(new Post(
                                    obj.optInt("id", 0),
                                    obj.optString("author", "Usuario"),
                                    "U",
                                    "Estudiante",
                                    obj.optString("company", ""),
                                    obj.optString("role", ""),
                                    "Reciente",
                                    obj.optString("title", ""),
                                    obj.optString("content", ""),
                                    0, 0
                            ));
                        }
                        filterPosts();
                    } catch (Exception e) { e.printStackTrace(); }
                });
            }
        });
    }

    private void showNewPostDialog() {
        android.widget.LinearLayout dialogLayout = new android.widget.LinearLayout(this);
        dialogLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        dialogLayout.setPadding(48, 24, 48, 24);

        android.widget.EditText edtTitle = new android.widget.EditText(this);
        edtTitle.setHint("Título");
        dialogLayout.addView(edtTitle);

        android.widget.EditText edtCompany = new android.widget.EditText(this);
        edtCompany.setHint("Empresa");
        dialogLayout.addView(edtCompany);

        android.widget.EditText edtRole = new android.widget.EditText(this);
        edtRole.setHint("Puesto");
        dialogLayout.addView(edtRole);

        android.widget.EditText edtContent = new android.widget.EditText(this);
        edtContent.setHint("Tu experiencia...");
        edtContent.setLines(4);
        dialogLayout.addView(edtContent);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Publicar Experiencia")
            .setView(dialogLayout)
            .setPositiveButton("Publicar", (dialog, which) -> {
                savePost(edtTitle.getText().toString(), edtCompany.getText().toString(), edtRole.getText().toString(), edtContent.getText().toString());
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void savePost(String title, String company, String role, String content) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", "");
        String author = prefs.getString("nombre", "Usuario") + " " + prefs.getString("apellido", "");
        String token = prefs.getString("token", "");

        SupabaseManager.saveForumPost(userId, author, company, role, title, content, token, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(ForumActivity.this, "Error de red", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(ForumActivity.this, "Publicado", Toast.LENGTH_SHORT).show();
                        loadForumData();
                    }
                });
            }
        });
    }

    private void filterPosts() {
        List<Post> filteredList = new ArrayList<>();
        for (Post post : postList) {
            boolean matchesSearch = currentSearchQuery.isEmpty() || post.title.toLowerCase().contains(currentSearchQuery) || post.content.toLowerCase().contains(currentSearchQuery);
            boolean matchesTag = currentSelectedTag.equalsIgnoreCase("Todos") || post.company.toLowerCase().contains(currentSelectedTag.toLowerCase());
            if (matchesSearch && matchesTag) filteredList.add(post);
        }
        postAdapter.updateList(filteredList);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_forum);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) startActivity(new Intent(this, DashboardActivity.class));
            else if (itemId == R.id.nav_simulator) startActivity(new Intent(this, InterviewSimulatorActivity.class));
            else if (itemId == R.id.nav_cv) startActivity(new Intent(this, CvReviewerActivity.class));
            else if (itemId == R.id.nav_profile) startActivity(new Intent(this, UserProfileActivity.class));
            else if (itemId == R.id.nav_forum) return true;
            overridePendingTransition(0, 0);
            return true;
        });
    }
}
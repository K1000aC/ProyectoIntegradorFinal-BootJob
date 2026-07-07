package com.example.proyectointegradorfinalbootjob;

import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
    private String currentSelectedTag = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        recyclerForum = findViewById(R.id.recycler_forum);
        ChipGroup chipGroupTags = findViewById(R.id.chip_group_tags);
        ExtendedFloatingActionButton fabNewPost = findViewById(R.id.fab_new_post);

        recyclerForum.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(new ArrayList<>());
        recyclerForum.setAdapter(postAdapter);

        loadForumData();

        String[] tags = {"Todos", "Google", "Microsoft", "CEMEX", "Bimbo"};
        for (String tag : tags) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCheckable(true);
            if (tag.equals("Todos")) chip.setChecked(true);
            chip.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked) {
                    currentSelectedTag = tag;
                    filterPosts();
                }
            });
            chipGroupTags.addView(chip);
        }

        fabNewPost.setOnClickListener(v -> showNewPostDialog());
        setupBottomNavigation();
    }

    private void savePost(String title, String company, String role, String content) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", "");
        String author = prefs.getString("nombre", "Usuario");
        String token = prefs.getString("token", "");

        // Generar un ID local único
        int newId = (int)(System.currentTimeMillis() % 100000);
        String initials = "US";
        if (author.length() > 1) {
            String[] parts = author.split(" ");
            if (parts.length > 1) {
                initials = parts[0].substring(0, 1).toUpperCase() + parts[1].substring(0, 1).toUpperCase();
            } else {
                initials = author.substring(0, Math.min(2, author.length())).toUpperCase();
            }
        }

        Post localPost = new Post(newId, author, initials, "Estudiante", company, role, "Hace un momento", title, content, 0, 0);
        postList.add(0, localPost);
        saveLocalPosts();
        filterPosts();

        SupabaseManager.saveForumPost(userId, author, company, role, title, content, token, new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(ForumActivity.this, "Guardado localmente (Sin conexión)", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(ForumActivity.this, "¡Publicado en la nube!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ForumActivity.this, "Guardado localmente (Error Supabase)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadForumData() {
        loadLocalPosts();

        String token = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("token", "");
        SupabaseManager.getForumPosts(token, new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    if (postList.isEmpty()) {
                        showDefaultPosts();
                    } else {
                        filterPosts();
                    }
                });
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                runOnUiThread(() -> {
                    try {
                        JSONArray array = new JSONArray(body);
                        // Mezclar posts de Supabase sin duplicar los locales
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int id = obj.optInt("id");

                            boolean exists = false;
                            for (Post p : postList) {
                                if (p.id == id) {
                                    exists = true;
                                    break;
                                }
                            }

                            if (!exists) {
                                String authName = obj.optString("author");
                                String initials = "AB";
                                if (authName.length() > 1) {
                                    String[] parts = authName.split(" ");
                                    if (parts.length > 1) {
                                        initials = parts[0].substring(0, 1).toUpperCase() + parts[1].substring(0, 1).toUpperCase();
                                    } else {
                                        initials = authName.substring(0, Math.min(2, authName.length())).toUpperCase();
                                    }
                                }
                                postList.add(new Post(id, authName, initials, "Estudiante",
                                        obj.optString("company"), obj.optString("role"), "Reciente",
                                        obj.optString("title"), obj.optString("content"), 0, 0));
                            }
                        }
                        filterPosts();
                    } catch (Exception e) {
                        if (postList.isEmpty()) showDefaultPosts();
                        else filterPosts();
                    }
                });
            }
        });
    }

    private void showDefaultPosts() {
        postList.clear();
        postList.add(new Post(101, "Ana Garcia", "AG", "Software Engineer", "Google", "Frontend Developer", "Hace 2d", "Mi experiencia en Google", "El proceso fue técnico pero muy amigable.", 12, 4));
        saveLocalPosts();
        filterPosts();
    }

    private void saveLocalPosts() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray array = new JSONArray();
        for (Post p : postList) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id", p.id);
                obj.put("author", p.author);
                obj.put("initials", p.initials);
                obj.put("career", p.career);
                obj.put("company", p.company);
                obj.put("role", p.role);
                obj.put("date", p.date);
                obj.put("title", p.title);
                obj.put("content", p.content);
                obj.put("likes", p.likes);
                obj.put("comments", p.comments);
                array.put(obj);
            } catch (Exception e) { e.printStackTrace(); }
        }
        editor.putString("local_forum_posts", array.toString());
        editor.apply();
    }

    private void loadLocalPosts() {
        postList.clear();
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String json = prefs.getString("local_forum_posts", "");
        if (!json.isEmpty()) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    postList.add(new Post(
                        obj.optInt("id"),
                        obj.optString("author"),
                        obj.optString("initials"),
                        obj.optString("career"),
                        obj.optString("company"),
                        obj.optString("role"),
                        obj.optString("date"),
                        obj.optString("title"),
                        obj.optString("content"),
                        obj.optInt("likes"),
                        obj.optInt("comments")
                    ));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void showNewPostDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        EditText title = new EditText(this); title.setHint("Título");
        EditText comp = new EditText(this); comp.setHint("Empresa");
        EditText cont = new EditText(this); cont.setHint("Tu experiencia...");
        layout.addView(title); layout.addView(comp); layout.addView(cont);

        new MaterialAlertDialogBuilder(this).setTitle("Publicar Experiencia").setView(layout)
                .setPositiveButton("Publicar", (d, w) -> savePost(title.getText().toString(), comp.getText().toString(), "N/A", cont.getText().toString()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void filterPosts() {
        List<Post> filtered = new ArrayList<>();
        for (Post p : postList) {
            if (currentSelectedTag.equals("Todos") || p.company.equalsIgnoreCase(currentSelectedTag)) filtered.add(p);
        }
        postAdapter.updateList(filtered);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bnv = findViewById(R.id.bottom_navigation);
        bnv.setSelectedItemId(R.id.nav_forum);
        bnv.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) startActivity(new Intent(this, DashboardActivity.class));
            else if (itemId == R.id.nav_simulator) startActivity(new Intent(this, InterviewSimulatorActivity.class));
            else if (itemId == R.id.nav_profile) startActivity(new Intent(this, UserProfileActivity.class));
            overridePendingTransition(0, 0);
            return true;
        });
    }
}
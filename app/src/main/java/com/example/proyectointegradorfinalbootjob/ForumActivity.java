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

        JSONObject postJson = new JSONObject();
        try {
            postJson.put("user_id", userId);
            postJson.put("author", author);
            postJson.put("company", company);
            postJson.put("role", role);
            postJson.put("title", title);
            postJson.put("content", content);
        } catch (Exception e) { e.printStackTrace(); }

        SupabaseManager.saveForumPost(userId, author, company, role, title, content, token, new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(ForumActivity.this, "Error de red", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(ForumActivity.this, "¡Publicado con éxito!", Toast.LENGTH_SHORT).show();
                        loadForumData();
                    });
                }
            }
        });
    }

    private void loadForumData() {
        String token = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("token", "");
        SupabaseManager.getForumPosts(token, new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> showDefaultPosts());
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                runOnUiThread(() -> {
                    try {
                        postList.clear();
                        JSONArray array = new JSONArray(body);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            postList.add(new Post(obj.optInt("id"), obj.optString("author"), "AB", "Estudiante",
                                    obj.optString("company"), obj.optString("role"), "Reciente",
                                    obj.optString("title"), obj.optString("content"), 0, 0));
                        }
                        filterPosts();
                    } catch (Exception e) { showDefaultPosts(); }
                });
            }
        });
    }

    private void showDefaultPosts() {
        postList.clear();
        postList.add(new Post(101, "Ana Garcia", "AG", "Software Engineer", "Google", "Frontend Developer", "Hace 2d", "Mi experiencia en Google", "El proceso fue técnico pero muy amigable.", 12, 4));
        filterPosts();
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
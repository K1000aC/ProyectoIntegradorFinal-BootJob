package com.example.proyectointegradorfinalbootjob;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;

public class SupabaseManager {
    public static final String SUPABASE_URL = "https://cvtuwncfnccxhowfgzup.supabase.co";
    public static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImN2dHV3bmNmbmNjeGhvd2ZnenVwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODMzMDI5OTMsImV4cCI6MjA5ODg3ODk5M30.u3l06H72Ziw1BZOGpsglco9zqDJXavuDEF_qDE7cGXY";

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * Registra un usuario nuevo en Supabase y guarda sus datos adicionales (metadata).
     */
    public static void registerUser(String email, String password, String nombre, String apellido, String username, String celular, String carrera, Callback callback) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("password", password);

            JSONObject userData = new JSONObject();
            userData.put("nombre", nombre);
            userData.put("apellido", apellido);
            userData.put("username", username);
            userData.put("celular", celular);
            userData.put("carrera", carrera);

            jsonBody.put("data", userData);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/auth/v1/signup")
                    .addHeader("apikey", SUPABASE_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Inicia sesión validando credenciales en Supabase.
     */
    public static void loginUser(String email, String password, Callback callback) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("password", password);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/auth/v1/token?grant_type=password")
                    .addHeader("apikey", SUPABASE_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Consulta las estadísticas de un usuario en Supabase.
     */
    public static void getStatistics(String userId, String token, Callback callback) {
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/estadisticas?user_id=eq." + userId)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void saveSimulation(String userId, String empresa, String puesto, int score, String token, Callback callback) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_id", userId);
            jsonBody.put("empresa", empresa);
            jsonBody.put("puesto", puesto);
            jsonBody.put("score", score);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/rest/v1/estadisticas")
                    .addHeader("apikey", SUPABASE_KEY)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .post(body)
                    .build();
            client.newCall(request).enqueue(callback);
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Guarda una experiencia en el foro.
     */
    public static void saveForumPost(String userId, String author, String company, String role, String title, String content, String token, Callback callback) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_id", userId);
            jsonBody.put("author", author);
            jsonBody.put("company", company);
            jsonBody.put("role", role);
            jsonBody.put("title", title);
            jsonBody.put("content", content);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/rest/v1/foro")
                    .addHeader("apikey", SUPABASE_KEY)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .post(body)
                    .build();
            client.newCall(request).enqueue(callback);
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Obtiene las experiencias del foro.
     */
    public static void getForumPosts(String token, Callback callback) {
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/foro?select=*&order=created_at.desc")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * Actualiza los datos del usuario en Supabase (metadata).
     */
    public static void updateUser(String userId, String nombre, String apellido, String username, String celular, String carrera, String token, Callback callback) {
        try {
            JSONObject jsonBody = new JSONObject();
            JSONObject userData = new JSONObject();
            userData.put("nombre", nombre);
            userData.put("apellido", apellido);
            userData.put("username", username);
            userData.put("celular", celular);
            userData.put("carrera", carrera);

            jsonBody.put("data", userData);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/auth/v1/user")
                    .addHeader("apikey", SUPABASE_KEY)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .put(body)
                    .build();

            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
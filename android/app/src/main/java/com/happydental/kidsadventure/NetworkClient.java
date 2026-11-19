package com.happydental.kidsadventure;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkClient {
    private static final String TAG = "NetworkClient";
    // Using 10.0.2.2 to access the host loopback server from the Android emulator
    private static final String BASE_URL = "http://10.0.2.2:54321";
    
    private static NetworkClient instance;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String currentUserId = null;

    public interface Callback {
        void onSuccess(JSONObject response);
        void onError(String errorMessage);
    }

    public static synchronized NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    public void login(final String email, final String password, final Callback callback) {
        executor.execute(() -> {
            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("email", email);
                jsonBody.put("password", password);
                
                postRequest("/api/auth/login", jsonBody, new InternalCallback(callback) {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            if (response.optBoolean("success", false)) {
                                currentUserId = response.optString("userId", email);
                            }
                        } catch (Exception ignored) {}
                        super.onSuccess(response);
                    }
                });
            } catch (Exception e) {
                notifyError(callback, e.getMessage());
            }
        });
    }

    public void register(final String parentName, final String email, final String password, final Callback callback) {
        executor.execute(() -> {
            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("parentName", parentName);
                jsonBody.put("email", email);
                jsonBody.put("password", password);
                
                postRequest("/api/auth/register", jsonBody, new InternalCallback(callback) {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            if (response.optBoolean("success", false)) {
                                currentUserId = response.optString("userId", email);
                            }
                        } catch (Exception ignored) {}
                        super.onSuccess(response);
                    }
                });
            } catch (Exception e) {
                notifyError(callback, e.getMessage());
            }
        });
    }

    public void getProgress(final String userId, final Callback callback) {
        executor.execute(() -> getRequest("/api/progress/" + userId, callback));
    }

    public void completeGame(final String userId, final String gameId, final int scoreEarned, final int anxietyReduction, final Callback callback) {
        executor.execute(() -> {
            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("gameId", gameId);
                jsonBody.put("scoreEarned", scoreEarned);
                jsonBody.put("anxietyReduction", anxietyReduction);
                
                postRequest("/api/progress/" + userId + "/complete-game", jsonBody, callback);
            } catch (Exception e) {
                notifyError(callback, e.getMessage());
            }
        });
    }

    private void postRequest(String endpoint, JSONObject jsonBody, Callback callback) {
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            String bodyStr = jsonBody.toString();
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = bodyStr.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            BufferedReader br;
            if (responseCode >= 200 && responseCode < 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            Log.d(TAG, "POST " + endpoint + " Response Code: " + responseCode + " Body: " + response.toString());
            
            final JSONObject jsonResponse = new JSONObject(response.toString());
            if (responseCode >= 200 && responseCode < 300) {
                notifySuccess(callback, jsonResponse);
            } else {
                notifyError(callback, jsonResponse.optString("message", "Request failed"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in POST " + endpoint, e);
            notifyError(callback, e.getMessage());
        }
    }

    private void getRequest(String endpoint, Callback callback) {
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            BufferedReader br;
            if (responseCode >= 200 && responseCode < 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            Log.d(TAG, "GET " + endpoint + " Response Code: " + responseCode + " Body: " + response.toString());

            final JSONObject jsonResponse = new JSONObject(response.toString());
            if (responseCode >= 200 && responseCode < 300) {
                notifySuccess(callback, jsonResponse);
            } else {
                notifyError(callback, jsonResponse.optString("message", "Request failed"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in GET " + endpoint, e);
            notifyError(callback, e.getMessage());
        }
    }

    private void notifySuccess(final Callback callback, final JSONObject response) {
        mainHandler.post(() -> callback.onSuccess(response));
    }

    private void notifyError(final Callback callback, final String error) {
        mainHandler.post(() -> callback.onError(error != null ? error : "Unknown error occurred"));
    }

    private static class InternalCallback implements Callback {
        private final Callback target;
        InternalCallback(Callback target) { this.target = target; }
        @Override public void onSuccess(JSONObject response) { target.onSuccess(response); }
        @Override public void onError(String errorMessage) { target.onError(errorMessage); }
    }
}

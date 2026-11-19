package com.happydental.kidsadventure;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;

public class ParentPortalActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView tvAnxietyLabel;
    private ProgressBar pbAnxiety;
    private TextView tvReadinessLabel;
    private ProgressBar pbReadiness;
    private TextView tvGamesCompleted;
    private TextView tvCoinsEarned;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_portal);

        btnBack = findViewById(R.id.btnBack);
        tvAnxietyLabel = findViewById(R.id.tvAnxietyLabel);
        pbAnxiety = findViewById(R.id.pbAnxiety);
        tvReadinessLabel = findViewById(R.id.tvReadinessLabel);
        pbReadiness = findViewById(R.id.pbReadiness);
        tvGamesCompleted = findViewById(R.id.tvGamesCompleted);
        tvCoinsEarned = findViewById(R.id.tvCoinsEarned);

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchProgress();
    }

    private void fetchProgress() {
        String userId = NetworkClient.getInstance().getCurrentUserId();
        if (userId == null || userId.equals("offline_guest")) {
            // Display mock details for offline mode
            tvAnxietyLabel.setText("Anxiety Level: 35%");
            pbAnxiety.setProgress(35);
            tvReadinessLabel.setText("Readiness Level: 75%");
            pbReadiness.setProgress(75);
            tvGamesCompleted.setText("Games Completed: Brush Master (6)");
            tvCoinsEarned.setText("Total Coins Earned: 120");
            return;
        }

        NetworkClient.getInstance().getProgress(userId, new NetworkClient.Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    int coins = response.optInt("coins", 0);
                    int anxietyScore = response.optInt("anxietyScore", 50);
                    int readinessScore = response.optInt("readinessScore", 50);
                    
                    tvAnxietyLabel.setText("Anxiety Level: " + anxietyScore + "%");
                    pbAnxiety.setProgress(anxietyScore);
                    
                    tvReadinessLabel.setText("Readiness Level: " + readinessScore + "%");
                    pbReadiness.setProgress(readinessScore);
                    
                    tvCoinsEarned.setText("Total Coins Earned: " + coins);

                    JSONArray gamesCompleted = response.optJSONArray("gamesCompleted");
                    if (gamesCompleted != null && gamesCompleted.length() > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < gamesCompleted.length(); i++) {
                            String gameId = gamesCompleted.optString(i);
                            if (gameId.equals("6")) {
                                sb.append("Brush Master");
                            } else {
                                sb.append("Game ID: ").append(gameId);
                            }
                            if (i < gamesCompleted.length() - 1) {
                                sb.append(", ");
                            }
                        }
                        tvGamesCompleted.setText("Games Completed: " + sb.toString());
                    } else {
                        tvGamesCompleted.setText("Games Completed: None");
                    }
                } catch (Exception e) {
                    Toast.makeText(ParentPortalActivity.this, "Error parsing statistics", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ParentPortalActivity.this, "Could not fetch remote stats: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

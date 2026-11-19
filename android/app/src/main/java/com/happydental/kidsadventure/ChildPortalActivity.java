package com.happydental.kidsadventure;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import org.json.JSONObject;

public class ChildPortalActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView tvLevel;
    private TextView tvCoins;
    private TextView tvXpLabel;
    private ProgressBar pbXp;
    private CardView cardBrushMaster;
    private Button btnPlayBrush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_portal);

        btnBack = findViewById(R.id.btnBack);
        tvLevel = findViewById(R.id.tvLevel);
        tvCoins = findViewById(R.id.tvCoins);
        tvXpLabel = findViewById(R.id.tvXpLabel);
        pbXp = findViewById(R.id.pbXp);
        cardBrushMaster = findViewById(R.id.cardBrushMaster);
        btnPlayBrush = findViewById(R.id.btnPlayBrush);

        btnBack.setOnClickListener(v -> finish());

        btnPlayBrush.setOnClickListener(v -> {
            Intent intent = new Intent(this, BrushMasterActivity.class);
            startActivity(intent);
        });

        cardBrushMaster.setOnClickListener(v -> {
            Intent intent = new Intent(this, BrushMasterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchProgress();
    }

    private void fetchProgress() {
        String userId = NetworkClient.getInstance().getCurrentUserId();
        if (userId == null || userId.equals("offline_guest")) {
            // Local mockup details for offline mode
            tvCoins.setText("🪙 120 Coins");
            tvLevel.setText("Level 2 Explorer 🐻");
            tvXpLabel.setText("XP: 140/200");
            pbXp.setMax(200);
            pbXp.setProgress(140);
            return;
        }

        NetworkClient.getInstance().getProgress(userId, new NetworkClient.Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    int coins = response.optInt("coins", 0);
                    int level = response.optInt("level", 1);
                    int xp = response.optInt("xp", 0);
                    
                    tvCoins.setText("🪙 " + coins + " Coins");
                    tvLevel.setText("Level " + level + " Explorer 🐻");
                    
                    // Simple leveling calculation: 100 XP per level
                    int levelBaseXp = (level - 1) * 100;
                    int levelProgressXp = xp - levelBaseXp;
                    if (levelProgressXp < 0) levelProgressXp = 0;
                    
                    tvXpLabel.setText("XP: " + levelProgressXp + "/100");
                    pbXp.setMax(100);
                    pbXp.setProgress(levelProgressXp);
                } catch (Exception e) {
                    Toast.makeText(ChildPortalActivity.this, "Error parsing statistics", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ChildPortalActivity.this, "Could not fetch remote stats: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

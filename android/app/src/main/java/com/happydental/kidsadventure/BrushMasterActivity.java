package com.happydental.kidsadventure;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;

public class BrushMasterActivity extends AppCompatActivity {
    private static final int TOTAL_SPOTS = 12;
    private static final int INITIAL_TIME_LEFT = 30; // seconds

    private ImageButton btnBack;
    private TextView tvTimer;
    private ProgressBar pbCleaned;
    private RelativeLayout rlGameArea;
    private View[] spots = new View[TOTAL_SPOTS];
    private boolean[] spotCleaned = new boolean[TOTAL_SPOTS];

    private CountDownTimer gameTimer;
    private int timeLeft = INITIAL_TIME_LEFT;
    private boolean isGameOver = false;
    private int cleanedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brush_master);

        btnBack = findViewById(R.id.btnBack);
        tvTimer = findViewById(R.id.tvTimer);
        pbCleaned = findViewById(R.id.pbCleaned);
        rlGameArea = findViewById(R.id.rlGameArea);

        btnBack.setOnClickListener(v -> finish());

        // Bind spot views
        for (int i = 0; i < TOTAL_SPOTS; i++) {
            int resId = getResources().getIdentifier("spot" + (i + 1), "id", getPackageName());
            spots[i] = findViewById(resId);
            spotCleaned[i] = false;
        }

        // Set up dragging brush listener
        rlGameArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isGameOver) return false;
                
                int action = event.getAction();
                if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN) {
                    float rawX = event.getRawX();
                    float rawY = event.getRawY();
                    checkSpotsBrushing(rawX, rawY);
                }
                return true;
            }
        });

        startGame();
    }

    private void checkSpotsBrushing(float rawX, float rawY) {
        for (int i = 0; i < TOTAL_SPOTS; i++) {
            if (!spotCleaned[i] && spots[i] != null) {
                if (isTouchInsideView(rawX, rawY, spots[i])) {
                    cleanSpot(i);
                }
            }
        }
    }

    private boolean isTouchInsideView(float touchX, float touchY, View view) {
        if (view.getVisibility() != View.VISIBLE) return false;
        
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x1 = location[0];
        int y1 = location[1];
        int x2 = x1 + view.getWidth();
        int y2 = y1 + view.getHeight();
        
        // Add a small padding/tolerance so it's easier to brush on small screens
        int padding = 15; 
        return (touchX >= (x1 - padding) && touchX <= (x2 + padding) &&
                touchY >= (y1 - padding) && touchY <= (y2 + padding));
    }

    private void cleanSpot(int index) {
        spotCleaned[index] = true;
        spots[index].animate().alpha(0f).setDuration(250).withEndAction(() -> {
            spots[index].setVisibility(View.INVISIBLE);
        });

        cleanedCount++;
        pbCleaned.setProgress(cleanedCount);

        if (cleanedCount == TOTAL_SPOTS) {
            winGame();
        }
    }

    private void startGame() {
        cleanedCount = 0;
        pbCleaned.setMax(TOTAL_SPOTS);
        pbCleaned.setProgress(0);
        isGameOver = false;

        for (int i = 0; i < TOTAL_SPOTS; i++) {
            spotCleaned[i] = false;
            if (spots[i] != null) {
                spots[i].setVisibility(View.VISIBLE);
                spots[i].setAlpha(1.0f);
            }
        }

        // Start Countdown Timer
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        
        gameTimer = new CountDownTimer(INITIAL_TIME_LEFT * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = (int) (millisUntilFinished / 1000);
                tvTimer.setText("Time: " + timeLeft + "s");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("Time: 0s");
                if (cleanedCount < TOTAL_SPOTS) {
                    loseGame();
                }
            }
        }.start();
    }

    private void winGame() {
        isGameOver = true;
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        String userId = NetworkClient.getInstance().getCurrentUserId();
        if (userId == null || userId.equals("offline_guest")) {
            showResultDialog("Level Cleared! 🎉", "Awesome brushing! You cleaned all teeth and earned 50 Coins (Offline Mode).");
            return;
        }

        // Sync complete-game API
        NetworkClient.getInstance().completeGame(userId, "6", 50, 10, new NetworkClient.Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                showResultDialog("Level Cleared! 🎉", "Awesome brushing! Statistics synced successfully with Happy Dental. You earned 50 Coins and 50 XP!");
            }

            @Override
            public void onError(String errorMessage) {
                showResultDialog("Level Cleared! 🎉", "Awesome brushing! (Could not sync stats: " + errorMessage + ")");
            }
        });
    }

    private void loseGame() {
        isGameOver = true;
        showResultDialog("Time\'s Up! 😢", "Some sugar bugs are still on the teeth. Let\'s try brushing again!");
    }

    private void showResultDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        if (title.contains("Up")) {
            // For lost state, offer a retry option
            builder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startGame();
                }
            });
        }
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        super.onDestroy();
    }
}

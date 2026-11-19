package com.happydental.kidsadventure;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class DashboardActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private TextView tvUserEmail;
    private Button btnLogout;
    private CardView cardChild;
    private CardView cardParent;
    private CardView cardDentist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnLogout = findViewById(R.id.btnLogout);
        cardChild = findViewById(R.id.cardChild);
        cardParent = findViewById(R.id.cardParent);
        cardDentist = findViewById(R.id.cardDentist);

        String userId = NetworkClient.getInstance().getCurrentUserId();
        if (userId != null) {
            tvUserEmail.setText(userId);
            if (userId.equals("offline_guest")) {
                tvWelcome.setText("Welcome Guest!");
            } else {
                tvWelcome.setText("Welcome Parent!");
            }
        }

        btnLogout.setOnClickListener(v -> {
            NetworkClient.getInstance().setCurrentUserId(null);
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        cardChild.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChildPortalActivity.class);
            startActivity(intent);
        });

        cardParent.setOnClickListener(v -> {
            Intent intent = new Intent(this, ParentPortalActivity.class);
            startActivity(intent);
        });

        cardDentist.setOnClickListener(v -> {
            Toast.makeText(this, "🩺 Dentist Console loaded! Reading patients database...", Toast.LENGTH_LONG).show();
            // We can also reuse or navigate. Let's just toast for now or route to parent for clinical info
        });
    }
}

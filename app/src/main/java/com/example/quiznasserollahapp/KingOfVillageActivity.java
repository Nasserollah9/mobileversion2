package com.example.quiznasserollahapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class KingOfVillageActivity extends AppCompatActivity {

    private TextView tvMessage;
    private ImageView imgKing;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_king_of_village);

        tvMessage = findViewById(R.id.tvMessage);
        imgKing = findViewById(R.id.imgKing);
        btnLogout = findViewById(R.id.btnLogout);

        tvMessage.setText("You are the King of the Village!");
        imgKing.setImageResource(R.drawable.king);  // Use the appropriate image

        // Logout Button
        btnLogout.setOnClickListener(v -> {
            // Déconnexion Firebase
            FirebaseAuth.getInstance().signOut();

            // Réinitialiser l'état de connexion
            SharedPreferences sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.apply();

            // Redirection vers login
            Intent intent = new Intent(KingOfVillageActivity.this, login.class);
            startActivity(intent);
            finish();
        });

    }
}

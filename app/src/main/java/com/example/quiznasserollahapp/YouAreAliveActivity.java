package com.example.quiznasserollahapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class YouAreAliveActivity extends AppCompatActivity {

    private TextView tvMessage;
    private ImageView imgAlive;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_you_are_alive);

        tvMessage = findViewById(R.id.tvMessage);
        imgAlive = findViewById(R.id.imgAlive);
        btnLogout = findViewById(R.id.btnLogout);

        tvMessage.setText("You are alive!");
        imgAlive.setImageResource(R.drawable.alive);  // Use the appropriate image

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
            Intent intent = new Intent(YouAreAliveActivity.this, login.class);
            startActivity(intent);
            finish();
        });

    }
}

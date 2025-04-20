package com.example.quiznasserollahapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class DefeatActivity extends AppCompatActivity {

    private TextView tvMessage;
    private ImageView imgDefeat;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_defeat);

        tvMessage = findViewById(R.id.tvMessage);
        imgDefeat = findViewById(R.id.imgDefeat);
        btnLogout = findViewById(R.id.btnLogout);

        tvMessage.setText("You died");
        imgDefeat.setImageResource(R.drawable.dead);  // Use the appropriate image

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
            Intent intent = new Intent(DefeatActivity.this, login.class);
            startActivity(intent);
            finish();
        });

    }
}

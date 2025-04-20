package com.example.quiznasserollahapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AccidentActivity extends AppCompatActivity {

    private TextView tvAccidentMessage;
    private ImageView ivAccidentImage;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident);

        // Initialize views
        tvAccidentMessage = findViewById(R.id.tvAccidentMessage);
        ivAccidentImage = findViewById(R.id.ivAccidentImage);
        btnLogout = findViewById(R.id.btnLogout);

        // Set the text and image for the accident scenario
        tvAccidentMessage.setText("You were afraid and ran back to the road. Unfortunately, a car hit you, and you died in the accident.");
        ivAccidentImage.setImageResource(R.drawable.accident);  // Use the appropriate image

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
            Intent intent = new Intent(AccidentActivity.this, login.class);
            startActivity(intent);
            finish();
        });

    }
}

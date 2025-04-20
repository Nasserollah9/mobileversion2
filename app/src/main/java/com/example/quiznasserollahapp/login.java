package com.example.quiznasserollahapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class login extends AppCompatActivity {
    // Declare necessary variables
    EditText etLogin, etPassword;
    Button bLogin;
    TextView tvRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views by their IDs
        etLogin = findViewById(R.id.etMail);  // Email EditText
        etPassword = findViewById(R.id.etPassword);  // Password EditText
        bLogin = findViewById(R.id.bLogin);  // Login Button
        tvRegister = findViewById(R.id.tvRegister);  // Register TextView

        // Check if user is already logged in
        checkUserLoginStatus();

        // Set onClickListener for Login Button
        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etLogin.getText().toString();
                String password = etPassword.getText().toString();

                // Basic validation to check if email and password fields are filled
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Attempt to sign in using Firebase
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Successful login
                                    Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_SHORT).show();

                                    // Save login state
                                    SharedPreferences sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putBoolean("isLoggedIn", true);
                                    editor.apply();

                                    // Navigate to main screen
                                    startActivity(new Intent(login.this, storybegin.class)); // Change to your main screen activity
                                    finish();
                                } else {
                                    // Failed login
                                    Toast.makeText(getApplicationContext(), "Login Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Set onClickListener for Register TextView to navigate to Register activity
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(login.this, register.class));  // Navigate to Register activity
            }
        });
    }

    // Check if the user is already logged in
    private void checkUserLoginStatus() {
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPref.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // If the user is already logged in, navigate to the main screen
            startActivity(new Intent(login.this, storybegin.class)); // Change to your main screen activity
            finish();  // Close the login activity so user can't return to it using the back button
        }
    }
}

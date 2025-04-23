package com.example.quiznasserollahapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class VillageActivity extends AppCompatActivity {

    private TextView tvStory;
    private RadioGroup radioGroup;
    private Button btnNext;

    private int bloodScore = 500;  // Initialize blood score

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_village);

        // Get the uploaded image name and face path from Intent

        String uploadedImagePath = getIntent().getStringExtra("uploadedImagePath");
        Log.d("ImagePath", "Received Image Path: " + uploadedImagePath);

        // Initialize views
        tvStory = findViewById(R.id.tvStory);
        radioGroup = findViewById(R.id.radioGroup);
        btnNext = findViewById(R.id.btnNext);

        // Set the image and story text
        tvStory.setText("Welcome to our village! We are sick people, and we need your blood, but don't worry, we won't take it from you because we're not bad.\n\n" +
                "We will play a game like a quiz. Let me explain it to you first.\n\n" +
                "You have a blood score of 500. We will give you 7 questions. Each question has 3 options, 2 correct and 1 false.\n\n" +
                "If you choose the false one, your blood points will decrease by 50. In the end, if you have more than 300 blood points, you will still be alive!\n\n" +
                "Also, we are secretly taking a picture of you during each question to make sure it's really you answering — no cheating allowed!\n\n" +
                "And we are still watching...\n\n" +
                "Are you ready?");


        // Set radio button texts
        RadioButton radioAccept = findViewById(R.id.radioAccept);
        RadioButton radioGo = findViewById(R.id.radioGo);

        radioAccept.setText("Yes, I accept");
        radioGo.setText("No, I will go. I don't have time for this");

        // Make the RadioGroup and button visible
        radioGroup.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);

        // Button click listener
        btnNext.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                tvStory.setText("Please select an option to continue.");
            } else {
                RadioButton selectedRadio = findViewById(selectedId);
                String selectedOption = selectedRadio.getText().toString();

                if (selectedOption.equals("Yes, I accept")) {
                    // Start the quiz activity (add your quiz activity here)
                    Intent intent = new Intent(VillageActivity.this, QuizActivity.class);
                    intent.putExtra("bloodScore", bloodScore);
                      // Pass the uploaded image name
                    intent.putExtra("uploadedImagePath", uploadedImagePath); ;  // Pass the uploaded face path
                    startActivity(intent);
                    finish();  // Optional: finish the current activity to remove it from the back stack
                } else if (selectedOption.equals("No, I will go. I don't have time for this")) {
                    // Start AccidentActivity when the user chooses "No, I will go"
                    Intent intent = new Intent(VillageActivity.this, AccidentActivity.class);
                    startActivity(intent);
                    finish();  // Close the current VillageActivity
                }
            }
        });
    }
}

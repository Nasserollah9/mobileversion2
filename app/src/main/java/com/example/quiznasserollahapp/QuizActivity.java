package com.example.quiznasserollahapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class QuizActivity extends AppCompatActivity {

    private ProgressBar progressBlood;
    private TextView tvQuiz, tvBloodScore;
    private Button btnNext;
    private RadioGroup radioGroup;
    private RadioButton radioMother, radioWife, radioNotSay;
    private int bloodScore;
    private int currentQuestion = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Initialize views
        tvQuiz = findViewById(R.id.tvQuiz);
        tvBloodScore = findViewById(R.id.tvBloodScore);
        btnNext = findViewById(R.id.btnNext);
        radioGroup = findViewById(R.id.radioGroup);
        radioMother = findViewById(R.id.radioMother);
        radioWife = findViewById(R.id.radioWife);
        radioNotSay = findViewById(R.id.radioNotSay);
        progressBlood = findViewById(R.id.progressBlood);

        // Get the blood score from the intent
        bloodScore = getIntent().getIntExtra("bloodScore", 500);
        tvBloodScore.setText("Blood Score: " + bloodScore);
        progressBlood.setMax(500);
        progressBlood.setProgress(bloodScore);

        // Display the first question
        showQuestion();

        // Button click to move to the next question
        btnNext.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                tvQuiz.setText("Please select an option to continue.");
            } else {
                RadioButton selectedRadio = findViewById(selectedId);
                String selectedOption = selectedRadio.getText().toString();

                // Handle response and deduct blood points if the answer is false
                if (currentQuestion == 1 && selectedOption.equals("Wife")) {
                    bloodScore -= 50;
                } else if (currentQuestion == 2 && selectedOption.equals("Take the wallet to the police.")) {
                    bloodScore -= 50;
                } else if (currentQuestion == 3 && selectedOption.equals("Ask for help from a local or embassy.")) {
                    bloodScore -= 50;
                } else if (currentQuestion == 4 && selectedOption.equals("Run to the nearest building for shelter.")) {
                    bloodScore -= 50;
                } else if (currentQuestion == 5 && selectedOption.equals("Remain calm, press the emergency button, and wait for help.")) {
                    bloodScore -= 50;
                } else if (currentQuestion == 6 && selectedOption.equals("Alert the authorities and evacuate the area calmly.")) {
                    bloodScore -= 50;
                } else if (currentQuestion == 7 && selectedOption.equals("An elderly person")) {
                    bloodScore -= 50;
                }

                // Update blood score and progress bar display
                tvBloodScore.setText("Blood Score: " + bloodScore);
                progressBlood.setProgress(bloodScore);

                // Check if we are at the last question
                if (currentQuestion == 7) {
                    Intent intent;
                    if (bloodScore < 300) {
                        intent = new Intent(QuizActivity.this, DefeatActivity.class);
                    } else if (bloodScore == 500) {
                        intent = new Intent(QuizActivity.this, KingOfVillageActivity.class);
                    } else {
                        intent = new Intent(QuizActivity.this, YouAreAliveActivity.class);
                    }
                    intent.putExtra("finalBloodScore", bloodScore);
                    startActivity(intent);
                    finish(); // Close current activity
                } else {
                    // Move to the next question
                    currentQuestion++;
                    showQuestion();
                }
            }
        });
    }

    // Method to display questions based on the current question number
    private void showQuestion() {
        switch (currentQuestion) {
            case 1:
                tvQuiz.setText("Question 1: If you're in a hard situation, would you save one person? Your mother or your wife?");
                radioMother.setText("Mother");
                radioWife.setText("Wife");
                radioNotSay.setText("I prefer not to say");
                break;
            case 2:
                tvQuiz.setText("Question 2: You find a wallet on the street with a large amount of money. What do you do?");
                radioMother.setText("Take the money and leave the wallet.");
                radioWife.setText("Leave the wallet where you found it");
                radioNotSay.setText("Take the wallet to the police.");
                break;
            case 3:
                tvQuiz.setText("Question 3: You are stuck in a foreign country without money and have limited food and water. How do you survive?");
                radioMother.setText("Ask for help from a local or embassy.");
                radioWife.setText("Try to survive alone without seeking help");
                radioNotSay.setText("Steal food from a local market.");
                break;
            case 4:
                tvQuiz.setText("Question 4: You are being chased by a wild animal. What do you do?");
                radioMother.setText("Run to the nearest building for shelter.");
                radioWife.setText("Stay still and hope it leaves you alone.");
                radioNotSay.setText("Try to scare it away by yelling loudly");
                break;
            case 5:
                tvQuiz.setText("Question 5: You are stuck in an elevator and the power goes out. What do you do?");
                radioMother.setText("Panic and try to force the door open.");
                radioWife.setText("Remain calm, press the emergency button, and wait for help.");
                radioNotSay.setText("Call someone on the phone to tell them where you are.");
                break;
            case 6:
                tvQuiz.setText("Question 6: You are at a crowded event and someone starts a fire. What do you do first?");
                radioMother.setText("Alert the authorities and evacuate the area calmly.");
                radioWife.setText("Try to put out the fire on your own.");
                radioNotSay.setText("Panic and run in any direction to escape.");
                break;
            case 7:
                tvQuiz.setText("Question 7: You are on a sinking boat. The life raft is small, and there is only room for one more person. Who do you choose to save?");
                radioMother.setText("A child");
                radioWife.setText("An elderly person");
                radioNotSay.setText("A stranger you don’t know");
                break;
        }
        // Clear previous selection
        radioGroup.clearCheck();
    }
}
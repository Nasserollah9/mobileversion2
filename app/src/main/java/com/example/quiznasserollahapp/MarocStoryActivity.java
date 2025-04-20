package com.example.quiznasserollahapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class MarocStoryActivity extends AppCompatActivity {

    private TextView tvStory, tvUserSpeech;
    private Button btnNext;
    private RadioGroup radioGroup;
    private RadioButton radioName, radioAnonymous, radioYes;
    private int step = 1;
    private String userName = "Anonymous";
    private String uploadedImageName = null; // This should be initialized here
    private String fullImageUrl = null;

    private SpeechRecognizer speechRecognizer;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maroc_story);

        // Initialize UI elements
        tvStory = findViewById(R.id.tvStory);
        tvUserSpeech = findViewById(R.id.tvUserSpeech);
        btnNext = findViewById(R.id.btnNext);
        radioGroup = findViewById(R.id.radioGroup);
        radioName = findViewById(R.id.radioName);
        radioAnonymous = findViewById(R.id.radioAnonymous);

        // Get the uploaded image name safely from the intent
        uploadedImageName = getIntent().getStringExtra("uploadedImageName");
       // Log the image path

        String imagePath = getFilesDir().getAbsolutePath() + File.separator + uploadedImageName;
        Log.d("ImagePath", "Uploaded Image Path: " + imagePath);


        // Debugging log to check the image name
        if (uploadedImageName == null || uploadedImageName.isEmpty()) {
            tvStory.setText("No image found.");
        } else {
            fullImageUrl = "https://nzfiiozondmzvdqxdrld.supabase.co/storage/v1/object/public/images/photos/" + uploadedImageName;
            // Log the URL to check
            Log.d("UploadedImageURL", fullImageUrl);
        }

        // First step UI
        tvStory.setText("She looks at you and says: \"What’s your name?\"");
        radioGroup.setVisibility(View.VISIBLE);
        radioName.setText(userName);
        radioAnonymous.setText("Prefer not to say");

        btnNext.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId == -1) {
                tvStory.setText("Please choose one to continue.");
                return;
            }

            if (step == 1) {
                RadioButton selectedRadio = findViewById(selectedId);
                String chosenName = selectedRadio.getText().toString();

                tvStory.setText("She says: \"My village has sick people who need help. Will you come with me?\"");

                radioGroup.removeAllViews();
                radioYes = new RadioButton(this);
                radioYes.setId(View.generateViewId());
                radioYes.setText("Yes");
                radioYes.setTextColor(getResources().getColor(R.color.old_paper));
                radioYes.setButtonTintList(getResources().getColorStateList(R.color.old_paper));
                radioGroup.addView(radioYes);

                step = 2;

                checkAudioPermissionAndStartListening();
            } else if (step == 2) {
                startNextActivity();
            }
        });
    }

    private void startNextActivity() {
        // Make sure to pass the image URL to the next activity
        Intent intent = new Intent(MarocStoryActivity.this, VillageActivity.class);
        String imagePath = getFilesDir().getAbsolutePath() + File.separator + uploadedImageName;
        intent.putExtra("uploadedImagePath", imagePath); // Pass the image path to the next activity
        startActivity(intent);
        finish();
    }

    private void checkAudioPermissionAndStartListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            startVoiceRecognition();
        }
    }

    private void startVoiceRecognition() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    tvStory.setText("Didn’t catch that. Please say 'Yes'.");
                    new Handler().postDelayed(() -> startVoiceRecognition(), 3000);
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String userSpeech = matches.get(0);
                    tvUserSpeech.setText("You said: " + userSpeech);
                    checkIfUserSaidYes(userSpeech);
                }
            }

            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        new Handler().postDelayed(() -> speechRecognizer.startListening(intent), 800);
    }

    private void checkIfUserSaidYes(String userInput) {
        String normalized = userInput.trim().toLowerCase();
        if (normalized.contains("yes") || normalized.contains("yeah") || normalized.contains("sure") || normalized.contains("okay")) {
            tvUserSpeech.setText("You said: " + userInput + "\nInterpreted as YES.");
            radioYes.setChecked(true);
            step = 2;
            startNextActivity();
        } else {
            tvStory.setText("Didn't hear a clear 'Yes'. Please confirm by saying 'Yes'.");
            new Handler().postDelayed(this::startVoiceRecognition, 3000);
        }
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
}

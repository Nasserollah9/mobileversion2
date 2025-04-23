package com.example.quiznasserollahapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class storybegin extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int IMAGE_CAPTURE_CODE = 1000;

    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private TextView tvLocation;
    private Button btnBeginStory, btnUploadImage;
    private CascadeClassifier faceDetector;

    private String detectedCountry = "";
    private boolean isImageUploaded = false;
    private File currentPhotoFile;

    private String uploadedImageName = ""; // Store uploaded file name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storybegin);

        tvLocation = findViewById(R.id.tvLocation);
        btnBeginStory = findViewById(R.id.btnBeginStory);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnBeginStory.setEnabled(false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnUploadImage.setOnClickListener(v -> openImagePicker());
        loadCascadeClassifier();
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Get location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        if (mMap != null) {
                            LatLng userLocation = new LatLng(latitude, longitude);
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions().position(userLocation).title("Vous êtes ici"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10f));
                        }

                        Geocoder geocoder = new Geocoder(storybegin.this, Locale.getDefault());
                        try {
                            List<android.location.Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                detectedCountry = addresses.get(0).getCountryName();
                                tvLocation.setText("Pays détecté : " + detectedCountry);
                                Log.d("CountryName", "Pays détecté : " + detectedCountry);

                                btnBeginStory.setOnClickListener(v1 -> {
                                    if (isImageUploaded) {
                                        startStoryBasedOnLocation(detectedCountry);
                                    } else {
                                        Toast.makeText(storybegin.this, "Veuillez d'abord téléverser une image.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (IOException e) {
                            tvLocation.setText("Impossible de détecter le pays.");
                            e.printStackTrace();
                        }
                    } else {
                        tvLocation.setText("Emplacement introuvable.");
                    }
                });
    }

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "OpenCV initialization failed");
        } else {
            Log.d("OpenCV", "OpenCV initialized");
        }
    }

    private void openImagePicker() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    IMAGE_CAPTURE_CODE);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                try {
                    currentPhotoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Erreur lors de la création du fichier", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (currentPhotoFile != null) {
                    Uri photoUri = FileProvider.getUriForFile(this,
                            "com.example.quiznasserollahapp.fileprovider",
                            currentPhotoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(intent, IMAGE_CAPTURE_CODE);
                }
            } else {
                Toast.makeText(this, "La caméra n'est pas disponible", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadCascadeClassifier() {
        try {
            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");

            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            faceDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (faceDetector.empty()) {
                faceDetector = null;
                Log.e("OpenCV", "Failed to load cascade classifier");
            } else {
                Log.i("OpenCV", "Cascade classifier loaded");
            }
            Toast.makeText(this, "Cascade chargé avec succès", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("OpenCV", "Error loading cascade", e);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }
        return new File(storageDir, imageFileName);
    }

    // Inside onActivityResult, update this part
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            if (currentPhotoFile != null && currentPhotoFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoFile.getAbsolutePath());

                // Correct orientation if needed
                bitmap = correctOrientation(bitmap);

                // Use the updated detectFace method (returns true if a face is detected)
                boolean faceDetected = detectFace(bitmap);

                if (faceDetected) {
                    Toast.makeText(this, "Visage détecté avec succès.", Toast.LENGTH_SHORT).show();
                    btnBeginStory.setEnabled(false); // Optional: wait for upload success to enable
                    uploadImageToSupabase(currentPhotoFile);
                } else {
                    Toast.makeText(this, "Aucun visage détecté. Veuillez réessayer.", Toast.LENGTH_LONG).show();
                    btnBeginStory.setEnabled(false); // Just in case
                    if (currentPhotoFile.exists()) {
                        currentPhotoFile.delete(); // Clean up bad images
                    }
                }
            } else {
                Log.e("Camera", "Image file does not exist or is null");
            }
        }
    }



    private Bitmap correctOrientation(Bitmap bitmap) {
        try {
            // Get the image file path
            String imagePath = currentPhotoFile.getAbsolutePath();

            // Create an ExifInterface object to read EXIF metadata
            ExifInterface exif = new ExifInterface(imagePath);

            // Get the orientation value from EXIF metadata
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            // Rotate the bitmap based on the EXIF orientation value
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.postScale(1, -1);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    break;
            }

            // Apply the rotation matrix to the bitmap
            Bitmap correctedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return correctedBitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap; // Return the original bitmap if an error occurs
    }
    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float ratio = (float) width / height;
        if (width > height) {
            width = maxSize;
            height = (int) (width / ratio);
        } else {
            height = maxSize;
            width = (int) (height * ratio);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private boolean detectFace(Bitmap bitmap) {
        if (faceDetector == null) {
            Log.e("OpenCV", "FaceDetector not loaded");
            return false;
        }

        // Convert Bitmap to Mat
        Mat rgba = new Mat();
        Utils.bitmapToMat(bitmap, rgba);

        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY);

        // Enhance contrast
        Imgproc.equalizeHist(gray, gray);

        // Reduce noise
        Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 0);

        // Detect faces
        MatOfRect faces = new MatOfRect();
        faceDetector.detectMultiScale(
                gray,
                faces,
                1.1, // Conservative scaling
                5,   // Require more neighbors to validate a face
                Objdetect.CASCADE_SCALE_IMAGE,
                new Size(100, 100), // Only detect reasonably sized faces
                new Size()          // No max size
        );

        // Optional: draw rectangles around detected faces
        for (Rect face : faces.toArray()) {
            Imgproc.rectangle(rgba, face.tl(), face.br(), new Scalar(0, 255, 0, 255), 2);
        }

        // ✅ Return true only if at least one real face is found
        return faces.toArray().length > 0;
    }



    private void uploadImageToSupabase(File imageFile) {
        OkHttpClient client = new OkHttpClient();

        String supabaseUrl = "https://nzfiiozondmzvdqxdrld.supabase.co";
        String bucketName = "images";
        String apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im56Zmlpb3pvbmRtenZkcXhkcmxkIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0NTE0MjIxNywiZXhwIjoyMDYwNzE4MjE3fQ.2d0GZGpYADZ985xNUpxWh3i7pHHun1WxkXn4QhX4g9A"; // Replace with your actual API key
        String objectPath = "photos/" + imageFile.getName();

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
                runOnUiThread(() -> Toast.makeText(storybegin.this, "Erreur de téléversement", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("SUPABASE_UPLOAD", "Response Code: " + response.code() + " Body: " + responseBody);

                if (response.isSuccessful()) {
                    isImageUploaded = true;
                    uploadedImageName = imageFile.getName(); // Save uploaded file name
                    runOnUiThread(() -> {
                        Toast.makeText(storybegin.this, "Image téléversée avec succès", Toast.LENGTH_SHORT).show();
                        btnBeginStory.setEnabled(true);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(storybegin.this, "Erreur: " + responseBody, Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng defaultLocation = new LatLng(33.5731, -7.5898); // Casablanca
        mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Casablanca"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 8f));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == IMAGE_CAPTURE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // If permissions granted, open the camera
                openImagePicker();
            } else {
                Toast.makeText(this, "Permissions refusées", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startStoryBasedOnLocation(String countryName) {
        String cleanedCountry = countryName.trim().toLowerCase();
        Intent intent;

        if (cleanedCountry.contains("morocco") || cleanedCountry.contains("maroc")) {
            intent = new Intent(this, MarocStoryActivity.class);
        } else if (cleanedCountry.contains("united kingdom") || cleanedCountry.contains("uk")) {
            intent = new Intent(this, UkStoryActivity.class);
        } else {
            intent = new Intent(this, DefaultStoryActivity.class);
        }

        intent.putExtra("uploadedImageName", uploadedImageName); // Optional if needed
        startActivity(intent);
    }
}
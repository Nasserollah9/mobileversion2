package com.example.quiznasserollahapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
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

import java.io.File;
import java.io.IOException;
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


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }
        return new File(storageDir, imageFileName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            if (currentPhotoFile != null && currentPhotoFile.exists()) {
                uploadImageToSupabase(currentPhotoFile);
            } else {
                Log.e("Camera", "Image file does not exist or is null");
            }
        }
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

        intent.putExtra("uploadedImageName", uploadedImageName); // Pass to next activity
        startActivity(intent);
    }
}

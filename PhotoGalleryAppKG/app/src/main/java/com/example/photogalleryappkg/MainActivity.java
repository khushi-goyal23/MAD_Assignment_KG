package com.example.photogalleryappkg;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button btnTakePhoto, btnSelectFolder;
    private File photoFile;

    // ActivityResultLauncher for requesting permissions
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean readImagesGranted = result.getOrDefault(Manifest.permission.READ_MEDIA_IMAGES, false);
                Boolean cameraGranted = result.getOrDefault(Manifest.permission.CAMERA, false);

                if (readImagesGranted != null && readImagesGranted && cameraGranted != null && cameraGranted) {
                    Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied. App may not function properly.", Toast.LENGTH_LONG).show();
                }
            });

    // ActivityResultLauncher to handle the result of the camera intent
    private final ActivityResultLauncher<Intent> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, "Photo captured successfully", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestPermissions();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar); // same ID as in XML
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true); // 🔥 Make sure title is shown
        getSupportActionBar().setTitle("Photo Gallery App");

        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSelectFolder = findViewById(R.id.btnSelectFolder);

        // Request permissions based on Android version
        requestPermissions();

        btnTakePhoto.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                try {
                    photoFile = createImageFile();
                    Uri photoURI = FileProvider.getUriForFile(
                            this,
                            getApplicationContext().getPackageName() + ".provider",
                            photoFile);

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    takePhotoLauncher.launch(takePictureIntent);
                } catch (IOException ex) {
                    Log.e("MainActivity", "Error creating image file", ex);
                    Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSelectFolder.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, GalleryActivity.class)));

        // Set padding for system bars on modern devices
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Request permissions depending on Android version
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA
            });
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            });
        }
    }

    // Method to create an image file to save the photo
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    // Handle result of permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            Toast.makeText(this,
                    (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                            ? "Permission granted to read images!"
                            : "Permission denied. App may not show images correctly.",
                    Toast.LENGTH_LONG).show();
        }
    }
}



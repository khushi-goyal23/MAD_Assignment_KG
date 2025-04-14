package com.example.photogalleryappkg;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDetailsActivity extends AppCompatActivity {

    ImageView imageView;
    TextView detailsText;
    Button deleteButton;
    String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        imageView = findViewById(R.id.imageView);
        detailsText = findViewById(R.id.detailsText);
        deleteButton = findViewById(R.id.deleteButton);

        imagePath = getIntent().getStringExtra("imagePath");

        if (imagePath == null) {
            Toast.makeText(this, "Image path is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        File file = new File(imagePath);
        if (!file.exists()) {
            Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        imageView.setImageBitmap(bitmap);

        String info = "Name: " + file.getName() +
                "\nPath: " + file.getAbsolutePath() +
                "\nSize: " + file.length() + " bytes" +
                "\nDate: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(file.lastModified()));
        detailsText.setText(info);

        deleteButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (file.delete()) {
                        Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to Gallery
                    } else {
                        Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show());
    }
}

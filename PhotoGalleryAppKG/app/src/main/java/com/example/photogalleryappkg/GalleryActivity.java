package com.example.photogalleryappkg;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;

public class GalleryActivity extends AppCompatActivity {

    GridView gridView;
    File[] imageFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Toolbar toolbar = findViewById(R.id.galleryToolbar);
        setSupportActionBar(toolbar);

// Show back button on the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        gridView = findViewById(R.id.gridView);

        // Load images from Pictures directory
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        imageFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));

        if (imageFiles != null && imageFiles.length > 0) {
            ImageAdapter adapter = new ImageAdapter(this,imageFiles);
            gridView.setAdapter(adapter);
        }

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(GalleryActivity.this, ImageDetailsActivity.class);
            intent.putExtra("imagePath", imageFiles[position].getAbsolutePath());
            startActivity(intent);
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Navigate back to MainActivity
        return true;
    }

}

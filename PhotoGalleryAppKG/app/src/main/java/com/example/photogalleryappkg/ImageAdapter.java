package com.example.photogalleryappkg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;

public class ImageAdapter extends BaseAdapter {

    private final Context context;  // Marked as final
    private final File[] imageFiles;  // Marked as final

    public ImageAdapter(Context context, File[] imageFiles) {
        this.context = context;
        this.imageFiles = imageFiles;
    }

    @Override
    public int getCount() {
        return imageFiles.length;
    }

    @Override
    public Object getItem(int position) {
        return imageFiles[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        // Use the recycled view if it's available, otherwise create a new one
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new GridView.LayoutParams(300, 300));  // You can adjust the size if needed
        } else {
            imageView = (ImageView) convertView;
        }

        // Load the image from the file
        Bitmap bitmap = BitmapFactory.decodeFile(imageFiles[position].getAbsolutePath());
        imageView.setImageBitmap(bitmap);
        return imageView;
    }
}


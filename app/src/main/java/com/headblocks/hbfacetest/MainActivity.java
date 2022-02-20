package com.headblocks.hbfacetest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.headblocks.hbface.FaceMatcher;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private FaceMatcher faceMatcher = new FaceMatcher();
    private final int SELECT_PICTURE_ONE = 200;
    private final int SELECT_PICTURE_TWO = 201;
    private File imageOne, imageTwo = null;
    private ImageView imageOneView, imageTwoView;
    private Button compareButton;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageOneView = findViewById(R.id.imageOneView);
        imageTwoView = findViewById(R.id.imageTwoView);
        compareButton = findViewById(R.id.compareButton);
        resultTextView = findViewById(R.id.resultTextView);

        imageOneView.setOnClickListener(v -> openImageChooserForOne());

        imageTwoView.setOnClickListener(v -> openImageChooserForTwo());

        compareButton.setOnClickListener(v -> {
            if (imageOne != null && imageTwo != null) {
                Toast.makeText(getApplicationContext(), "Processing...", Toast.LENGTH_LONG).show();
                JSONObject jsonObject = faceMatcher.matchFaceOneToOne(imageOne, imageTwo);
//                    Log.d("result", String.valueOf());
                resultTextView.setText(jsonObject.toString());
            } else {
                Toast.makeText(getApplicationContext(), "Please select two images", Toast.LENGTH_LONG).show();
            }
        });


    }

    private void openImageChooserForOne() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE_ONE);
    }

    private void openImageChooserForTwo() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE_TWO);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private File saveBitmap(Bitmap bmp, String imageName) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("img", Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }
        File mypath = new File(directory, imageName);

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(mypath);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) {
            Log.d("SAVE_IMAGE", e.getMessage(), e);
        }
        return  mypath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE_ONE && resultCode == RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                imageOne =  new File(uri.getPath());
                try {
                    Bitmap bitmap = getBitmapFromUri(uri);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    imageOneView.setImageBitmap(rotatedBitmap);
                    imageOne =  saveBitmap(rotatedBitmap, "imageOne.jpg");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == SELECT_PICTURE_TWO && resultCode == RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
//                imageTwo =  new File(uri.getPath());
                try {
                    Bitmap bitmap = getBitmapFromUri(uri);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    imageTwoView.setImageBitmap(rotatedBitmap);
                    imageTwo =  saveBitmap(rotatedBitmap, "imageTwo.jpg");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
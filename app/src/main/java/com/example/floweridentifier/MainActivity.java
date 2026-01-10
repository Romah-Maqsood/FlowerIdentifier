package com.example.floweridentifier;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI Elements
    private ImageView imagePreview;
    private Button btnCamera, btnGallery, btnIdentify;
    private TextView tvResult, tvConfidence;
    private ProgressBar progressBar, loadingBar;
    private CardView resultsCard;

    private Bitmap selectedBitmap;
    private Interpreter tflite;
    private List<String> labelList;

    // Model configuration
    private static final int INPUT_SIZE = 224;
    private static final int CHANNEL_SIZE = 3;
    private static final int BATCH_SIZE = 1;
    private static final float CONFIDENCE_THRESHOLD = 0.6f;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    selectedBitmap = (Bitmap) extras.get("data");
                    imagePreview.setImageBitmap(selectedBitmap);
                    btnIdentify.setEnabled(true);
                    resultsCard.setVisibility(View.GONE);
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        imagePreview.setImageBitmap(selectedBitmap);
                        btnIdentify.setEnabled(true);
                        resultsCard.setVisibility(View.GONE);
                    } catch (IOException e) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
        } catch (Exception e) {
            Log.e("MainActivity", "Error setting content view", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish(); // Close the activity if it fails to load
            return;
        }

        initUI();
        loadModel();
        loadLabels();
    }

    private void initUI() {
        imagePreview = findViewById(R.id.imagePreview);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnIdentify = findViewById(R.id.btnIdentify);
        tvResult = findViewById(R.id.tvResult);
        tvConfidence = findViewById(R.id.tvConfidence);
        progressBar = findViewById(R.id.progressBar);
        loadingBar = findViewById(R.id.loadingBar);
        resultsCard = findViewById(R.id.resultsCard);

        btnCamera.setOnClickListener(v -> checkCameraPermission());
        btnGallery.setOnClickListener(v -> checkGalleryPermission());
        btnIdentify.setOnClickListener(v -> identifyFlower());
    }

    private void loadModel() {
        try {
            tflite = new Interpreter(FileUtil.loadMappedFile(this, "flowernet_model.tflite"));
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load model: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void loadLabels() {
        try {
            InputStream is = getAssets().open("flowernet_labels.txt");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String labels = new String(buffer);
            labelList = new ArrayList<>();
            for (String label : labels.split("\n")) {
                labelList.add(label.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            labelList = new ArrayList<>();
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        } else {
            openCamera();
        }
    }

    private void checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestGalleryPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            openGallery();
        }
    }

    private final ActivityResultLauncher<String> requestCameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> requestGalleryPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        galleryLauncher.launch(galleryIntent);
    }

    private void identifyFlower() {
        if (selectedBitmap == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tflite == null || labelList == null || labelList.isEmpty()) {
            Toast.makeText(this, "Model or labels not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingBar.setVisibility(View.VISIBLE);
        btnIdentify.setEnabled(false);

        new Thread(() -> {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(selectedBitmap, INPUT_SIZE, INPUT_SIZE, true);
            float[][][][] input = preprocessBitmap(resizedBitmap);
            float[][] output = new float[BATCH_SIZE][labelList.size()];

            try {
                tflite.run(input, output);
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Error during inference: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    loadingBar.setVisibility(View.GONE);
                    btnIdentify.setEnabled(true);
                });
                return;
            }

            int maxIndex = -1;
            float maxConfidence = 0;

            for (int i = 0; i < output[0].length; i++) {
                if (output[0][i] > maxConfidence) {
                    maxConfidence = output[0][i];
                    maxIndex = i;
                }
            }

            final String flowerName;
            final float confidence;

            if (maxIndex != -1 && maxConfidence >= CONFIDENCE_THRESHOLD) {
                flowerName = labelList.get(maxIndex);
                confidence = maxConfidence;
            } else {
                flowerName = "No flower detected";
                confidence = maxConfidence;
            }

            runOnUiThread(() -> {
                loadingBar.setVisibility(View.GONE);
                btnIdentify.setEnabled(true);
                resultsCard.setVisibility(View.VISIBLE);

                tvResult.setText(flowerName);
                int confidencePercent = (int) (confidence * 100);
                tvConfidence.setText("Confidence: " + confidencePercent + "%");
                progressBar.setProgress(confidencePercent);

                if (confidence < CONFIDENCE_THRESHOLD) {
                    tvResult.setTextColor(ContextCompat.getColor(this, R.color.red_dark));
                    Toast.makeText(MainActivity.this,
                            "Low confidence - may not be a flower",
                            Toast.LENGTH_LONG).show();
                } else {
                    tvResult.setTextColor(ContextCompat.getColor(this, R.color.purple_700));
                    Toast.makeText(MainActivity.this,
                            "Flower identified successfully!",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private float[][][][] preprocessBitmap(Bitmap bitmap) {
        float[][][][] input = new float[BATCH_SIZE][INPUT_SIZE][INPUT_SIZE][CHANNEL_SIZE];
        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);

        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < INPUT_SIZE; j++) {
                int pixel = pixels[i * INPUT_SIZE + j];
                input[0][i][j][0] = ((pixel >> 16) & 0xFF) / 255.0f; // R
                input[0][i][j][1] = ((pixel >> 8) & 0xFF) / 255.0f;  // G
                input[0][i][j][2] = (pixel & 0xFF) / 255.0f;         // B
            }
        }

        return input;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) {
            tflite.close();
        }
    }
}

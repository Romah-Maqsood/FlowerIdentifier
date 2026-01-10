package com.example.floweridentifier.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.floweridentifier.R;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    private ImageView imagePreview;
    private Button btnCamera, btnGallery, btnIdentify;
    private TextView tvResult, tvConfidence, tvDescription;
    private ProgressBar progressBar, loadingBar;
    private CardView resultsCard;

    private Bitmap selectedBitmap;
    private Interpreter tflite;
    private List<String> labelList;
    private Map<String, String> flowerDescriptions;

    private static final int INPUT_SIZE = 224;
    private static final int CHANNEL_SIZE = 3;
    private static final int BATCH_SIZE = 1;
    private static final float CONFIDENCE_THRESHOLD = 0.6f;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        selectedBitmap = (Bitmap) extras.get("data");
                        imagePreview.setImageBitmap(selectedBitmap);
                        btnIdentify.setEnabled(true);
                        resultsCard.setVisibility(View.GONE);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                        imagePreview.setImageBitmap(selectedBitmap);
                        btnIdentify.setEnabled(true);
                        resultsCard.setVisibility(View.GONE);
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> requestGalleryPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    Toast.makeText(getContext(), "Gallery permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI(view);
        loadModel();
        loadLabels();
        loadDescriptions();
    }

    private void initUI(View view) {
        imagePreview = view.findViewById(R.id.imagePreview);
        btnCamera = view.findViewById(R.id.btnCamera);
        btnGallery = view.findViewById(R.id.btnGallery);
        btnIdentify = view.findViewById(R.id.btnIdentify);
        tvResult = view.findViewById(R.id.tvResult);
        tvConfidence = view.findViewById(R.id.tvConfidence);
        tvDescription = view.findViewById(R.id.tvDescription);
        progressBar = view.findViewById(R.id.progressBar);
        loadingBar = view.findViewById(R.id.loadingBar);
        resultsCard = view.findViewById(R.id.resultsCard);

        btnCamera.setOnClickListener(v -> checkCameraPermission());
        btnGallery.setOnClickListener(v -> checkGalleryPermission());
        btnIdentify.setOnClickListener(v -> identifyFlower());
    }

    private void loadModel() {
        try {
            tflite = new Interpreter(FileUtil.loadMappedFile(requireActivity(), "flowernet_model.tflite"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to load model", Toast.LENGTH_LONG).show();
        }
    }

    private void loadLabels() {
        try (InputStream is = requireActivity().getAssets().open("flowernet_labels.txt")) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            String labels = new String(buffer);
            labelList = new ArrayList<>(List.of(labels.split("\n")));
        } catch (IOException e) {
            labelList = new ArrayList<>();
        }
    }

    private void loadDescriptions() {
        flowerDescriptions = new HashMap<>();
        flowerDescriptions.put("Rose", "A woody perennial flowering plant of the genus Rosa, in the family Rosaceae.");
        flowerDescriptions.put("Daisy", "A common European composite flower with white rays and a yellow disk.");
        flowerDescriptions.put("Dandelion", "A widely distributed weed of the daisy family, with a rosette of leaves and large bright yellow flowers.");
        flowerDescriptions.put("Tulip", "A bulbous spring-flowering plant of the lily family, with boldly colored cup-shaped flowers.");
        flowerDescriptions.put("Sunflower", "A tall North American plant of the daisy family, with very large golden-rayed flowers.");
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    private void checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestGalleryPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void identifyFlower() {
        if (selectedBitmap == null) {
            Toast.makeText(getContext(), "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tflite == null || labelList == null || labelList.isEmpty()) {
            Toast.makeText(getContext(), "Model or labels not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingBar.setVisibility(View.VISIBLE);
        btnIdentify.setEnabled(false);

        new Thread(() -> {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(selectedBitmap, INPUT_SIZE, INPUT_SIZE, true);
            float[][][][] input = preprocessBitmap(resizedBitmap);
            float[][] output = new float[BATCH_SIZE][labelList.size()];
            tflite.run(input, output);

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
                flowerName = labelList.get(maxIndex).trim();
                confidence = maxConfidence;
            } else {
                flowerName = "No flower detected";
                confidence = maxConfidence;
            }

            final String description = flowerDescriptions.getOrDefault(flowerName, "No description available.");

            requireActivity().runOnUiThread(() -> {
                loadingBar.setVisibility(View.GONE);
                btnIdentify.setEnabled(true);
                resultsCard.setVisibility(View.VISIBLE);

                tvResult.setText(flowerName);
                int confidencePercent = (int) (confidence * 100);
                tvConfidence.setText(String.format("Confidence: %d%%", confidencePercent));
                tvDescription.setText(description);
                progressBar.setProgress(confidencePercent);
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
                input[0][i][j][0] = ((pixel >> 16) & 0xFF) / 255.0f;
                input[0][i][j][1] = ((pixel >> 8) & 0xFF) / 255.0f;
                input[0][i][j][2] = (pixel & 0xFF) / 255.0f;
            }
        }
        return input;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tflite != null) {
            tflite.close();
        }
    }
}

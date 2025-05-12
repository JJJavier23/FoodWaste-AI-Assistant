package com.example.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_CODE = 100;

    private ImageView imageView;
    private Uri photoUri;
    private LinearLayout mainContent;
    private View splashScreen;
    private ImageClassifier classifier;
    private Button captureButton;
    private Button selectButton;
    private TextView modelInfoText;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    processSelectedImage(uri);
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    processCapturedImage();
                } else {
                    Log.d(TAG, "Camera activity result not OK: " + result.getResultCode());
                    Toast.makeText(MainActivity.this, "Failed to capture image",
                            Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainContent = findViewById(R.id.mainContent);
        splashScreen = findViewById(R.id.splashScreen);
        imageView = findViewById(R.id.imageView);
        captureButton = findViewById(R.id.captureButton);
        selectButton = findViewById(R.id.selectButton);
        modelInfoText = findViewById(R.id.modelInfoText);

        modelInfoText.setText("Combined Fruit-Ripeness Model");

        captureButton.setEnabled(false);
        selectButton.setEnabled(false);

        setupSplashScreen();
        requestRequiredPermissions();
        new Handler(Looper.getMainLooper()).postDelayed(this::initializeClassifier, 2000);

        setupCaptureButton();
        setupSelectButton();
        listAssetFiles();
    }

    private void requestRequiredPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION_CODE);
        }
    }

    private void listAssetFiles() {
        try {
            String[] files = getAssets().list("");
            Log.d(TAG, "Assets directory contents: " + Arrays.toString(files));
        } catch (IOException e) {
            Log.e(TAG, "Error listing asset files", e);
        }
    }

    private void setupSelectButton() {
        selectButton.setOnClickListener(v -> {
            if (!classifier.isInitialized()) {
                Toast.makeText(this, "Classifier not initialized", Toast.LENGTH_SHORT).show();
                return;
            }
            galleryLauncher.launch("image/*");
        });
    }

    private void processSelectedImage(Uri uri) {
        try {
            Log.d(TAG, "Processing selected image: " + uri);
            imageView.setImageURI(uri);

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            if (bitmap == null) {
                throw new IOException("Failed to load bitmap from URI");
            }

            Log.d(TAG, "Bitmap loaded successfully. Size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            classifyAndShowResults(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error processing selected image: ", e);
            Toast.makeText(this,
                    "Error processing image: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setupSplashScreen() {
        mainContent.setVisibility(View.GONE);
        splashScreen.setVisibility(View.VISIBLE);

        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(1000);
        fadeOut.setFillAfter(true);

        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            splashScreen.startAnimation(fadeOut);
            mainContent.setVisibility(View.VISIBLE);
            mainContent.startAnimation(fadeIn);
            splashScreen.setVisibility(View.GONE);
        }, 2000);
    }

    private void initializeClassifier() {
        try {
            Log.d(TAG, "Initializing combined classifier");

            if (classifier != null) {
                classifier.close();
            }

            AssetManager assetManager = getAssets();
            classifier = new ImageClassifier(assetManager);

            if (classifier.isInitialized()) {
                Log.d(TAG, "Combined classifier initialized successfully");
                classifier.logModelDetails();

                runOnUiThread(() -> {
                    captureButton.setEnabled(true);
                    selectButton.setEnabled(true);
                    Toast.makeText(this, "Combined fruit-ripeness model loaded successfully", Toast.LENGTH_SHORT).show();
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize classifier: " + e.getMessage(), e);
            runOnUiThread(() -> {
                Toast.makeText(this, "Failed to load model: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                captureButton.setEnabled(false);
                selectButton.setEnabled(false);
            });
        }
    }

    private void setupCaptureButton() {
        captureButton.setOnClickListener(v -> {
            if (!classifier.isInitialized()) {
                Toast.makeText(this, "Classifier not initialized", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Directions")
                    .setMessage("When taking an image of your fruit, please make sure the background is black or white and no other items are in the image.")
                    .setPositiveButton("OK", (dialogInterface, which) -> {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.CAMERA},
                                    CAMERA_PERMISSION_CODE);
                        } else {
                            takePicture();
                        }
                    })
                    .create();

            dialog.setOnShowListener(dialogInterface -> {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.white, getTheme()));
                positiveButton.setBackgroundColor(getResources().getColor(R.color.purple_500, getTheme()));
                positiveButton.setPadding(50, 0, 50, 0);
            });

            dialog.show();
        });
    }

    private void processCapturedImage() {
        try {
            Log.d(TAG, "Processing captured image");
            Bitmap bitmap = null;

            // Check if we have a URI from FileProvider
            if (photoUri != null) {
                try {
                    Log.d(TAG, "Processing from URI: " + photoUri);
                    imageView.setImageURI(null); // Clear previous image
                    imageView.setImageURI(photoUri);
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading bitmap from URI", e);
                }
            }

            // If URI approach failed, check if we have data from the activity result
            if (bitmap == null) {
                try {
                    // This would work when using the thumbnail approach
                    Log.d(TAG, "Trying to get thumbnail from intent data");
                    // In a real cameraLauncher result, you would access this from the result data
                    // For now, we'll just return an error as we can't access that data here
                    throw new IOException("Cannot access thumbnail data in this context");
                } catch (Exception e) {
                    Log.e(TAG, "Error getting thumbnail bitmap", e);
                    Toast.makeText(this, "Failed to load image data", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (bitmap == null) {
                throw new IOException("Failed to load bitmap from captured image");
            }

            Log.d(TAG, "Bitmap loaded successfully. Size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            classifyAndShowResults(bitmap);

        } catch (Exception e) {
            Log.e(TAG, "Error processing captured image: ", e);
            Toast.makeText(this,
                    "Error processing image: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void classifyAndShowResults(Bitmap bitmap) {
        try {
            Log.d(TAG, "Starting classification for bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());

            long startTime = System.currentTimeMillis();
            ImageClassifier.CombinedClassificationResult results = classifier.classifyImage(bitmap);
            long inferenceTime = System.currentTimeMillis() - startTime;

            Log.d(TAG, "Classification completed in " + inferenceTime + "ms");

            String predictedFruit = classifier.getFruitClassName(results.fruitIndex);
            String predictedRipeness = classifier.getRipenessClassName(results.ripenessIndex);
            float fruitConfidence = results.fruitConfidence;
            float ripenessConfidence = results.ripenessConfidence;

            StringBuilder resultText = new StringBuilder("Classification Results:\n\n");
            resultText.append("Fruit Classification:\n");

            for (int i = 0; i < results.fruitProbabilities.length; i++) {
                resultText.append(classifier.getFruitClassName(i))
                        .append(": ")
                        .append(String.format("%.1f%%", results.fruitProbabilities[i] * 100))
                        .append("\n");
            }

            resultText.append("\nRipeness Classification:\n");

            for (int i = 0; i < results.ripenessProbabilities.length; i++) {
                resultText.append(classifier.getRipenessClassName(i))
                        .append(": ")
                        .append(String.format("%.1f%%", results.ripenessProbabilities[i] * 100))
                        .append("\n");
            }

            resultText.append("\nPredicted: ").append(predictedFruit)
                    .append(" ").append(predictedRipeness)
                    .append("\nFruit Confidence: ").append(String.format("%.1f%%", fruitConfidence))
                    .append("\nRipeness Confidence: ").append(String.format("%.1f%%", ripenessConfidence))
                    .append("\n\nInference time: ").append(inferenceTime).append("ms");

            String ripenessAdvice;
            switch (predictedRipeness.toLowerCase()) {
                case "unripe":
                    ripenessAdvice = "⏳ Unripe: You have 4–6 days.";
                    break;
                case "ripe":
                    ripenessAdvice = "🍽️ Ripe: Best within 2–3 days.";
                    break;
                case "veryripe":
                    ripenessAdvice = "⚠️ Very Ripe: Use within 1–2 days.";
                    break;
                case "overripe":
                    ripenessAdvice = "🚨 Overripe: Less than 24 hours — consider throwing it away!";
                    break;
                default:
                    ripenessAdvice = "Ripeness estimate not available.";
                    break;
            }

            resultText.append("\n\n").append(ripenessAdvice);

            new AlertDialog.Builder(this)
                    .setTitle("Classification Results")
                    .setMessage(resultText.toString())
                    .setPositiveButton("OK", null)
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "Classification error: " + e.getMessage(), e);
            Toast.makeText(this,
                    "Classification error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                Log.d(TAG, "All permissions granted");
            } else {
                Log.w(TAG, "Some permissions denied");
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Log.w(TAG, "Permission denied: " + permissions[i]);
                        if (Manifest.permission.CAMERA.equals(permissions[i])) {
                            Toast.makeText(MainActivity.this,
                                    "Camera Permission Denied. Cannot take pictures.",
                                    Toast.LENGTH_LONG).show();
                            captureButton.setEnabled(false);
                        }
                    }
                }
            }
        }
    }

    private void takePicture() {
        try {
            Log.d(TAG, "Taking picture...");

            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Camera permission not granted, requesting...");
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                return;
            }

            // Create the photo file first
            File photoFile = createImageFile();
            if (photoFile == null) {
                Log.e(TAG, "Error creating image file");
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create the URI using FileProvider
            photoUri = FileProvider.getUriForFile(this,
                    "com.example.ui.fileprovider",
                    photoFile);
            Log.d(TAG, "Photo URI created: " + photoUri);

            // Try different camera intents in sequence until one works
            boolean cameraLaunched = false;

            // Try the standard approach first
            try {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                // Check if there's an app that can handle this intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                    cameraLauncher.launch(intent);
                    cameraLaunched = true;
                } else {
                    Log.w(TAG, "Standard camera intent not available");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error launching standard camera intent", e);
            }

            // If standard approach failed, try alternative camera intent
            if (!cameraLaunched) {
                try {
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    cameraLauncher.launch(intent);
                    cameraLaunched = true;
                } catch (Exception e) {
                    Log.e(TAG, "Error launching alternative camera intent", e);
                }
            }

            // If both approaches failed, try without specifying output URI
            if (!cameraLaunched) {
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // No EXTRA_OUTPUT, will just get thumbnail
                    cameraLauncher.launch(intent);
                    cameraLaunched = true;
                    Toast.makeText(this, "Using thumbnail mode (reduced quality)", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "Error launching thumbnail camera intent", e);
                }
            }

            // If all attempts failed, show error
            if (!cameraLaunched) {
                Log.e(TAG, "No camera app available or accessible");
                Toast.makeText(this, "No camera app available on this device. Check system settings.", Toast.LENGTH_LONG).show();

                // Log installed packages that might be able to handle camera
                PackageManager pm = getPackageManager();
                try {
                    Intent dummyIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Log.d(TAG, "Available camera apps: " + pm.queryIntentActivities(dummyIntent, 0).size());
                } catch (Exception e) {
                    Log.e(TAG, "Error querying camera apps", e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error taking picture: ", e);
            Toast.makeText(this, "Error taking picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            if (!storageDir.exists() && !storageDir.mkdirs()) {
                Log.w(TAG, "Failed to create storage directory");
            }

            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            Log.d(TAG, "Created image file: " + image.getAbsolutePath());
            return image;
        } catch (IOException e) {
            Log.e(TAG, "Error creating image file: ", e);
            Toast.makeText(this, "Failed to create image file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (classifier != null) {
            classifier.close();
            classifier = null;
        }
    }
}

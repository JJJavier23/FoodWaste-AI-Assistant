package com.example.ui;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ImageClassifier {
    private static final String TAG = "ImageClassifier";
    private static final int INPUT_SIZE = 224;
    private static final int PIXEL_SIZE = 3;

    // Define number of output classes
    private static final int NUM_FRUIT_CLASSES = 3;    // Banana, Mango, Tomato
    private static final int NUM_RIPENESS_CLASSES = 4; // OverRipe, Ripe, Unripe, VeryRipe

    // Combined model path
    private static final String MODEL_PATH = "FinalModel.tflite";

    // Class names mappings
    private final String[] fruitClasses = {"Banana", "Mango", "Tomato"};
    private final String[] ripenessClasses = {"OverRipe", "Ripe", "Unripe", "VeryRipe"};

    private Interpreter interpreter;
    private ByteBuffer inputBuffer;
    private boolean isInitialized = false;
    private int preprocessingMethod = 4; // Use Method 4 (no preprocessing) - KEEP THIS AS 4

    public ImageClassifier(AssetManager assetManager) throws IOException {
        try {
            Log.d(TAG, "Loading combined model: " + MODEL_PATH);
            MappedByteBuffer modelBuffer = loadModelFile(assetManager, MODEL_PATH);

            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);

            interpreter = new Interpreter(modelBuffer, options);

            // Verify model has two output tensors (fruit and ripeness)
            // Replace lines 52-55 with:
            int outputCount = interpreter.getOutputTensorCount();
            Log.d(TAG, "Model has " + outputCount + " output tensors instead of expected 2");
// Either adapt to this count or throw a more informative error
            // Verify output tensor shapes
            int fruitOutputClasses = interpreter.getOutputTensor(0).shape()[1];
            int ripenessOutputClasses = interpreter.getOutputTensor(1).shape()[1];

            Log.d(TAG, "Fruit output classes: " + fruitOutputClasses);
            Log.d(TAG, "Ripeness output classes: " + ripenessOutputClasses);

            if (fruitOutputClasses != NUM_FRUIT_CLASSES || ripenessOutputClasses != NUM_RIPENESS_CLASSES) {
                throw new IOException("Model output dimensions do not match expected classes");
            }

            // Create input buffer (4 bytes per float)
            inputBuffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE * 4);
            inputBuffer.order(ByteOrder.nativeOrder());

            Log.d(TAG, "Combined model loaded successfully!");
            Log.d(TAG, "Input shape: " + Arrays.toString(interpreter.getInputTensor(0).shape()));
            Log.d(TAG, "Fruit output shape: " + Arrays.toString(interpreter.getOutputTensor(0).shape()));
            Log.d(TAG, "Ripeness output shape: " + Arrays.toString(interpreter.getOutputTensor(1).shape()));
            Log.d(TAG, "Using preprocessing method: " + preprocessingMethod);

            isInitialized = true;

        } catch (Exception e) {
            Log.e(TAG, "Error initializing TFLite: " + e.getMessage(), e);
            throw new IOException("Failed to initialize Combined TFLite", e);
        }
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);

        fileDescriptor.close();
        inputStream.close();

        return buffer;
    }

    /**
     * Classifies an image and returns both fruit type and ripeness probabilities
     *
     * @param bitmap The input image
     * @return A classification result with fruit and ripeness probabilities
     */
    public CombinedClassificationResult classifyImage(Bitmap bitmap) {
        if (!isInitialized) {
            throw new IllegalStateException("Classifier not initialized");
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);

        // Process image
        inputBuffer.rewind();
        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        resizedBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);

        // Process pixels with the chosen preprocessing method
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];

            // Extract RGB values
            float r = ((pixel >> 16) & 0xFF);
            float g = ((pixel >> 8) & 0xFF);
            float b = (pixel & 0xFF);

            // Apply preprocessing based on selected method
            switch (preprocessingMethod) {
                case 1: // [0,1] scaling
                    r /= 255.0f;
                    g /= 255.0f;
                    b /= 255.0f;
                    break;
                case 2: // [-1,1] scaling
                    r = r / 127.5f - 1.0f;
                    g = g / 127.5f - 1.0f;
                    b = b / 127.5f - 1.0f;
                    break;
                case 3: // ImageNet mean subtraction
                    r = (r - 123.68f) / 255.0f;
                    g = (g - 116.78f) / 255.0f;
                    b = (b - 103.94f) / 255.0f;
                    break;
                case 4: // No preprocessing
                    // Keep raw pixel values
                    break;
            }

            // Store values in RGB order
            inputBuffer.putFloat(r);
            inputBuffer.putFloat(g);
            inputBuffer.putFloat(b);
        }

        // Prepare output buffers
        float[][] fruitOutput = new float[1][NUM_FRUIT_CLASSES];
        float[][] ripenessOutput = new float[1][NUM_RIPENESS_CLASSES];

        // Create output map for multiple outputs
        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, fruitOutput);
        outputMap.put(1, ripenessOutput);

        // Run inference
        inputBuffer.rewind();
        interpreter.runForMultipleInputsOutputs(new Object[]{inputBuffer}, outputMap);

        // Log raw outputs
        Log.d(TAG, "Raw fruit output: " + Arrays.toString(fruitOutput[0]));
        Log.d(TAG, "Raw ripeness output: " + Arrays.toString(ripenessOutput[0]));

        // Apply softmax to get probabilities
        float[] fruitProbabilities = softmax(fruitOutput[0]);
        float[] ripenessProbabilities = softmax(ripenessOutput[0]);

        // Log probabilities for each class
        for (int i = 0; i < NUM_FRUIT_CLASSES; i++) {
            Log.d(TAG, fruitClasses[i] + ": " + String.format("%.2f%%", fruitProbabilities[i] * 100));
        }
        for (int i = 0; i < NUM_RIPENESS_CLASSES; i++) {
            Log.d(TAG, ripenessClasses[i] + ": " + String.format("%.2f%%", ripenessProbabilities[i] * 100));
        }

        // Find the fruit with the highest probability
        int maxFruitIdx = 0;
        float maxFruitProb = fruitProbabilities[0];
        for (int i = 1; i < fruitProbabilities.length; i++) {
            if (fruitProbabilities[i] > maxFruitProb) {
                maxFruitProb = fruitProbabilities[i];
                maxFruitIdx = i;
            }
        }

        // Find the ripeness with the highest probability
        int maxRipenessIdx = 0;
        float maxRipenessProb = ripenessProbabilities[0];
        for (int i = 1; i < ripenessProbabilities.length; i++) {
            if (ripenessProbabilities[i] > maxRipenessProb) {
                maxRipenessProb = ripenessProbabilities[i];
                maxRipenessIdx = i;
            }
        }

        Log.d(TAG, "Predicted fruit: " + fruitClasses[maxFruitIdx] +
                " with confidence " + String.format("%.2f%%", maxFruitProb * 100));
        Log.d(TAG, "Predicted ripeness: " + ripenessClasses[maxRipenessIdx] +
                " with confidence " + String.format("%.2f%%", maxRipenessProb * 100));

        if (resizedBitmap != bitmap) {
            resizedBitmap.recycle();
        }

        // Create and return result
        CombinedClassificationResult result = new CombinedClassificationResult();
        result.fruitProbabilities = fruitProbabilities;
        result.ripenessProbabilities = ripenessProbabilities;
        result.fruitIndex = maxFruitIdx;
        result.ripenessIndex = maxRipenessIdx;
        result.fruitConfidence = maxFruitProb * 100;
        result.ripenessConfidence = maxRipenessProb * 100;

        return result;
    }

    private float[] softmax(float[] logits) {
        float[] probs = new float[logits.length];
        float sum = 0.0f;

        // Find max for numerical stability
        float max = Float.NEGATIVE_INFINITY;
        for (float val : logits) {
            max = Math.max(max, val);
        }

        // Calculate exp of shifted logits and sum
        for (int i = 0; i < logits.length; i++) {
            probs[i] = (float) Math.exp(logits[i] - max);
            sum += probs[i];
        }

        // Normalize
        for (int i = 0; i < probs.length; i++) {
            probs[i] /= sum;
        }

        return probs;
    }

    /**
     * Get fruit class name from index
     */
    public String getFruitClassName(int index) {
        if (index >= 0 && index < fruitClasses.length) {
            return fruitClasses[index];
        }
        return "Unknown";
    }

    /**
     * Get ripeness class name from index
     */
    public String getRipenessClassName(int index) {
        if (index >= 0 && index < ripenessClasses.length) {
            return ripenessClasses[index];
        }
        return "Unknown";
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
            isInitialized = false;
        }
    }

    public void logModelDetails() {
        if (!isInitialized) return;

        Log.d(TAG, "===== COMBINED MODEL DETAILS =====");
        Log.d(TAG, "Model path: " + MODEL_PATH);
        Log.d(TAG, "Fruit classes: " + Arrays.toString(fruitClasses));
        Log.d(TAG, "Ripeness classes: " + Arrays.toString(ripenessClasses));
        Log.d(TAG, "Input tensor count: " + interpreter.getInputTensorCount());
        Log.d(TAG, "Output tensor count: " + interpreter.getOutputTensorCount());

        // Input tensor details
        for (int i = 0; i < interpreter.getInputTensorCount(); i++) {
            Log.d(TAG, "Input #" + i + " shape: " + Arrays.toString(interpreter.getInputTensor(i).shape()));
            Log.d(TAG, "Input #" + i + " type: " + interpreter.getInputTensor(i).dataType());
        }

        // Output tensor details
        for (int i = 0; i < interpreter.getOutputTensorCount(); i++) {
            Log.d(TAG, "Output #" + i + " shape: " + Arrays.toString(interpreter.getOutputTensor(i).shape()));
            Log.d(TAG, "Output #" + i + " type: " + interpreter.getOutputTensor(i).dataType());
        }

        Log.d(TAG, "Using preprocessing method: " + preprocessingMethod);
        Log.d(TAG, "===============================");
    }

    /**
     * Result class to hold both fruit and ripeness classification results
     */
    public static class CombinedClassificationResult {
        public float[] fruitProbabilities;
        public float[] ripenessProbabilities;
        public int fruitIndex;
        public int ripenessIndex;
        public float fruitConfidence;
        public float ripenessConfidence;
    }
}
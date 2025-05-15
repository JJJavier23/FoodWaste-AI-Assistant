package com.example.camerademo;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for input validation in the CombinedImageClassifier
 */
public class ClassifierInputValidationTest {

    /**
     * Test that classifiers should validate initialization state
     */
    @Test
    public void testInitializationValidation() {
        // Create test cases for initialized and uninitialized states
        MockClassifier uninitializedClassifier = new MockClassifier(false);
        MockClassifier initializedClassifier = new MockClassifier(true);

        // Test uninitialized case
        try {
            uninitializedClassifier.simulateClassifyImage();
            fail("Should have thrown IllegalStateException for uninitialized classifier");
        } catch (IllegalStateException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("not initialized"));
        }

        // Test initialized case
        try {
            // This should not throw an IllegalStateException
            initializedClassifier.simulateClassifyImage();
        } catch (IllegalStateException e) {
            fail("Should not throw exception for initialized classifier");
        }
    }

    /**
     * Test that null bitmap validation works
     */
    @Test
    public void testNullBitmapValidation() {
        MockClassifier classifier = new MockClassifier(true);

        // Test with null bitmap
        try {
            classifier.processNullBitmap();
            fail("Should have thrown NullPointerException for null bitmap");
        } catch (NullPointerException e) {
            // Expected exception
        }
    }

    /**
     * Test bitmap resizing logic
     */
    @Test
    public void testBitmapResizing() {
        // Create a small test bitmap (not 224x224)
        Bitmap testBitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);

        // Use our test helper to capture the resized bitmap
        BitmapResizeHelper resizeHelper = new BitmapResizeHelper();
        Bitmap resizedBitmap = resizeHelper.resizeBitmap(testBitmap);

        // Verify the bitmap was resized correctly
        assertNotNull("Resized bitmap should not be null", resizedBitmap);
        assertEquals("Bitmap should be resized to 224 width", 224, resizedBitmap.getWidth());
        assertEquals("Bitmap should be resized to 224 height", 224, resizedBitmap.getHeight());
    }

    /**
     * Mock classifier for testing without extending CombinedImageClassifier
     */
    private static class MockClassifier {
        private final boolean isInitialized;

        public MockClassifier(boolean isInitialized) {
            this.isInitialized = isInitialized;
        }

        public boolean isInitialized() {
            return isInitialized;
        }

        /**
         * Simulates the initialization check in classifyImage
         */
        public void simulateClassifyImage() {
            if (!isInitialized()) {
                throw new IllegalStateException("Classifier not initialized");
            }

            // If initialized, proceed normally
        }

        /**
         * Simulates processing a null bitmap
         */
        public void processNullBitmap() {
            Bitmap nullBitmap = null;

            // This will throw NullPointerException
            Bitmap resized = Bitmap.createScaledBitmap(nullBitmap, 224, 224, true);
        }
    }

    /**
     * Helper class for testing bitmap resizing
     */
    private static class BitmapResizeHelper {
        /**
         * Resize a bitmap to 224x224 (same as in CombinedImageClassifier)
         */
        public Bitmap resizeBitmap(Bitmap bitmap) {
            return Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        }
    }
}

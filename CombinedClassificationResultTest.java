package com.example.camerademo;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the CombinedClassificationResult inner class
 */
public class CombinedClassificationResultTest {

    @Test
    public void testResultFields() {
        // Create a result object
        CombinedImageClassifier.CombinedClassificationResult result =
                new CombinedImageClassifier.CombinedClassificationResult();

        // Test default values (all should be null or 0)
        assertNull(result.fruitProbabilities);
        assertNull(result.ripenessProbabilities);
        assertEquals(0, result.fruitIndex);
        assertEquals(0, result.ripenessIndex);
        assertEquals(0.0f, result.fruitConfidence, 0.0001f);
        assertEquals(0.0f, result.ripenessConfidence, 0.0001f);

        // Set test values
        result.fruitProbabilities = new float[]{0.8f, 0.15f, 0.05f};
        result.ripenessProbabilities = new float[]{0.1f, 0.7f, 0.15f, 0.05f};
        result.fruitIndex = 0;
        result.ripenessIndex = 1;
        result.fruitConfidence = 80.0f;
        result.ripenessConfidence = 70.0f;

        // Verify values were set correctly
        assertArrayEquals(new float[]{0.8f, 0.15f, 0.05f}, result.fruitProbabilities, 0.0001f);
        assertArrayEquals(new float[]{0.1f, 0.7f, 0.15f, 0.05f}, result.ripenessProbabilities, 0.0001f);
        assertEquals(0, result.fruitIndex);
        assertEquals(1, result.ripenessIndex);
        assertEquals(80.0f, result.fruitConfidence, 0.0001f);
        assertEquals(70.0f, result.ripenessConfidence, 0.0001f);
    }

    @Test
    public void testResultWithDifferentValues() {
        // Create result objects with different values
        CombinedImageClassifier.CombinedClassificationResult banana = createResult(
                new float[]{0.9f, 0.05f, 0.05f}, // Banana probabilities
                new float[]{0.1f, 0.8f, 0.05f, 0.05f}, // Ripe probabilities
                0, 1, 90.0f, 80.0f
        );

        CombinedImageClassifier.CombinedClassificationResult mango = createResult(
                new float[]{0.05f, 0.9f, 0.05f}, // Mango probabilities
                new float[]{0.05f, 0.1f, 0.8f, 0.05f}, // Unripe probabilities
                1, 2, 90.0f, 80.0f
        );

        CombinedImageClassifier.CombinedClassificationResult tomato = createResult(
                new float[]{0.05f, 0.05f, 0.9f}, // Tomato probabilities
                new float[]{0.05f, 0.05f, 0.1f, 0.8f}, // VeryRipe probabilities
                2, 3, 90.0f, 80.0f
        );

        // Verify banana result
        assertEquals(0, banana.fruitIndex); // Banana
        assertEquals(1, banana.ripenessIndex); // Ripe

        // Verify mango result
        assertEquals(1, mango.fruitIndex); // Mango
        assertEquals(2, mango.ripenessIndex); // Unripe

        // Verify tomato result
        assertEquals(2, tomato.fruitIndex); // Tomato
        assertEquals(3, tomato.ripenessIndex); // VeryRipe
    }

    /**
     * Helper method to create a result object with specified values
     */
    private CombinedImageClassifier.CombinedClassificationResult createResult(
            float[] fruitProbs, float[] ripenessProbs,
            int fruitIdx, int ripenessIdx,
            float fruitConf, float ripenessConf) {

        CombinedImageClassifier.CombinedClassificationResult result =
                new CombinedImageClassifier.CombinedClassificationResult();

        result.fruitProbabilities = fruitProbs;
        result.ripenessProbabilities = ripenessProbs;
        result.fruitIndex = fruitIdx;
        result.ripenessIndex = ripenessIdx;
        result.fruitConfidence = fruitConf;
        result.ripenessConfidence = ripenessConf;

        return result;
    }
}
package com.example.camerademo;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.io.IOException;

public class ExampleUnitTest {
    private SimpleDateFormat dateFormat;

    @Before
    public void setup() {
        dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    }

    @Test
    public void testTimeStampFormat() {
        String timeStamp = dateFormat.format(new Date());

        assertEquals("Timestamp should be 15 characters", 15, timeStamp.length());
        assertTrue("Should contain underscore", timeStamp.contains("_"));
        assertTrue("Should match format yyyyMMdd_HHmmss",
                timeStamp.matches("\\d{8}_\\d{6}"));
    }

    @Test
    public void testImageFileNameConstruction() {
        String timeStamp = dateFormat.format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        System.out.println("Timestamp length: " + timeStamp.length());
        System.out.println("Full filename: " + imageFileName);
        System.out.println("Full length: " + imageFileName.length());

        assertEquals("Timestamp should be 15 characters", 15, timeStamp.length());

        assertTrue("Should start with JPEG_", imageFileName.startsWith("JPEG_"));
        assertTrue("Should end with underscore", imageFileName.endsWith("_"));

        assertEquals("Length should be JPEG_(5) + timestamp(15) + _(1)",
                21, imageFileName.length());
    }

    @Test
    public void testTempFileCreation() {
        String timeStamp = dateFormat.format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File tempFile = null;
        try {
            tempFile = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    null
            );

            assertTrue("File name should start with JPEG_",
                    tempFile.getName().startsWith("JPEG_"));
            assertTrue("File should end with .jpg",
                    tempFile.getName().endsWith(".jpg"));

        } catch (IOException e) {
            fail("Should not throw IOException");
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }
}
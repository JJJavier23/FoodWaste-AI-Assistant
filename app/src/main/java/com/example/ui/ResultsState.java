package com.example.ui;

public class ResultsState extends PictureCaptureState {
    private AnalyzerResult.AnalyzedResult result;

    public void onEnter() {
        result = new AnalyzerResult.AnalyzedResult("Detected: Apple, Confidence: 97%");
        displayResult();
    }

    public void displayResult() {
        System.out.println(result);
    }

    public void userActionHandle() {
        // Handle button press, etc.
    }

    public void onExit() {
        System.out.println("Exiting ResultsState...");
    }
}


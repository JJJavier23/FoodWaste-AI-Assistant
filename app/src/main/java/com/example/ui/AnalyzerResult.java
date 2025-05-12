package com.example.ui;

public class AnalyzerResult {
    public static class AnalyzedResult {
        private String summary;

        public AnalyzedResult(String summary) {
            this.summary = summary;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        @Override
        public String toString() {
            return "Analysis Summary: " + summary;
        }
    }

}

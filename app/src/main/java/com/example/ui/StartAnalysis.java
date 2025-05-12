package com.example.ui;

public class StartAnalysis implements ICommand{
    private Analysis analysisService;
    private String analysisID;

    public StartAnalysis(Analysis service, String id) {
        this.analysisService = service;
        this.analysisID = id;
    }

    @Override
    public void execute() {
        System.out.println("Starting analysis: " + analysisID);
        // Actual analysis logic here
    }

    @Override
    public void undo() {
        System.out.println("Undo StartAnalysis: Not implemented");
    }
}

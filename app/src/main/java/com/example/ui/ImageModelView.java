package com.example.ui;

public class ImageModelView extends Observer implements ModeView {
    private PhoneScreen screen;
    private Database db;
    private CommandArea commandArea;
    private ImageArea imageArea;
    private UserScreen usersScreen;

    public ImageModelView(PhoneScreen screen, Database db, CommandArea c, ImageArea i, UserScreen u) {
        this.screen = screen;
        this.db = db;
        this.commandArea = c;
        this.imageArea = i;
        this.usersScreen = u;
    }

    @Override
    public void update(Subject subject) {
        bindData();
        updateUI();
    }

    @Override
    public void updateUI() {
        commandArea.render();
        imageArea.render();
        usersScreen.render();
    }

    @Override
    public void handleCommand() {
        commandArea.handlerUserInput();
    }

    @Override
    public void bindData() {
        imageArea.displayImage(screen.getImage());
        usersScreen.updateDisplay(screen.getProduceName());
    }

    public void saveToDatabase() {
        db.save(screen.getImage(), screen.getProduceName());
    }

}

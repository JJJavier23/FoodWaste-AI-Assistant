package com.example.ui;

import java.util.ArrayList;
import java.util.List;

public class PhoneScreen implements Subject{
    private List<Observer> observers = new ArrayList<>();
    private Image image;
    private String produceName;

    @Override
    public void attach(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(this);
        }
    }

    public void setImage(Image image) {
        this.image = image;
        notifyObservers();
    }

    public void setProduceName(String name) {
        this.produceName = name;
        notifyObservers();
    }

    public Image getImage() {
        return image;
    }

    public String getProduceName() {
        return produceName;
    }

}

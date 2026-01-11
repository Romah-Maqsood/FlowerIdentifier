package com.example.floweridentifier.data;

public class Prediction {
    private final String flowerName;
    private final String date;
    private final int imageResourceId;

    public Prediction(String flowerName, String date, int imageResourceId) {
        this.flowerName = flowerName;
        this.date = date;
        this.imageResourceId = imageResourceId;
    }

    public String getFlowerName() {
        return flowerName;
    }

    public String getDate() {
        return date;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
}

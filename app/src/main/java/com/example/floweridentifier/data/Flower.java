package com.example.floweridentifier.data;

public class Flower {
    private final String name;
    private final String scientificName;
    private final String description;
    private final int imageResourceId;

    public Flower(String name, String scientificName, String description, int imageResourceId) {
        this.name = name;
        this.scientificName = scientificName;
        this.description = description;
        this.imageResourceId = imageResourceId;
    }

    public String getName() {
        return name;
    }

    public String getScientificName() {
        return scientificName;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
}

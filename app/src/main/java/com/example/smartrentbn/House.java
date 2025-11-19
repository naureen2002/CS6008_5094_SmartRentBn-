package com.example.smartrentbn;

import java.util.List;

public class House {

    // basic fields to store each house's details
    private String houseId;
    private String name;
    private String description;
    private String price;
    private String imageUrl;
    private String details;
    private String amenities;

    // numbers for rooms
    private int bedrooms;
    private int bathrooms;

    // extra info about the location
    private boolean nearShoppingComplex;
    private double latitude;
    private double longitude;

    // who owns the house
    private String ownerId;

    // list of extra property images
    private List<String> detailImages;

    // if owner adds a video
    private String videoUrl;


    // empty constructor (Firebase uses this to load data)
    public House() {}


    // main constructor I use when creating a new house object
    public House(String houseId, String name, String description, String price, String imageUrl,
                 String details, String amenities, int bedrooms, int bathrooms,
                 boolean nearShoppingComplex, double latitude, double longitude,
                 String ownerId, List<String> detailImages, String videoUrl) {

        this.houseId = houseId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.details = details;
        this.amenities = amenities;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.nearShoppingComplex = nearShoppingComplex;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ownerId = ownerId;
        this.detailImages = detailImages;
        this.videoUrl = videoUrl;
    }


    // all the getters so I can read house info anywhere in the app
    public String getHouseId() { return houseId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getDetails() { return details; }
    public String getAmenities() { return amenities; }
    public int getBedrooms() { return bedrooms; }
    public int getBathrooms() { return bathrooms; }
    public boolean isNearShoppingComplex() { return nearShoppingComplex; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getOwnerId() { return ownerId; }
    public List<String> getDetailImages() { return detailImages; }
    public String getVideoUrl() { return videoUrl; }


    // setters so Firebase can update the data when needed
    public void setHouseId(String houseId) { this.houseId = houseId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(String price) { this.price = price; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDetails(String details) { this.details = details; }
    public void setAmenities(String amenities) { this.amenities = amenities; }
    public void setBedrooms(int bedrooms) { this.bedrooms = bedrooms; }
    public void setBathrooms(int bathrooms) { this.bathrooms = bathrooms; }
    public void setNearShoppingComplex(boolean nearShoppingComplex) { this.nearShoppingComplex = nearShoppingComplex; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public void setDetailImages(List<String> detailImages) { this.detailImages = detailImages; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
}

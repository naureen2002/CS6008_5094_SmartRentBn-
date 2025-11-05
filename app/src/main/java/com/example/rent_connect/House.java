package com.example.rent_connect;

import java.util.List;

public class House {
    private String houseId; // ✅ Firebase key for this house
    private String name;
    private String description;
    private String price;
    private String imageUrl;   // Firebase image
    private String details;
    private String amenities;
    private int bedrooms;
    private int bathrooms;
    private boolean nearShoppingComplex;
    private double latitude;
    private double longitude;
    private String ownerId;
    private List<String> detailImages;
    private String videoUrl; // ✅ Added

    // ✅ Default constructor (required for Firebase)
    public House() {}

    // ✅ Full constructor
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

    // ✅ Getters
    public String getHouseId() { return houseId; }     // NEW
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

    // ✅ Setters
    public void setHouseId(String houseId) { this.houseId = houseId; }   // NEW
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

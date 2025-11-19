package com.example.smartrentbn;

public class HelperClass {

    // these are the basic fields we want to store for each user
    private String name, email, username;

    // constructor I use when creating a new user with all info
    public HelperClass(String name, String email, String username) {
        this.name = name;
        this.email = email;
        this.username = username;
    }

    // empty constructor (needed for Firebase to read the data properly)
    public HelperClass() {
    }

    // getter and setter for name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // getter and setter for email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    // getter and setter for username
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}

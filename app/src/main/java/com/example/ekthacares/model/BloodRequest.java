package com.example.ekthacares.model;

public class BloodRequest {
    private String name;
    private String bloodgroup;
    private String mobile;
    private String city;
    private String state;

    public BloodRequest(String name, String bloodgroup, String mobile, String city, String state) {
        this.name = name;
        this.bloodgroup = bloodgroup;
        this.mobile = mobile;
        this.city = city;
        this.state = state;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getbloodgroup() {
        return bloodgroup;
    }

    public void setbloodgroup(String bloodgroup) {
        this.bloodgroup = bloodgroup;
    }

    public String getmobile() {
        return mobile;
    }

    public void setmobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "BloodRequest{" +
                "name='" + name + '\'' +
                ", bloodGroup='" + bloodgroup + '\'' +
                ", mobileNumber='" + mobile + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                '}';
    }

}

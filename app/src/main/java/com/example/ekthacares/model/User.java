package com.example.ekthacares.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("donorname")
    private String donorName;

    @SerializedName("mobile")
    private String mobile;

    @SerializedName("emailid")
    private String emailId;

    @SerializedName("dateofbirth")
    private String dateOfBirth;

    @SerializedName("bloodgroup")
    private String bloodGroup;

    @SerializedName("age")
    private Integer age;

    @SerializedName("gender")
    private String gender;

    @SerializedName("address")
    private String address;

    @SerializedName("city")
    private String city;

    @SerializedName("state")
    private String state;

    @SerializedName("isverified")
    private boolean isVerified;

    @SerializedName("createdBy")
    private Long createdBy;

    @SerializedName("createdByType")
    private String createdByType;

    @SerializedName("updatedBy")
    private Long updatedBy;

    @SerializedName("deletedBy")
    private Long deletedBy;

    @SerializedName("deletedByType")
    private String deletedByType;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("deletedAt")
    private String deletedAt;

    @SerializedName("isDeleted")
    private boolean isDeleted;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByType() {
        return createdByType;
    }

    public void setCreatedByType(String createdByType) {
        this.createdByType = createdByType;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(Long deletedBy) {
        this.deletedBy = deletedBy;
    }

    public String getDeletedByType() {
        return deletedByType;
    }

    public void setDeletedByType(String deletedByType) {
        this.deletedByType = deletedByType;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    // Default constructor
    public User() {
    }

    // Copy constructor
    public User(User user) {
        this.id = user.id;
        this.donorName = user.donorName;
        this.emailId = user.emailId;
        this.address = user.address;
        this.city = user.city;
        this.state = user.state;
        this.mobile = user.mobile;
        this.dateOfBirth = user.dateOfBirth;
        this.bloodGroup = user.bloodGroup;
        this.age = user.age;
        this.gender = user.gender;
    }
}

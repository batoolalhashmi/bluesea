package com.barmej.bluesea.domain.entity;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String userPhoto;
    private String userName;
    private String email;
    private String status;
    private String bookedTrip;

    public String getBookedTrip() {
        return bookedTrip;
    }

    public void setBookedTrip(String bookedTrip) {
        this.bookedTrip = bookedTrip;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public User() {

    }

    public enum Status {
        FREE,
        ON_TRIP,
        ARRIVED
    }
}


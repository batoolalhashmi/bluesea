package com.barmej.bluesea.domain.entity;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Trip implements Serializable {

    private String id;
    private long date;
    private String pickUpPort;
    private String destinationPort;
    private double pickUpLat;
    private double pickUpLng;
    private double destinationLat;
    private double destinationLng;
    private double currentLat;
    private double currentLng;
    private String status;
    private String captainId;
    private List<String> userIds;

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCaptainId() {
        return captainId;
    }

    public void setCaptainId(String captainId) {
        this.captainId = captainId;
    }



    public double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(double currentLat) {
        this.currentLat = currentLat;
    }

    public double getCurrentLng() {
        return currentLng;
    }

    public void setCurrentLng(double currentLng) {
        this.currentLng = currentLng;
    }

    private String availableSeats;
    private String bookedUpSeats;


    public Trip() {
    }


    public String getBookedUpSeats() {
        return bookedUpSeats;
    }

    public void setBookedUpSeats(String bookedUpSeats) {
        this.bookedUpSeats = bookedUpSeats;
    }

    public String getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(String availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getPickUpPort() {
        return pickUpPort;
    }

    public void setPickUpPort(String pickUpPort) {
        this.pickUpPort = pickUpPort;
    }

    public String getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(String destinationPort) {
        this.destinationPort = destinationPort;
    }

    public double getPickUpLat() {
        return pickUpLat;
    }

    public void setPickUpLat(double pickUpLat) {
        this.pickUpLat = pickUpLat;
    }

    public double getPickUpLng() {
        return pickUpLng;
    }

    public void setPickUpLng(double pickUpLng) {
        this.pickUpLng = pickUpLng;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public double getDestinationLng() {
        return destinationLng;
    }

    public void setDestinationLng(double destinationLng) {
        this.destinationLng = destinationLng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getFormattedDate() {
        return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(new Date(date));
    }
    public enum Status{
        MOVING_SOON,
        ON_TRIP,
        ARRIVED
    }
}

package com.barmej.bluesea.domain.entity;

import java.io.Serializable;

public class FullStatus implements Serializable {
    private User user;
    private Captain captain;
    private Trip trip;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Captain getCaptain() {
        return captain;
    }

    public void setCaptain(Captain captain) {
        this.captain = captain;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }
}

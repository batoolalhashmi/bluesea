package com.barmej.bluesea.domain.entity;

import java.io.Serializable;

public class Captain implements Serializable {
    private String id;
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Captain(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Captain() {

    }

    public enum Status {
        FREE,
        ON_TRIP,
        ARRIVED
    }
}

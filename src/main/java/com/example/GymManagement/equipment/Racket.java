package com.example.GymManagement.equipment;

import jakarta.persistence.Entity;

@Entity
public class Racket extends Equipment {

    private String sportType;   // e.g. "Badminton", "Tennis"
    private String stringStatus; // e.g. "Good", "Needs restringing"

    protected Racket() {
        super();
    }

    public Racket(String name, String condition, String sportType, String stringStatus) {
        super(name, condition);
        this.sportType = sportType;
        this.stringStatus = stringStatus;
    }

    public String getSportType() {
        return sportType;
    }

    public void setSportType(String sportType) {
        this.sportType = sportType;
    }

    public String getStringStatus() {
        return stringStatus;
    }

    public void setStringStatus(String stringStatus) {
        this.stringStatus = stringStatus;
    }
}
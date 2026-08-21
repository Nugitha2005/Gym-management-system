package com.example.GymManagement.equipment;

import jakarta.persistence.Entity;

@Entity
public class Ball extends Equipment {

    private String sportType; // e.g. "Basketball", "Volleyball", "Football"

    protected Ball() {
        super();
    }

    public Ball(String name, String condition, String sportType) {
        super(name, condition);
        this.sportType = sportType;
    }

    public String getSportType() {
        return sportType;
    }

    public void setSportType(String sportType) {
        this.sportType = sportType;
    }
}
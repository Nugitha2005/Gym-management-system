package com.example.GymManagement.incident.entity;

// The workflow states an incident moves through.
// REPORTED -> IN_REVIEW -> RESOLVED -> CLOSED
public enum IncidentStatus {
    REPORTED,
    IN_REVIEW,
    RESOLVED,
    CLOSED
}
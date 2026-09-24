package com.example.GymManagement.incident.entity;

// What kind of incident is being reported. Type decides who deals with it.
public enum IncidentType {
    INJURY,             // routed to the University Doctor
    EQUIPMENT_DAMAGE,   // routed to the Trainer, and flags Member C's equipment
    FACILITY_ISSUE      // routed to the Trainer (lighting, flooring, AC)
}

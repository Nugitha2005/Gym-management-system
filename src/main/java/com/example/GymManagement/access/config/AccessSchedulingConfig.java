package com.example.GymManagement.access.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class AccessSchedulingConfig {
    // 1. Enables Spring's @Scheduled background task execution for CheckInMaintenanceScheduler
}
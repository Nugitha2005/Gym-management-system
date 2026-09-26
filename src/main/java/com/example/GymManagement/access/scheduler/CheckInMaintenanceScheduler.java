package com.example.GymManagement.access.scheduler;

import com.example.GymManagement.access.service.CheckInService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CheckInMaintenanceScheduler {

    private final CheckInService checkInService;

    public CheckInMaintenanceScheduler(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @Scheduled(fixedRate = 900000)
    public void autoCloseForgottenCheckOuts() {
        // 1. Run every 15 minutes (900,000 ms) to auto-close stale open check-in records
        checkInService.autoCloseStaleRecords();
    }
}
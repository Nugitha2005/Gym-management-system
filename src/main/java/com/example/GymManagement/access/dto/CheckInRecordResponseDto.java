package com.example.GymManagement.access.dto;

import com.example.GymManagement.access.entity.EntryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CheckInRecordResponseDto {

    private Long recordId;
    private Long userId;
    private String userName;
    private String universityId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private EntryType entryType;
    private Long bookingId;
    private String sessionTitle;
    private boolean autoClosed;
    private long durationMinutes;
    private boolean insideNow;
    private boolean overstayed;
    private String checkedInByName;
}
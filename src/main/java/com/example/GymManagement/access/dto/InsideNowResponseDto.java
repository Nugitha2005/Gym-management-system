package com.example.GymManagement.access.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InsideNowResponseDto {

    private int peopleInside;
    private Integer capacityNow;
    private int reservedNotArrived;
    private List<CheckInRecordResponseDto> records;
}

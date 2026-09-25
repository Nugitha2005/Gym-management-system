package com.example.GymManagement.access.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CheckInResultDto {

    private String outcome;
    private String message;
    private CheckInRecordResponseDto record;
}
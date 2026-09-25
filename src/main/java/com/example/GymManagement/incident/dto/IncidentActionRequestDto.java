package com.example.GymManagement.incident.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// What the doctor or trainer sends when recording what they did
@Getter
@Setter
@NoArgsConstructor
public class IncidentActionRequestDto {

    @NotBlank(message = "Action taken is required")
    @Size(max = 1000, message = "Action taken must be 1000 characters or fewer")
    private String actionTaken;

    @Size(max = 500, message = "Outcome must be 500 characters or fewer")
    private String outcome;
}
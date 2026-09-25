package com.example.GymManagement.access.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScanRequestDto {

    @NotBlank(message = "Scanned QR code is required")
    private String code;
}

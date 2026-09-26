package com.example.GymManagement.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Body for an admin changing a user's role, e.g. { "roleName": "UNIVERSITY_STAFF" }
@Getter
@Setter
@NoArgsConstructor
public class ChangeRoleRequestDto {

    // 1. Must match a role_name in the role table: STUDENT, UNIVERSITY_STAFF, TRAINER, DOCTOR or ADMIN
    @NotBlank(message = "Role name is required")
    private String roleName;
}

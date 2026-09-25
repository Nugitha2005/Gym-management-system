package com.example.GymManagement.user.dto;

import com.example.GymManagement.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResponseDto {
    private Long userId;
    private String name;
    private String universityId;
    private String email;
    private String contactNo;
    private String roleName;
    private boolean active;

    public UserResponseDto(User user) {
        this.userId = user.getUserId();
        this.name = user.getName();
        this.universityId = user.getUniversityId();
        this.email = user.getEmail();
        this.contactNo = user.getContactNo();
        this.roleName = user.getRole().getRoleName();
        this.active = user.isActive();
    }
}

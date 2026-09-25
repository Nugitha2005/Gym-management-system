package com.example.GymManagement.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="user")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String universityId;

    @ManyToOne(optional = false)
    @JoinColumn(name="role_id",nullable = false)
    private Role role;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;


    private String contactNo;

    @Column(nullable = false, length = 255)
    private String password;
    @Column(nullable = false)
    private boolean active = true;



}

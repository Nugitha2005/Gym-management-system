package com.example.GymManagement.user.repository;

import com.example.GymManagement.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role,Long> {
}

package com.example.GymManagement.user.repository;

import com.example.GymManagement.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}

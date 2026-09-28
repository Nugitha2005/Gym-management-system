package com.example.GymManagement.user.repository;

import com.example.GymManagement.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUniversityId(String universityId);

    // Front-desk lookup: staff type the ID printed on the member's card, in any letter case
    Optional<User> findByUniversityIdIgnoreCase(String universityId);

}

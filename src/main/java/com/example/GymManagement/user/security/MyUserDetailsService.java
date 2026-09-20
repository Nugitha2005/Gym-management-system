package com.example.GymManagement.user.security;

import com.example.GymManagement.user.entity.User;
import com.example.GymManagement.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Spring Security calls this during login to load the user from MySQL
@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Spring calls the parameter "username", but in our system it holds the email
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. Look up the user by email in the database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));

        // 2. Wrap the entity so Spring Security can read password, roles and status
        return new UserPrincipal(user);
    }
}

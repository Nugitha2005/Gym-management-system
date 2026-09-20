package com.example.GymManagement.user.security;

import com.example.GymManagement.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Adapter: translates our User entity into the shape Spring Security expects.
// This is infrastructure only — it is NOT a database table and NOT in the class diagram.
public class UserPrincipal implements UserDetails {

    // 1. The real User entity being wrapped
    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    // 2. Convert the user's Role into a Spring Security authority.
    //    "ROLE_" prefix is required so that hasRole('ADMIN') matches "ROLE_ADMIN"
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authorityName = "ROLE_" + user.getRole().getRoleName();
        return List.of(new SimpleGrantedAuthority(authorityName));
    }

    // 3. Spring Security compares the login password against this BCrypt hash
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // 4. We log in with email, so email is the "username"
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // 5. A deactivated user (active = false) cannot log in
    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    // 6. Account expiry/locking are not part of our scope, so always allow
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 7. Lets controllers reach the full User entity after login
    public User getUser() {
        return user;
    }
}
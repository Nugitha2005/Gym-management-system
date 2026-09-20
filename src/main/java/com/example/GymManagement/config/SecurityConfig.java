package com.example.GymManagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity // Enables @PreAuthorize on controller methods — without this, role checks are silently ignored
public class SecurityConfig {

    // 1. BCrypt hashing for passwords (used at registration and login)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Exposes Spring's AuthenticationManager so AuthController can perform login.
    //    It automatically uses MyUserDetailsService + PasswordEncoder behind the scenes.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 3. CORS — allows the React dev server to call this API and send the session cookie
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setAllowCredentials(true); // required so the JSESSIONID cookie is sent

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

    // 4. Request-level security rules
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 4a. REST API consumed by React — CSRF protection not needed here
                .csrf(csrf -> csrf.disable())

                // 4b. Apply the CORS settings defined above
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 4c. TEMPORARY: all requests are permitted at the filter level so teammates
                //     are not blocked while building their features.
                //     Role protection is enforced per-endpoint with @PreAuthorize instead.
                //     Change to .anyRequest().authenticated() once the whole team is ready.
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

                // 4d. Built-in form login and HTTP Basic are disabled — we use our own /api/auth/login
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
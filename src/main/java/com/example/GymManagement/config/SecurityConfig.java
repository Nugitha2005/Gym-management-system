package com.example.GymManagement.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
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

                // 4c. Who may call what, before any controller runs:
                //     - register and login must work for people who are not logged in yet
                //     - /error is where Spring forwards failed requests; without this, a normal
                //       "400 Bad Request" could turn into a misleading 401
                //     - browser CORS preflight (OPTIONS) requests never carry a login
                //     - everything else requires a logged-in session.
                //     Role checks (TRAINER, DOCTOR, ADMIN) are still done per endpoint with @PreAuthorize.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/error").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 4d. Not logged in -> 401 with a JSON message.
                //     With form login disabled, Spring would otherwise reply with a bare 403,
                //     which wrongly means "logged in but not allowed".
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(this::sendNotLoggedIn)
                )

                // 4e. Built-in form login and HTTP Basic are disabled — we use our own /api/auth/login
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    // 5. Writes the 401 response used by 4d
    private void sendNotLoggedIn(HttpServletRequest request,
                                 HttpServletResponse response,
                                 AuthenticationException authException) throws IOException {

        // 5a. 401 means "you need to log in first"
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 5b. Same {"error": "..."} shape as GlobalExceptionHandler, so the front-end handles both the same way
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"Please log in to continue\"}");
    }
}
package com.example.GymManagement.user.controller;

import com.example.GymManagement.user.dto.LoginRequestDto;
import com.example.GymManagement.user.dto.RegisterRequestDto;
import com.example.GymManagement.user.dto.UserResponseDto;
import com.example.GymManagement.user.entity.User;
import com.example.GymManagement.user.security.UserPrincipal;
import com.example.GymManagement.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    // Stores the logged-in SecurityContext inside the HTTP session
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    // ---------------- REGISTER ----------------
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {

        // 1. Delegate business logic (duplicate checks, hashing, role assignment) to the service
        User createdUser = userService.register(request);

        // 2. Convert saved Entity into Response DTO (never expose the password)
        UserResponseDto responseDto = new UserResponseDto(createdUser);

        // 3. Return HTTP 201 Created with Response DTO body
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // ---------------- LOGIN ----------------
    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@Valid @RequestBody LoginRequestDto request,
                                                 HttpServletRequest httpRequest,
                                                 HttpServletResponse httpResponse) {

        // 1. Package the submitted email + password into an unauthenticated token
        UsernamePasswordAuthenticationToken loginToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        // 2. Spring Security loads the user (MyUserDetailsService) and checks the BCrypt hash.
        //    Wrong credentials throw BadCredentialsException -> handled as 401 in GlobalExceptionHandler
        Authentication authentication = authenticationManager.authenticate(loginToken);

        // 3. Place the authenticated user into a fresh SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 4. Save the context into the HTTP session so the NEXT request is still logged in.
        //    Without this line, login succeeds but every following request is anonymous.
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        // 5. Extract our User entity from the authenticated principal
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UserResponseDto responseDto = new UserResponseDto(principal.getUser());

        // 6. Return HTTP 200 OK with the logged-in user's details
        return ResponseEntity.ok(responseDto);
    }

    // ---------------- CURRENT USER ----------------
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {

        // 1. No principal means no valid session -> not logged in
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not logged in"));
        }

        // 2. Return the currently logged-in user's details
        UserResponseDto responseDto = new UserResponseDto(principal.getUser());
        return ResponseEntity.ok(responseDto);
    }

    // ---------------- LOGOUT ----------------
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest httpRequest) {

        // 1. Get the existing session without creating a new one
        HttpSession session = httpRequest.getSession(false);

        // 2. Destroy the session so the JSESSIONID cookie becomes invalid
        if (session != null) {
            session.invalidate();
        }

        // 3. Clear the security context for the current thread
        SecurityContextHolder.clearContext();

        // 4. Confirm logout
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}

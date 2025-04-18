package com.example.backend.controllers;

import com.example.backend.dtos.LoginRequest;
import com.example.backend.dtos.RegisterRequest;
import com.example.backend.models.User;
import com.example.backend.services.ProfileService;
import com.example.backend.services.UserService;
import com.example.backend.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final ProfileService profileService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Logs in a user")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Optional<User> userOpt = userService.findUserByUsername(loginRequest.getUsername());
        if (userOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Invalid credentials."));

        User user = userOpt.get();
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getHashed_password()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));

        // Generate JWT
        String jwtToken = jwtUtils.generateJwtToken(user);

        // Create secure HTTP-only cookie
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", jwtToken)
                .httpOnly(true)
                .secure(true) // only over HTTPS
                .path("/")
                .maxAge(Duration.ofDays(7)) // 7 days expiry
                .sameSite("Strict") // or "Lax" depending on your frontend needs
                .build();

        // Set cookie in response header
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

        return ResponseEntity.ok(Map.of("message", "Login successful"));
    }

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Registers a new user")
    public ResponseEntity<?> register(@RequestBody RegisterRequest user) {
        if (userService.findUserByUsername(user.getUsername()).isPresent())
            return ResponseEntity.badRequest().body(Map.of("message", "Username already exists"));

        if (userService.findUserByEmail(user.getEmail()).isPresent())
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists"));

        user.setHashed_password(passwordEncoder.encode(user.getHashed_password()));
        User savedUser = userService.saveUser(user);
        profileService.save(profileService.createEmptyProfile(savedUser));

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Registration successful"));
    }

}


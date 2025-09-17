package com.recruitment.system.controller;

import com.recruitment.system.entity.User;
import com.recruitment.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Debug Controller để test password encoding
 */
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @GetMapping("/encode/{password}")
    public String encodePassword(@PathVariable String password) {
        return passwordEncoder.encode(password);
    }
    
    @PostMapping("/verify")
    public boolean verifyPassword(@RequestParam String rawPassword, @RequestParam String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return "User not found";
        }
        
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
        
        return "Password updated for " + email + " with hash: " + hashedPassword;
    }
    
    @GetMapping("/test")
    public String test() {
        return "Debug controller is working!";
    }
}
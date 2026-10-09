package com.example.loginapp;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        if ("ADMIN".equals(request.username()) && "ADMIN".equals(request.password())) {
            return Map.of("success", true, "message", "LOG IN SUCCESS");
        }
        return Map.of("success", false, "message", "LOGIN FAILED");
    }

    public record LoginRequest(String username, String password) {}
}

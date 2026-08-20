package org.example.stocktradingplatform.Controller;

import org.example.stocktradingplatform.Dto.RequestResponse.AuthResponse;
import org.example.stocktradingplatform.Dto.RequestResponse.DepositRequest;
import org.example.stocktradingplatform.Dto.RequestResponse.LoginRequest;
import org.example.stocktradingplatform.Dto.RequestResponse.RegisterRequest;
import org.example.stocktradingplatform.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authService.getCurrentUser(authentication.getName()));
    }

    @PostMapping("/deposit")
    public ResponseEntity<AuthResponse> deposit(@RequestBody DepositRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authService.deposit(authentication.getName(), request.getAmount()));
    }
}

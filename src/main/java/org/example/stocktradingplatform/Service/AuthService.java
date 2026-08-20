package org.example.stocktradingplatform.Service;

import org.example.stocktradingplatform.Dto.RequestResponse.AuthResponse;
import org.example.stocktradingplatform.Dto.RequestResponse.LoginRequest;
import org.example.stocktradingplatform.Dto.RequestResponse.RegisterRequest;
import org.example.stocktradingplatform.Entity.Basket;
import org.example.stocktradingplatform.Entity.Favorite;
import org.example.stocktradingplatform.Entity.Role;
import org.example.stocktradingplatform.Entity.Userr;
import org.example.stocktradingplatform.ExceptionManager.UserrNotFind;
import org.example.stocktradingplatform.Reposity.BasketReposity;
import org.example.stocktradingplatform.Reposity.FavoriteReposity;
import org.example.stocktradingplatform.Reposity.UserReposity;
import org.example.stocktradingplatform.Security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AuthService {

    private final UserReposity userReposity;
    private final BasketReposity basketReposity;
    private final FavoriteReposity favoriteReposity;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserReposity userReposity,
                       BasketReposity basketReposity,
                       FavoriteReposity favoriteReposity,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       AuthenticationManager authenticationManager) {
        this.userReposity = userReposity;
        this.basketReposity = basketReposity;
        this.favoriteReposity = favoriteReposity;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userReposity.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered: " + request.getEmail());
        }

        Userr user = new Userr();
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setAge(request.getAge());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // All regular registrations are standard customers (ROLE_USER).
        // The single Administrator account is auto-seeded on system startup.
        user.setRole(Role.ROLE_USER);

        // Initial balance
        BigDecimal initialBalance = request.getBalance() != null ? request.getBalance() : BigDecimal.valueOf(1000.00);
        user.setBalance(initialBalance);

        // Initialize Basket
        Basket basket = new Basket();
        basket = basketReposity.save(basket);
        user.setBasket(basket);

        // Initialize Favorite
        Favorite favorite = new Favorite();
        favorite = favoriteReposity.save(favorite);
        user.setFavorite(favorite);

        Userr savedUser = userReposity.save(user);
        basket.setUserr(savedUser);
        favorite.setUserr(savedUser);
        basketReposity.save(basket);
        favoriteReposity.save(favorite);

        String token = jwtUtils.generateToken(savedUser);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(savedUser.getId())
                .name(savedUser.getName())
                .surname(savedUser.getSurname())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .age(savedUser.getAge())
                .role(savedUser.getRole())
                .balance(savedUser.getBalance())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password");
        }

        Userr user = userReposity.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserrNotFind("User not found with email: " + request.getEmail()));

        String token = jwtUtils.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .age(user.getAge())
                .role(user.getRole())
                .balance(user.getBalance())
                .build();
    }

    public AuthResponse getCurrentUser(String email) {
        Userr user = userReposity.findByEmail(email)
                .orElseThrow(() -> new UserrNotFind("User not found with email: " + email));

        String token = jwtUtils.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .age(user.getAge())
                .role(user.getRole())
                .balance(user.getBalance())
                .build();
    }

    @Transactional
    public AuthResponse deposit(String email, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be positive");
        }

        Userr user = userReposity.findByEmail(email)
                .orElseThrow(() -> new UserrNotFind("User not found with email: " + email));

        BigDecimal newBalance = (user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO).add(amount);
        user.setBalance(newBalance);
        Userr updated = userReposity.save(user);

        String token = jwtUtils.generateToken(updated);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(updated.getId())
                .name(updated.getName())
                .surname(updated.getSurname())
                .email(updated.getEmail())
                .phoneNumber(updated.getPhoneNumber())
                .age(updated.getAge())
                .role(updated.getRole())
                .balance(updated.getBalance())
                .build();
    }
}

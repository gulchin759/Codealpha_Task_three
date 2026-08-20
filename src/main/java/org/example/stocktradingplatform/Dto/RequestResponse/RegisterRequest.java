package org.example.stocktradingplatform.Dto.RequestResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.stocktradingplatform.Entity.Role;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String name;
    private String surname;
    private String age;
    private String phoneNumber;
    private String email;
    private String password;
    private Role role; // Optional: defaults to ROLE_USER if null
    private BigDecimal balance;
}

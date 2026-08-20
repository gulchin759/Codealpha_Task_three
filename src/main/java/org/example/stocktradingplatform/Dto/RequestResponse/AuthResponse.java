package org.example.stocktradingplatform.Dto.RequestResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.stocktradingplatform.Entity.Role;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String phoneNumber;
    private String age;
    private Role role;
    private BigDecimal balance;
}

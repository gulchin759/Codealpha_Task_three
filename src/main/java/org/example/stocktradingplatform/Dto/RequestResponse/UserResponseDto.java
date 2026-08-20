package org.example.stocktradingplatform.Dto.RequestResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private  Long id;
    private String name;
    private  String surname;
    private String email;
    private String age;
    private String phoneNumber;
    private BigDecimal balance;
    private org.example.stocktradingplatform.Entity.Role role;


}

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

public class UserRequestDto {

    private String name;
    private  String surname;
    private  String age;
    private String phoneNumber;
    private  String email;
    private BigDecimal balance;
    private String password;



}

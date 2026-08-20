package org.example.stocktradingplatform.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
public class Userr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long  id;

    private String name;
    private  String surname;
    private  String age;
    private String phoneNumber;
    @Column(unique = true, nullable = false)
    private  String email;
    private BigDecimal balance;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;


    @OneToMany(mappedBy = "userr")
    private List<Product> products;

    @OneToOne
    @JoinColumn(name="basked_id")
    private  Basket basket;

    @OneToOne
    @JoinColumn(name = "favorite_id")
    private  Favorite favorite;




}

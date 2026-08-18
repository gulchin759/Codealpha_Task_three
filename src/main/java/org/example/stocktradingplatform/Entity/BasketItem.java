package org.example.stocktradingplatform.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;




@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
public class BasketItem {

    @Id
    @GeneratedValue()
    private Long id;


    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;

    @ManyToOne
    @JoinColumn(name = "basket_id")
    @JsonIgnore
    private  Basket basket;

    private Integer itemCount;




}

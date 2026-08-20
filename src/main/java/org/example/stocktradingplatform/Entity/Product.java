package org.example.stocktradingplatform.Entity;



import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor



@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private  String name;
    private BigDecimal price;
    private int stock;
    private  String image;

    @Enumerated(EnumType.STRING)
    private  ProductCategory productCategory;

    @ManyToOne
    @JoinColumn(name = "userr_id")
    private Userr userr;



    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<BasketItem> basketItems;


    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<FavoriteProduct> favorite;


}

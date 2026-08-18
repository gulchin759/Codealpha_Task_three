package org.example.stocktradingplatform.Dto.RequestResponse;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.stocktradingplatform.Entity.ProductCategory;

import java.math.BigDecimal;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ProductResponse {

    private  String name;
    private BigDecimal price;
    private  String image;

    @Enumerated(EnumType.STRING)
    private ProductCategory productCategory;

}

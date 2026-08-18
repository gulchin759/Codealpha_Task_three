package org.example.stocktradingplatform.Dto.Mapper;

import org.example.stocktradingplatform.Dto.RequestResponse.ProductRequestDto;
import org.example.stocktradingplatform.Dto.RequestResponse.ProductResponse;
import org.example.stocktradingplatform.Entity.Product;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(ProductRequestDto dto);
    ProductResponse toProductResponse(Product product);

}

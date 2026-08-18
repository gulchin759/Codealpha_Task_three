package org.example.stocktradingplatform.Service;


import org.example.stocktradingplatform.Dto.Mapper.ProductMapper;
import org.example.stocktradingplatform.Dto.RequestResponse.ProductRequestDto;
import org.example.stocktradingplatform.Dto.RequestResponse.ProductResponse;
import org.example.stocktradingplatform.Entity.Product;
import org.example.stocktradingplatform.ExceptionManager.ProductNotFind;
import org.example.stocktradingplatform.Reposity.ProductReposity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductReposity productReposity;

    public ProductService(ProductMapper productMapper, ProductReposity productReposity) {
        this.productMapper = productMapper;
        this.productReposity = productReposity;
    }


    public ProductResponse createProduct(ProductRequestDto requestDto) {
        Product product = productMapper.toEntity(requestDto);
        Product saveProduct = productReposity.save(product);
        return productMapper.toProductResponse(saveProduct);
    }

    public void deleteProduct(Long id) {
        Product product = productReposity.findById(id).orElseThrow(() -> new ProductNotFind("product not find and delete fail :("));
        productReposity.delete(product);
    }

    public ProductResponse updateProduct(Long id, ProductRequestDto dto) {
        Product product = productMapper.toEntity(dto);
        Product updateProduct = productReposity.findById(id).orElseThrow(() -> new ProductNotFind("product not find and update fail :("));

        updateProduct.setName(product.getName());
        updateProduct.setPrice(product.getPrice());
        updateProduct.setImage(product.getImage());
        updateProduct.setProductCategory(product.getProductCategory());

        Product saveProduct = productReposity.save(updateProduct);
        return productMapper.toProductResponse(saveProduct);

    }

    public List<ProductResponse> getAllProduct() {
        List<ProductResponse> list = productReposity.findAll().stream()
                .map(product -> productMapper.toProductResponse(product))
                .toList();
        return list;
    }

    public ProductResponse getByIdProduct(Long id) {
        Product product = productReposity.findById(id).orElseThrow(() -> new ProductNotFind("product not find  fail :("));
        return productMapper.toProductResponse(product);
    }

    public List<ProductResponse> serchCategory(String category) {
        List<ProductResponse> list = productReposity.findAll()
                .stream()
                .filter(product -> category.equals(product.getProductCategory()))
                .map(product -> productMapper.toProductResponse(product))
                .toList();
        return  list;
    }

    public List<ProductResponse> getName(String name) {
        return productReposity.findByName(name)
                .stream()
                .map(productMapper::toProductResponse)
                .toList();
    }





}

package org.example.stocktradingplatform.Service;


import org.example.stocktradingplatform.Dto.Mapper.ProductMapper;
import org.example.stocktradingplatform.Dto.RequestResponse.ProductRequestDto;
import org.example.stocktradingplatform.Dto.RequestResponse.ProductResponse;
import org.example.stocktradingplatform.Entity.Product;
import org.example.stocktradingplatform.ExceptionManager.ProductNotFind;
import org.example.stocktradingplatform.Reposity.BasketItemRepository;
import org.example.stocktradingplatform.Reposity.FavoriteProductRepository;
import org.example.stocktradingplatform.Reposity.ProductReposity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductReposity productReposity;
    private final FavoriteProductRepository favoriteProductRepository;
    private final BasketItemRepository basketItemRepository;

    public ProductService(ProductMapper productMapper,
                          ProductReposity productReposity,
                          FavoriteProductRepository favoriteProductRepository,
                          BasketItemRepository basketItemRepository) {
        this.productMapper = productMapper;
        this.productReposity = productReposity;
        this.favoriteProductRepository = favoriteProductRepository;
        this.basketItemRepository = basketItemRepository;
    }


    public ProductResponse createProduct(ProductRequestDto requestDto) {
        Product product = productMapper.toEntity(requestDto);
        Product saveProduct = productReposity.save(product);
        return productMapper.toProductResponse(saveProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productReposity.findById(id).orElseThrow(() -> new ProductNotFind("Product not found with id: " + id));
        favoriteProductRepository.deleteByProductId(id);
        basketItemRepository.deleteByProductId(id);
        productReposity.delete(product);
    }

    public ProductResponse updateProduct(Long id, ProductRequestDto dto) {
        Product product = productMapper.toEntity(dto);
        Product updateProduct = productReposity.findById(id).orElseThrow(() -> new ProductNotFind("product not find and update fail :("));

        updateProduct.setName(product.getName());
        updateProduct.setPrice(product.getPrice());
        updateProduct.setStock(product.getStock());
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
                .filter(product -> product.getProductCategory() != null && product.getProductCategory().name().equalsIgnoreCase(category))
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

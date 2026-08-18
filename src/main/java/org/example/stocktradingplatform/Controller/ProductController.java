package org.example.stocktradingplatform.Controller;

import org.example.stocktradingplatform.Dto.RequestResponse.ProductRequestDto;
import org.example.stocktradingplatform.Dto.RequestResponse.ProductResponse;
import org.example.stocktradingplatform.Service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/products")
public class ProductController {

    private  final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ProductResponse createProduct(@RequestBody ProductRequestDto productRequestDto){
        return productService.createProduct(productRequestDto);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
    }

    @PutMapping("/{id}")
    public  ProductResponse updateProduct(@PathVariable Long id,@RequestBody ProductRequestDto productRequestDto){
        return productService.updateProduct(id,productRequestDto);
    }

    @GetMapping
    public List<ProductResponse> getAllProduct(){
        return  productService.getAllProduct();
    }

    @GetMapping("/{id}")
    public ProductResponse getByIdProduct(@PathVariable Long id){
        return productService.getByIdProduct(id);
    }

    @GetMapping("/category/{category}")
    public List<ProductResponse> serchCategory(@PathVariable String category){
        return productService.serchCategory(category);
    }

    @GetMapping("/name/{name}")
    public List<ProductResponse> getName(@PathVariable String name){
        return productService.getName(name);
    }



}

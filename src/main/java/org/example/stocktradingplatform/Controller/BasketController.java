package org.example.stocktradingplatform.Controller;

import org.example.stocktradingplatform.Entity.Product;
import org.example.stocktradingplatform.Service.BaskerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/basket")
public class BasketController {
    private final BaskerService basketService;

    public BasketController(BaskerService basketService) {
        this.basketService = basketService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToBasket(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer itemCount) {

        basketService.addToBasket(userId, productId, itemCount);

        return ResponseEntity.ok("Product added to basket");
    }

    @GetMapping("/allbasketItems/{userId}")
    public List<Product> getAlL(@PathVariable Long userId){
        return  basketService.getAlL(userId);
    }

    @GetMapping("/findname/{name}")
    public List<Product> getFindByName(@RequestParam Long userId, @RequestParam String name){
        return basketService.getFindByName(userId, name);
    }


    @PostMapping("/payment/{userId}")
    public String payment(@PathVariable Long userId) {
        return basketService.payment(userId);
    }









}

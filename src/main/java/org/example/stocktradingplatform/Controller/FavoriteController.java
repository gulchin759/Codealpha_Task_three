package org.example.stocktradingplatform.Controller;


import lombok.RequiredArgsConstructor;
import org.example.stocktradingplatform.Entity.Product;
import org.example.stocktradingplatform.Service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{userId}/{productId}")
    public ResponseEntity<String> toggleFavorite(
            @PathVariable Long userId,
            @PathVariable Long productId
    ) {

        favoriteService.toggleFavorite(userId, productId);

        return ResponseEntity.ok("Favorite updated successfully");
    }
    @GetMapping("/all/{userId}")
    public List<Product> getAllFavorites(@PathVariable Long userId) {
        return favoriteService.getAllFavorites(userId);
    }

    @GetMapping("/search")
    public List<Product> searchFavorites(
            @RequestParam Long userId,
            @RequestParam String name) {

        return favoriteService.searchFavorites(userId, name);
    }
}

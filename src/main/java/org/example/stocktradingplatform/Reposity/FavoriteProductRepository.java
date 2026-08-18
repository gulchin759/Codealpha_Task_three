package org.example.stocktradingplatform.Reposity;

import org.example.stocktradingplatform.Entity.FavoriteProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteProductRepository extends JpaRepository<FavoriteProduct, Long> {

    Optional<FavoriteProduct> findByFavoriteIdAndProductId(
            Long favoriteId,
            Long productId
    );

    List<FavoriteProduct> findByFavoriteId(Long favoriteId);


    List<FavoriteProduct> findByFavoriteIdAndProductNameContainingIgnoreCase(
            Long favoriteId,
            String name
    );

}

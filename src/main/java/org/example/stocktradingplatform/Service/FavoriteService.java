package org.example.stocktradingplatform.Service;


import org.example.stocktradingplatform.Entity.Favorite;
import org.example.stocktradingplatform.Entity.FavoriteProduct;
import org.example.stocktradingplatform.Entity.Product;
import org.example.stocktradingplatform.Entity.Userr;
import org.example.stocktradingplatform.Reposity.FavoriteProductRepository;
import org.example.stocktradingplatform.Reposity.FavoriteReposity;
import org.example.stocktradingplatform.Reposity.ProductReposity;
import org.example.stocktradingplatform.Reposity.UserReposity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteService {


        private final FavoriteReposity favoriteRepository;
        private final ProductReposity productReposity;
        private final FavoriteProductRepository favoriteProductRepository;
        private final UserReposity userReposity;

        public FavoriteService(
                FavoriteReposity favoriteRepository,
                ProductReposity productReposity,
                FavoriteProductRepository favoriteProductRepository,
                UserReposity userReposity) {

            this.favoriteRepository = favoriteRepository;
            this.productReposity = productReposity;
            this.favoriteProductRepository = favoriteProductRepository;
            this.userReposity = userReposity;
        }

        public void toggleFavorite(Long userId, Long productId) {

            Userr userr = userReposity.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Product product = productReposity.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            Favorite favorite = userr.getFavorite();

            if (favorite == null) {
                favorite = new Favorite();
                favorite.setUserr(userr);

                favoriteRepository.save(favorite);
                userr.setFavorite(favorite);
            }

            FavoriteProduct favoriteProduct =
                    favoriteProductRepository
                            .findByFavoriteIdAndProductId(
                                    favorite.getId(),
                                    product.getId()
                            )
                            .orElse(null);

            if (favoriteProduct != null) {

                // artıq favorite-dirsə → sil
                favoriteProductRepository.delete(favoriteProduct);

            } else {

                // favorite deyilsə → əlavə et
                FavoriteProduct newFavoriteProduct = new FavoriteProduct();

                newFavoriteProduct.setFavorite(favorite);
                newFavoriteProduct.setProduct(product);

                favoriteProductRepository.save(newFavoriteProduct);
            }
        }


        public List<Product> getAllFavorites(Long userId) {

            Userr userr = userReposity.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Favorite favorite = userr.getFavorite();

            if (favorite == null) {
                return new ArrayList<>();
            }

            return favoriteProductRepository
                    .findByFavoriteId(favorite.getId())
                    .stream()
                    .map(FavoriteProduct::getProduct)
                    .toList();

    }


    public List<Product> searchFavorites(Long userId, String name) {

        Userr userr = userReposity.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Favorite favorite = userr.getFavorite();

        if (favorite == null) {
            return new ArrayList<>();
        }

        return favoriteProductRepository
                .findByFavoriteIdAndProductNameContainingIgnoreCase(
                        favorite.getId(),
                        name
                )
                .stream()
                .map(FavoriteProduct::getProduct)
                .toList();
    }

}

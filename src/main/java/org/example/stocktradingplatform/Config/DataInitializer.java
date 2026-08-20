package org.example.stocktradingplatform.Config;

import org.example.stocktradingplatform.Entity.*;
import org.example.stocktradingplatform.Reposity.BasketReposity;
import org.example.stocktradingplatform.Reposity.FavoriteReposity;
import org.example.stocktradingplatform.Reposity.ProductReposity;
import org.example.stocktradingplatform.Reposity.UserReposity;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserReposity userReposity;
    private final BasketReposity basketReposity;
    private final FavoriteReposity favoriteReposity;
    private final ProductReposity productReposity;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserReposity userReposity,
                           BasketReposity basketReposity,
                           FavoriteReposity favoriteReposity,
                           ProductReposity productReposity,
                           PasswordEncoder passwordEncoder) {
        this.userReposity = userReposity;
        this.basketReposity = basketReposity;
        this.favoriteReposity = favoriteReposity;
        this.productReposity = productReposity;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedAdminUser();
        seedSampleProducts();
    }

    private void seedAdminUser() {
        String adminEmail = "admin@stock.com";
        if (userReposity.findByEmail(adminEmail).isEmpty()) {
            Userr admin = new Userr();
            admin.setName("Admin");
            admin.setSurname("System");
            admin.setAge("30");
            admin.setPhoneNumber("+994501234567");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ROLE_ADMIN);
            admin.setBalance(BigDecimal.valueOf(10000.00));

            Basket basket = new Basket();
            basket = basketReposity.save(basket);
            admin.setBasket(basket);

            Favorite favorite = new Favorite();
            favorite = favoriteReposity.save(favorite);
            admin.setFavorite(favorite);

            Userr savedAdmin = userReposity.save(admin);
            basket.setUserr(savedAdmin);
            favorite.setUserr(savedAdmin);
            basketReposity.save(basket);
            favoriteReposity.save(favorite);

            System.out.println("==================================================");
            System.out.println(">>> AUTOMATIC ADMIN ACCOUNT CREATED SUCCESSFULLY <<<");
            System.out.println(">>> Email:    admin@stock.com");
            System.out.println(">>> Password: admin123");
            System.out.println(">>> Role:     ROLE_ADMIN");
            System.out.println(">>> Balance:  $ 10,000.00");
            System.out.println("==================================================");
        }
    }

    private void seedSampleProducts() {
        if (productReposity.count() == 0) {
            List<Product> sampleProducts = List.of(
                    createProduct("Fresh Organic Milk", BigDecimal.valueOf(2.50), 50, "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=500", ProductCategory.DAIRY_PRODUCTS),
                    createProduct("Gouda Cheese Block", BigDecimal.valueOf(6.80), 30, "https://images.unsplash.com/photo-1486297678162-eb2a19b0a32d?w=500", ProductCategory.DAIRY_PRODUCTS),
                    createProduct("Prime Beef Steak", BigDecimal.valueOf(18.50), 20, "https://images.unsplash.com/photo-1603048588665-791ca8aea617?w=500", ProductCategory.MEAT),
                    createProduct("Fresh Chicken Breast", BigDecimal.valueOf(8.90), 35, "https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=500", ProductCategory.MEAT),
                    createProduct("Organic Red Apples", BigDecimal.valueOf(3.20), 80, "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=500", ProductCategory.FRUITS),
                    createProduct("Ripe Bananas", BigDecimal.valueOf(2.90), 60, "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=500", ProductCategory.FRUITS),
                    createProduct("Crisp Carrots & Broccoli", BigDecimal.valueOf(3.50), 45, "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=500", ProductCategory.VEGETABLES),
                    createProduct("Artisan Sourdough Bread", BigDecimal.valueOf(4.00), 25, "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500", ProductCategory.BAKERY),
                    createProduct("Cold Brew Arabica Coffee", BigDecimal.valueOf(5.50), 40, "https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=500", ProductCategory.BEVERAGES),
                    createProduct("Natural Sparkling Mineral Water", BigDecimal.valueOf(1.80), 100, "https://images.unsplash.com/photo-1559839914-17aae19cec71?w=500", ProductCategory.BEVERAGES),
                    createProduct("Gourmet Potato Chips", BigDecimal.valueOf(2.80), 50, "https://images.unsplash.com/photo-1566478989037-eec170784d0b?w=500", ProductCategory.SNACKS),
                    createProduct("Organic Green Tea", BigDecimal.valueOf(4.20), 40, "https://images.unsplash.com/photo-1627435601361-ec25f5b1d0e5?w=500", ProductCategory.BEVERAGES)
            );

            productReposity.saveAll(sampleProducts);
            System.out.println(">>> Sample products seeded successfully!");
        }
    }

    private Product createProduct(String name, BigDecimal price, int stock, String image, ProductCategory category) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        product.setImage(image);
        product.setProductCategory(category);
        return product;
    }
}

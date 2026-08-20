package org.example.stocktradingplatform.Service;

import org.example.stocktradingplatform.Entity.Basket;
import org.example.stocktradingplatform.Entity.BasketItem;
import org.example.stocktradingplatform.Entity.Product;
import org.example.stocktradingplatform.Entity.Userr;
import org.example.stocktradingplatform.ExceptionManager.ProductNotFind;
import org.example.stocktradingplatform.ExceptionManager.UserrNotFind;
import org.example.stocktradingplatform.Reposity.BasketItemRepository;
import org.example.stocktradingplatform.Reposity.BasketReposity;
import org.example.stocktradingplatform.Reposity.ProductReposity;
import org.example.stocktradingplatform.Reposity.UserReposity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BaskerService {

    private final UserReposity userReposity;
    private final BasketReposity basketReposity;
    private final ProductReposity productReposity;
    private final BasketItemRepository basketItemRepository;

    public BaskerService(UserReposity userReposity, BasketReposity basketReposity, ProductReposity productReposity, BasketItemRepository basketItemRepository) {
        this.userReposity = userReposity;
        this.basketReposity = basketReposity;
        this.productReposity = productReposity;
        this.basketItemRepository = basketItemRepository;
    }


    public void addToBasket(Long userId, Long productId, Integer itemCount) {

        Userr user = userReposity.findById(userId)
                .orElseThrow(() -> new UserrNotFind("User not found"));

        Product product = productReposity.findById(productId)
                .orElseThrow(() -> new ProductNotFind("Product not found"));

        Basket basket = basketReposity.findByUserrId(userId)
                .orElseGet(() -> {

                    Basket newBasket = new Basket();
                    newBasket.setUserr(user);

                    Basket savedBasket = basketReposity.save(newBasket);

                    // ÇOX VACİB
                    user.setBasket(savedBasket);
                    userReposity.save(user);

                    return savedBasket;
                });

        if (product.getStock() < itemCount) {
            throw new RuntimeException("Not enough stock available");
        }

        Optional<BasketItem> existingItem = basket.getItems()
                .stream()
                .filter(item ->
                        item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {

            BasketItem item = existingItem.get();

            item.setItemCount(
                    item.getItemCount() + itemCount
            );

        } else {

            BasketItem newItem = new BasketItem();

            newItem.setBasket(basket);
            newItem.setProduct(product);
            newItem.setItemCount(itemCount);

            basket.getItems().add(newItem);
        }

        basketReposity.save(basket);
    }


    public List<Product> getAlL(Long userId) {

        List<BasketItem> items =
                basketItemRepository.findByBasket_Userr_Id(userId);

        return items.stream()
                .map(BasketItem::getProduct)
                .toList();
    }


    public List<Product> getFindByName(Long userId, String name) {
        Userr user = userReposity.findById(userId).orElseThrow(() -> new UserrNotFind(" User not find  :("));

        Basket basket = user.getBasket();


        if (basket == null) {
            return new ArrayList<>();
        }

        return basketItemRepository.findByBasketIdAndProductNameContainingIgnoreCase(basket.getId(), name)
                .stream()
                .map(BasketItem::getProduct)
                .toList();
    }


    public void removeFromBasket(Long userId, Long productId) {
        Userr user = userReposity.findById(userId)
                .orElseThrow(() -> new UserrNotFind("User not found"));
        Basket basket = user.getBasket();
        if (basket != null) {
            Optional<BasketItem> itemOpt = basket.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst();
            if (itemOpt.isPresent()) {
                BasketItem item = itemOpt.get();
                basket.getItems().remove(item);
                basketItemRepository.delete(item);
                basketReposity.save(basket);
            }
        }
    }

    public List<BasketItem> getBasketItems(Long userId) {
        return basketItemRepository.findByBasket_Userr_Id(userId);
    }

    // Optional manual clear
    public void clearBasket(Long userId) {
        List<BasketItem> items = basketItemRepository.findByBasket_Userr_Id(userId);
        basketItemRepository.deleteAll(items);
    }


    @Transactional
    public String payment(Long userId) {

        Userr user = userReposity.findById(userId)
                .orElseThrow(() -> new UserrNotFind("User not found"));

        Basket basket = basketReposity.findByUserrId(userId)
                .orElseThrow(() -> new RuntimeException("Basket not found"));

        List<BasketItem> items = basket.getItems();

        if (items.size() == 0) {
            throw new RuntimeException("Basket is empty");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (BasketItem item : items) {

            Product product = item.getProduct();

            if (product.getStock() < item.getItemCount()) {
                throw new RuntimeException(
                        "Not enough stock for product: " + product.getName()
                );
            }

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(item.getItemCount()));

            totalPrice = totalPrice.add(itemTotal);
        }

        if (user.getBalance().compareTo(totalPrice) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // User-dən pulu çıx
        user.setBalance(
                user.getBalance().subtract(totalPrice)
        );

        // Stock-u azalt
        for (BasketItem item : items) {

            Product product = item.getProduct();

            product.setStock(
                    product.getStock() - item.getItemCount()
            );

            productReposity.save(product);
        }


        userReposity.save(user);


        basket.getItems().clear();


        basketReposity.save(basket);

        return "Payment successful";
    }


}

package org.example.stocktradingplatform.Reposity;


import org.example.stocktradingplatform.Entity.BasketItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BasketItemRepository extends JpaRepository<BasketItem, Long> {

    List<BasketItem> findByBasketIdAndProductNameContainingIgnoreCase(
            Long basketId,
            String name
    );


    List<BasketItem> findByBasket_Userr_Id(Long userId);

    void deleteByProductId(Long productId);

    void deleteByBasketId(Long basketId);
}
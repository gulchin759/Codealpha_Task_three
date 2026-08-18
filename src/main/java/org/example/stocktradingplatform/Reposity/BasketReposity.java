package org.example.stocktradingplatform.Reposity;

import org.example.stocktradingplatform.Entity.Basket;


import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BasketReposity extends JpaRepository<Basket,Long> {
    Optional<Basket> findByUserrId(Long userId);

}

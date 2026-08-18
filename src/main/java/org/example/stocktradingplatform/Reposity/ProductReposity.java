package org.example.stocktradingplatform.Reposity;

import org.example.stocktradingplatform.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductReposity extends JpaRepository<Product,Long> {
    List<Product> findByName(String name);
}

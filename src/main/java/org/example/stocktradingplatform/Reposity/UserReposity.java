package org.example.stocktradingplatform.Reposity;

import org.example.stocktradingplatform.Entity.Userr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserReposity extends JpaRepository<Userr,Long> {
    Optional<Userr> findByEmail(String email);
}

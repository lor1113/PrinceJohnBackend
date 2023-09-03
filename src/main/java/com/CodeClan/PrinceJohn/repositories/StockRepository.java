package com.CodeClan.PrinceJohn.repositories;

import com.CodeClan.PrinceJohn.models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByTicker(String ticker);
}

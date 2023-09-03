package com.CodeClan.PrinceJohn.repositories;

import com.CodeClan.PrinceJohn.models.StockMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockMetadataRepository extends JpaRepository<StockMetadata, Long> {
}

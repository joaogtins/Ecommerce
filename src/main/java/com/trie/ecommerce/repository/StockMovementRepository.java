package com.trie.ecommerce.repository;

import com.trie.ecommerce.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByVariantIdOrderByCreatedAtDesc(Long variantId);

    @Query("SELECT COALESCE(SUM(CASE WHEN sm.type = 'IN' THEN sm.quantity " +
           "WHEN sm.type = 'RELEASE' THEN sm.quantity " +
           "WHEN sm.type = 'OUT' THEN -sm.quantity " +
           "WHEN sm.type = 'RESERVE' THEN -sm.quantity ELSE 0 END), 0) " +
           "FROM StockMovement sm WHERE sm.variant.id = :variantId")
    Integer calculateCurrentStock(@Param("variantId") Long variantId);
}

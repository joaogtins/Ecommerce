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

    @Query("SELECT COALESCE(SUM(CASE WHEN sm.type = 'IN' THEN sm.quantity " +
           "WHEN sm.type = 'OUT' THEN -sm.quantity ELSE 0 END), 0) " +
           "FROM StockMovement sm WHERE sm.variant.id = :variantId")
    Integer calculatePhysicalStock(@Param("variantId") Long variantId);

    @Query(value = "SELECT COALESCE(SUM(sm.quantity), 0) FROM stock_movements sm " +
           "WHERE sm.variant_id = :variantId AND sm.type = 'RESERVE' " +
           "AND sm.order_reference IS NOT NULL " +
           "AND CAST(sm.order_reference AS BIGINT) NOT IN (" +
           "  SELECT o.id FROM orders o WHERE o.customer_id = :customerId" +
           ")", nativeQuery = true)
    Integer countActiveReservationsByOthers(@Param("variantId") Long variantId,
                                            @Param("customerId") Long customerId);

    @Query(value = "SELECT COALESCE(SUM(sm.quantity), 0) FROM stock_movements sm " +
           "WHERE sm.variant_id = :variantId AND sm.type = 'RESERVE' " +
           "AND sm.order_reference IS NOT NULL " +
           "AND CAST(sm.order_reference AS BIGINT) IN (" +
           "  SELECT o.id FROM orders o WHERE o.customer_id = :customerId AND o.status = 'DRAFT'" +
           ")", nativeQuery = true)
    Integer countActiveReservationsByCustomer(@Param("variantId") Long variantId,
                                               @Param("customerId") Long customerId);
}

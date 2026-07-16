package com.trie.ecommerce.repository;

import com.trie.ecommerce.entity.DeliveryZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, Long> {

    List<DeliveryZone> findByCityAndStateAndActiveTrue(String city, String state);

    @Query("SELECT dz FROM DeliveryZone dz WHERE dz.city = :city AND dz.state = :state "
         + "AND dz.active = true AND dz.allowedNeighborhoods LIKE %:neighborhood%")
    List<DeliveryZone> findByCityStateAndNeighborhood(String city, String state, String neighborhood);
}

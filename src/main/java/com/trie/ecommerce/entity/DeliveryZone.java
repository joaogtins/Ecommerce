package com.trie.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "delivery_zones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String city;

    private String state;

    @Column(columnDefinition = "TEXT")
    private String allowedNeighborhoods;

    @Builder.Default
    private Boolean active = true;
}

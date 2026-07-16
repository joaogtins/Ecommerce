package com.trie.ecommerce.config;

import com.trie.ecommerce.entity.DeliveryZone;
import com.trie.ecommerce.repository.DeliveryZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final DeliveryZoneRepository deliveryZoneRepository;

    @Override
    public void run(String... args) {
        if (deliveryZoneRepository.count() > 0) {
            log.info("Delivery zones already seeded, skipping");
            return;
        }

        log.info("Seeding delivery zones for dev profile...");

        deliveryZoneRepository.save(DeliveryZone.builder()
            .name("Zona Sul - Sao Paulo")
            .city("Sao Paulo")
            .state("SP")
            .allowedNeighborhoods("Vila Mariana,Moema,Brooklin,Itaim Bibi,Jardins,Pinheiros,Vila Madalena,Morumbi,Butanta")
            .active(true)
            .build());

        deliveryZoneRepository.save(DeliveryZone.builder()
            .name("Zona Oeste - Sao Paulo")
            .city("Sao Paulo")
            .state("SP")
            .allowedNeighborhoods("Barra Funda,Perdizes,Pompeia,Lapa,Vila Leopoldina,Jaguare")
            .active(true)
            .build());

        deliveryZoneRepository.save(DeliveryZone.builder()
            .name("Centro - Sao Paulo")
            .city("Sao Paulo")
            .state("SP")
            .allowedNeighborhoods("Republica,Se,Consolacao,Bela Vista, Liberdade,Bom Retiro,Santa Cecilia")
            .active(true)
            .build());

        log.info("Delivery zones seeded successfully");
    }
}

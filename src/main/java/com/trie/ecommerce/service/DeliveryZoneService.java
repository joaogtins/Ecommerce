package com.trie.ecommerce.service;

import com.trie.ecommerce.dto.request.AddressValidationRequest;
import com.trie.ecommerce.dto.response.AddressValidationResponse;
import com.trie.ecommerce.entity.DeliveryZone;
import com.trie.ecommerce.repository.DeliveryZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryZoneService {

    private final DeliveryZoneRepository deliveryZoneRepository;

    public AddressValidationResponse validate(AddressValidationRequest request) {
        List<DeliveryZone> zones = deliveryZoneRepository
            .findByCityStateAndNeighborhood(request.city(), request.state(), request.neighborhood());

        if (zones.isEmpty()) {
            List<DeliveryZone> cityZones = deliveryZoneRepository
                .findByCityAndStateAndActiveTrue(request.city(), request.state());
            List<String> available = cityZones.stream()
                .flatMap(z -> Arrays.stream(z.getAllowedNeighborhoods().split(",")))
                .map(String::trim)
                .distinct()
                .toList();
            return new AddressValidationResponse(false,
                "Bairro nao atendido. Bairros disponiveis: " + String.join(", ", available),
                available);
        }

        return new AddressValidationResponse(true, "Endereco dentro da area de entrega", null);
    }
}

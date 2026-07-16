package com.trie.ecommerce.controller;

import com.trie.ecommerce.dto.request.AddressValidationRequest;
import com.trie.ecommerce.dto.response.AddressValidationResponse;
import com.trie.ecommerce.service.DeliveryZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Enderecos", description = "Validacao de regiao de entrega")
public class AddressController {

    private final DeliveryZoneService deliveryZoneService;

    @PostMapping("/validate")
    @Operation(summary = "Validar se endereco esta dentro da area de entrega")
    @ApiResponse(responseCode = "200", description = "Resultado da validacao")
    public AddressValidationResponse validate(@Valid @RequestBody AddressValidationRequest request) {
        return deliveryZoneService.validate(request);
    }
}

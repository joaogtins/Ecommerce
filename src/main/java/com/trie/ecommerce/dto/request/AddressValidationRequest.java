package com.trie.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressValidationRequest(
    @NotBlank String street,
    String number,
    @NotBlank String neighborhood,
    @NotBlank String city,
    @NotBlank @Size(min = 2, max = 2) String state,
    @NotBlank String zipCode
) {}

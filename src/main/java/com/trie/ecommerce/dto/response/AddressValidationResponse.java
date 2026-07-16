package com.trie.ecommerce.dto.response;

import java.util.List;

public record AddressValidationResponse(
    boolean valid,
    String message,
    List<String> availableNeighborhoods
) {}

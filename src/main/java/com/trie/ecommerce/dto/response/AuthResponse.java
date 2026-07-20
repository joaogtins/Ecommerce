package com.trie.ecommerce.dto.response;

import com.trie.ecommerce.enums.UserRole;

public record AuthResponse(
    String token,
    Long id,
    String name,
    String email,
    UserRole role
) {}

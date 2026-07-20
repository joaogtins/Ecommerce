package com.trie.ecommerce.controller;

import com.trie.ecommerce.dto.response.CheckoutResponse;
import com.trie.ecommerce.security.CustomerUserDetails;
import com.trie.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Finalizacao do pedido via WhatsApp")
public class CheckoutController {

    private final CartService cartService;

    @PostMapping("/{id}/checkout")
    @Operation(summary = "Finalizar pedido e gerar link do WhatsApp")
    @ApiResponse(responseCode = "200", description = "Link WhatsApp gerado")
    @ApiResponse(responseCode = "404", description = "Carrinho nao encontrado")
    @ApiResponse(responseCode = "422", description = "Carrinho vazio")
    public CheckoutResponse checkout(@PathVariable Long id,
                                      @AuthenticationPrincipal CustomerUserDetails user) {
        return cartService.checkout(user.getId());
    }
}

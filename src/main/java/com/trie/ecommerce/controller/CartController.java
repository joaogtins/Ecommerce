package com.trie.ecommerce.controller;

import com.trie.ecommerce.dto.request.AddItemRequest;
import com.trie.ecommerce.dto.response.OrderResponse;
import com.trie.ecommerce.security.CustomerUserDetails;
import com.trie.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Carrinho", description = "Gestao do carrinho de compras")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Visualizar carrinho atual")
    @ApiResponse(responseCode = "200", description = "Carrinho encontrado")
    @ApiResponse(responseCode = "401", description = "Nao autenticado")
    @ApiResponse(responseCode = "404", description = "Carrinho nao encontrado")
    public OrderResponse getCart(@AuthenticationPrincipal CustomerUserDetails user) {
        return cartService.getCart(user.getId());
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adicionar item ao carrinho")
    @ApiResponse(responseCode = "201", description = "Item adicionado")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    @ApiResponse(responseCode = "401", description = "Nao autenticado")
    @ApiResponse(responseCode = "409", description = "Estoque insuficiente")
    public OrderResponse addItem(@AuthenticationPrincipal CustomerUserDetails user,
                                  @Valid @RequestBody AddItemRequest request) {
        return cartService.addItemToCart(user.getId(), request.variantId(), request.quantity());
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover item do carrinho")
    @ApiResponse(responseCode = "204", description = "Item removido")
    @ApiResponse(responseCode = "404", description = "Item ou carrinho nao encontrado")
    public void removeItem(@AuthenticationPrincipal CustomerUserDetails user,
                           @PathVariable Long itemId) {
        cartService.removeItem(user.getId(), itemId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Limpar carrinho")
    @ApiResponse(responseCode = "204", description = "Carrinho limpo")
    public void clearCart(@AuthenticationPrincipal CustomerUserDetails user) {
        cartService.clearCart(user.getId());
    }
}

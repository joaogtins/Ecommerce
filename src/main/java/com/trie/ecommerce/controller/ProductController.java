package com.trie.ecommerce.controller;

import com.trie.ecommerce.dto.request.CreateProductRequest;
import com.trie.ecommerce.dto.request.UpdateProductRequest;
import com.trie.ecommerce.dto.response.ProductResponse;
import com.trie.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "CRUD de produtos e variantes")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Listar produtos ativos")
    public List<ProductResponse> findAll() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    @ApiResponse(responseCode = "404", description = "Produto nao encontrado")
    public ProductResponse findById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar produto com variantes")
    @ApiResponse(responseCode = "201", description = "Produto criado")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto parcialmente")
    @ApiResponse(responseCode = "404", description = "Produto nao encontrado")
    public ProductResponse update(@PathVariable Long id,
                                   @Valid @RequestBody UpdateProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desativar produto (soft delete)")
    @ApiResponse(responseCode = "204", description = "Produto desativado")
    @ApiResponse(responseCode = "404", description = "Produto nao encontrado")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    @GetMapping("/featured")
    @Operation(summary = "Listar produtos em destaque (Mais vendidos)")
    public List<ProductResponse> findFeatured() {
        return productService.findFeatured();
    }

    @GetMapping("/new-collection")
    @Operation(summary = "Listar produtos da nova coleção")
    public List<ProductResponse> findNewCollection() {
        return productService.findNewCollection();
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar produtos por texto")
    public List<ProductResponse> search(@RequestParam String q) {
        return productService.search(q);
    }

    @GetMapping("/categories")
    @Operation(summary = "Listar categorias distintas")
    public List<String> listCategories() {
        return productService.listCategories();
    }
}

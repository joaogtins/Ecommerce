package com.trie.ecommerce.controller;

import com.trie.ecommerce.dto.request.LoginRequest;
import com.trie.ecommerce.dto.request.RegisterRequest;
import com.trie.ecommerce.dto.response.AuthResponse;
import com.trie.ecommerce.entity.Customer;
import com.trie.ecommerce.security.CustomerUserDetails;
import com.trie.ecommerce.security.JwtTokenProvider;
import com.trie.ecommerce.service.CustomerService;
import com.trie.ecommerce.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacao", description = "Cadastro e login de usuarios")
public class AuthController {

    private final CustomerService customerService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar novo usuario")
    @ApiResponse(responseCode = "201", description = "Usuario cadastrado com token JWT")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    @ApiResponse(responseCode = "422", description = "Email ja cadastrado")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        Customer customer = customerService.register(request);
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        String token = tokenProvider.generateToken(auth);
        return new AuthResponse(token, customer.getId(), customer.getName(),
            customer.getEmail(), customer.getRole());
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario")
    @ApiResponse(responseCode = "200", description = "Login bem-sucedido com token JWT")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    @ApiResponse(responseCode = "401", description = "Email ou senha invalidos")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        CustomerUserDetails user = (CustomerUserDetails) auth.getPrincipal();
        String token = tokenProvider.generateToken(auth);
        return new AuthResponse(token, user.getId(), user.getName(),
            user.getEmail(), user.getRole());
    }
}

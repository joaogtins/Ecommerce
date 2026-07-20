package com.trie.ecommerce.service;

import com.trie.ecommerce.dto.request.RegisterRequest;
import com.trie.ecommerce.entity.Customer;
import com.trie.ecommerce.exception.BusinessException;
import com.trie.ecommerce.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Customer register(RegisterRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email ja cadastrado: " + request.email());
        }

        Customer customer = Customer.builder()
            .name(request.name())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .phone(request.phone())
            .build();

        return customerRepository.save(customer);
    }
}

package com.trie.ecommerce.service;

import com.trie.ecommerce.dto.request.AddItemRequest;
import com.trie.ecommerce.dto.response.CheckoutResponse;
import com.trie.ecommerce.dto.response.OrderResponse;
import com.trie.ecommerce.entity.*;
import com.trie.ecommerce.enums.OrderStatus;
import com.trie.ecommerce.enums.PaymentStatus;
import com.trie.ecommerce.enums.PricingType;
import com.trie.ecommerce.enums.StockMovementType;
import com.trie.ecommerce.exception.BusinessException;
import com.trie.ecommerce.exception.InsufficientStockException;
import com.trie.ecommerce.exception.ResourceNotFoundException;
import com.trie.ecommerce.mapper.OrderMapper;
import com.trie.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockService stockService;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;

    @Value("${app.whatsapp.number}")
    private String whatsappNumber;

    private static final int RESERVATION_MINUTES = 15;

    @Transactional
    public OrderResponse addItemToCart(Long customerId, Long variantId, Integer quantity) {
        ProductVariant variant = variantRepository.findByIdWithLock(variantId)
            .orElseThrow(() -> new ResourceNotFoundException("Variante nao encontrada: " + variantId));

        int physicalStock = stockService.calculatePhysicalStock(variantId);
        int reservedByOthers = countReservedInOtherCarts(variantId, customerId);
        int alreadyReservedByMe = countReservedByCustomer(variantId, customerId);
        int available = physicalStock - reservedByOthers - alreadyReservedByMe;

        if (!variant.getActive() || !variant.getProduct().getActive()) {
            throw new BusinessException("Produto nao esta mais disponivel");
        }
        if (variant.getIsUniquePiece() && available < 1) {
            throw new InsufficientStockException("Peca unica ja reservada ou vendida");
        }
        if (available < quantity) {
            throw new InsufficientStockException(
                String.format("Estoque insuficiente. Disponivel: %d, Solicitado: %d", available, quantity));
        }

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado: " + customerId));

        Order cart = orderRepository.findByCustomerIdAndStatus(customerId, OrderStatus.DRAFT)
            .orElseGet(() -> createDraftOrder(customer));

        StockMovement reservation = StockMovement.builder()
            .variant(variant)
            .type(StockMovementType.RESERVE)
            .quantity(quantity)
            .reason("Reserva - Carrinho #" + cart.getId())
            .orderReference(cart.getId().toString())
            .build();
        stockMovementRepository.save(reservation);

        OrderItem item = OrderItem.builder()
            .order(cart)
            .variant(variant)
            .quantity(quantity)
            .priceAtPurchase(calculatePrice(variant))
            .build();
        cart.getItems().add(item);

        cart.setReservedUntil(LocalDateTime.now().plusMinutes(RESERVATION_MINUTES));
        cart.setUpdatedAt(LocalDateTime.now());
        cart.setTotalAmount(calculateCartTotal(cart));

        return OrderMapper.toResponse(orderRepository.save(cart));
    }

    @Transactional(readOnly = true)
    public OrderResponse getCart(Long customerId) {
        Order cart = orderRepository.findByCustomerIdAndStatus(customerId, OrderStatus.DRAFT)
            .orElseThrow(() -> new ResourceNotFoundException("Carrinho nao encontrado"));
        return OrderMapper.toResponse(cart);
    }

    @Transactional
    public void removeItem(Long customerId, Long itemId) {
        Order cart = orderRepository.findByCustomerIdAndStatus(customerId, OrderStatus.DRAFT)
            .orElseThrow(() -> new ResourceNotFoundException("Carrinho nao encontrado"));

        OrderItem item = cart.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Item nao encontrado no carrinho"));

        StockMovement release = StockMovement.builder()
            .variant(item.getVariant())
            .type(StockMovementType.RELEASE)
            .quantity(item.getQuantity())
            .reason("Remocao do carrinho #" + cart.getId())
            .orderReference(cart.getId().toString())
            .build();
        stockMovementRepository.save(release);

        cart.getItems().remove(item);
        cart.setTotalAmount(calculateCartTotal(cart));
        orderRepository.save(cart);
    }

    @Transactional
    public void clearCart(Long customerId) {
        Order cart = orderRepository.findByCustomerIdAndStatus(customerId, OrderStatus.DRAFT)
            .orElseThrow(() -> new ResourceNotFoundException("Carrinho nao encontrado"));

        for (OrderItem item : cart.getItems()) {
            StockMovement release = StockMovement.builder()
                .variant(item.getVariant())
                .type(StockMovementType.RELEASE)
                .quantity(item.getQuantity())
                .reason("Carrinho limpo #" + cart.getId())
                .orderReference(cart.getId().toString())
                .build();
            stockMovementRepository.save(release);
        }

        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setReservedUntil(null);
        orderRepository.save(cart);
    }

    @Transactional
    public CheckoutResponse checkout(Long customerId) {
        Order cart = orderRepository.findByCustomerIdAndStatus(customerId, OrderStatus.DRAFT)
            .orElseThrow(() -> new ResourceNotFoundException("Carrinho nao encontrado"));

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Carrinho vazio");
        }

        String message = buildWhatsAppMessage(cart);

        cart.setStatus(OrderStatus.PENDING);
        cart.setReservedUntil(LocalDateTime.now().plusMinutes(30));
        cart.setUpdatedAt(LocalDateTime.now());

        Payment payment = Payment.builder()
            .order(cart)
            .status(PaymentStatus.PENDING)
            .method("PIX")
            .amount(cart.getTotalAmount())
            .build();
        paymentRepository.save(payment);
        cart.setPayment(payment);

        orderRepository.save(cart);

        String waLink = "https://wa.me/" + whatsappNumber + "?text="
            + URLEncoder.encode(message, StandardCharsets.UTF_8);

        return new CheckoutResponse(cart.getId(), OrderStatus.PENDING, waLink, cart.getTotalAmount());
    }

    private String buildWhatsAppMessage(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Olá! Gostaria de confirmar o pedido *").append(order.getId()).append("*:\n\n");

        for (OrderItem item : order.getItems()) {
            String name = item.getVariant().getProduct().getName();
            String size = item.getVariant().getSize();
            BigDecimal subtotal = item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity()));
            sb.append("• ").append(name);
            if (size != null) sb.append(" (").append(size).append(")");
            sb.append(" x").append(item.getQuantity())
              .append(" = R$ ").append(String.format("%.2f", subtotal))
              .append("\n");
        }

        sb.append("\n*Total: R$ ").append(String.format("%.2f", order.getTotalAmount())).append("*\n");

        if (order.getDeliveryAddress() != null) {
            Address addr = order.getDeliveryAddress();
            sb.append("\nEndereço: ").append(addr.getStreet()).append(", ").append(addr.getNumber())
              .append(" - ").append(addr.getNeighborhood())
              .append(", ").append(addr.getCity()).append("/").append(addr.getState());
        }

        return sb.toString();
    }

    private int countReservedInOtherCarts(Long variantId, Long customerId) {
        Integer reserved = stockMovementRepository.countActiveReservationsByOthers(variantId, customerId);
        return reserved != null ? reserved : 0;
    }

    private int countReservedByCustomer(Long variantId, Long customerId) {
        Integer reserved = stockMovementRepository.countActiveReservationsByCustomer(variantId, customerId);
        return reserved != null ? reserved : 0;
    }

    private BigDecimal calculatePrice(ProductVariant variant) {
        if (variant.getProduct().getPricingType() == PricingType.BY_GRAM) {
            return variant.getWeightInGrams()
                .multiply(variant.getProduct().getPricePerGram());
        }
        return variant.getPrice();
    }

    private Order createDraftOrder(Customer customer) {
        Order order = Order.builder()
            .customer(customer)
            .status(OrderStatus.DRAFT)
            .totalAmount(BigDecimal.ZERO)
            .build();
        return orderRepository.save(order);
    }

    private BigDecimal calculateCartTotal(Order order) {
        return order.getItems().stream()
            .map(item -> item.getPriceAtPurchase()
                .multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

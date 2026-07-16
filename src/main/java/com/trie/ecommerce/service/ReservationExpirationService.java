package com.trie.ecommerce.service;

import com.trie.ecommerce.entity.Order;
import com.trie.ecommerce.entity.OrderItem;
import com.trie.ecommerce.entity.StockMovement;
import com.trie.ecommerce.enums.OrderStatus;
import com.trie.ecommerce.enums.StockMovementType;
import com.trie.ecommerce.repository.OrderRepository;
import com.trie.ecommerce.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationExpirationService {

    private final OrderRepository orderRepository;
    private final StockMovementRepository stockMovementRepository;

    @Scheduled(fixedRate = 60_000)
    @SchedulerLock(name = "releaseExpiredReservations",
                   lockAtLeastFor = "PT30S",
                   lockAtMostFor = "PT5M")
    @Transactional
    public void releaseExpiredReservations() {
        LocalDateTime cutoff = LocalDateTime.now();
        List<Order> expiredCarts = orderRepository
            .findByStatusAndReservedUntilBefore(OrderStatus.DRAFT, cutoff);

        for (Order cart : expiredCarts) {
            log.info("Liberando reserva expirada do carrinho #{}", cart.getId());

            for (OrderItem item : cart.getItems()) {
                StockMovement release = StockMovement.builder()
                    .variant(item.getVariant())
                    .type(StockMovementType.RELEASE)
                    .quantity(item.getQuantity())
                    .reason("Expiracao automatica - Carrinho #" + cart.getId())
                    .orderReference(cart.getId().toString())
                    .build();
                stockMovementRepository.save(release);
            }

            cart.setStatus(OrderStatus.CANCELLED);
            cart.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(cart);
        }
    }
}

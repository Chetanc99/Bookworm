package com.bookworm.backend.repository;

import com.bookworm.backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart_CartId(Long cartId);
    Optional<CartItem> findByCart_CartIdAndProduct_ProductIdAndIntent(
            Long cartId, Long productId, CartItem.Intent intent);
    boolean existsByCart_CartIdAndProduct_ProductIdAndIntent(
            Long cartId, Long productId, CartItem.Intent intent);
    void deleteByCart_CartId(Long cartId);
}

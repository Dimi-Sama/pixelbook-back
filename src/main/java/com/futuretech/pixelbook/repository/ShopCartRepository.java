package com.futuretech.pixelbook.repository;

import com.futuretech.pixelbook.model.ShopCart;
import com.futuretech.pixelbook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopCartRepository extends JpaRepository<ShopCart, Long> {
    Optional<ShopCart> findByUser(User user);
    Optional<ShopCart> findByUserId(Long userId);
} 
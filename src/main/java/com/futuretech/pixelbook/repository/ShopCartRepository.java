package com.futuretech.pixelbook.repository;

import com.futuretech.pixelbook.model.ShopCart;
import com.futuretech.pixelbook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ShopCartRepository extends JpaRepository<ShopCart, Long> {
    Optional<ShopCart> findByUser(User user);
    Optional<ShopCart> findByUserId(Long userId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM shop_cart_volume WHERE shop_cart_id = :cartId", nativeQuery = true)
    void detachVolumesFromCart(@Param("cartId") Long cartId);
    
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO shop_cart_volume (shop_cart_id, volume_id) VALUES (:cartId, :volumeId)", nativeQuery = true)
    void addVolumeToCart(@Param("cartId") Long cartId, @Param("volumeId") Long volumeId);
} 
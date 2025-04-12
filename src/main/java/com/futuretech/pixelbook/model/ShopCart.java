package com.futuretech.pixelbook.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "shop_carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopCart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToMany
    @JoinTable(
        name = "shop_cart_manga",
        joinColumns = @JoinColumn(name = "shop_cart_id"),
        inverseJoinColumns = @JoinColumn(name = "manga_id")
    )
    private Set<Manga> mangas = new HashSet<>();
} 
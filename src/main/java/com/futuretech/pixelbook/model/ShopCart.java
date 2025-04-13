package com.futuretech.pixelbook.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JsonBackReference
    private User user;
    
    @ManyToMany
    @JoinTable(
        name = "shop_cart_volume",
        joinColumns = @JoinColumn(name = "shop_cart_id"),
        inverseJoinColumns = @JoinColumn(name = "volume_id")
    )
    private Set<Volume> volumes = new HashSet<>();
} 
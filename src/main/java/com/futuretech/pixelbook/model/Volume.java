package com.futuretech.pixelbook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "volumes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Volume {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private Integer number;
    private String coverUrl;
    private Long malId;
    private String isbn;
    private Integer pageCount;
    private Double price;
    private String releaseDate;
    
    @ManyToOne
    @JoinColumn(name = "manga_id")
    private Manga manga;
    
    @OneToMany(mappedBy = "volume", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Content> contents = new ArrayList<>();
    
    @ManyToMany(mappedBy = "volumes")
    @JsonIgnore
    private Set<ShopCart> shopCarts = new HashSet<>();
} 
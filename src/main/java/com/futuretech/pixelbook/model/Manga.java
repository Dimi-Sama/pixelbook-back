package com.futuretech.pixelbook.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mangas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Manga {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String author;
    private String coverUrl;
    @Column(columnDefinition = "TEXT")
    private String synopsis;
    private Long malId;
    private String startDate;
    private String endDate;
    
    @JsonIgnore
    @OneToMany(mappedBy = "manga", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Volume> volumes = new ArrayList<>();
    
    // Plus besoin de ces relations directes qui sont maintenant gérées via Volume
    @JsonIgnore
    @Transient
    private List<Content> contents = new ArrayList<>();
    
    @JsonIgnore
    @Transient
    private List<ShopCart> shopCarts = new ArrayList<>();
} 
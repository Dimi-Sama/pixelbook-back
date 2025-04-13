package com.futuretech.pixelbook.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "bookshelves")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bookshelf {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;
    
    @OneToMany(mappedBy = "bookshelf", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Content> contents = new ArrayList<>();

    @Override
    public int hashCode() {
        // Utiliser uniquement l'id pour éviter la récursion
        return Objects.hash(id);
        // Ne pas inclure user dans le calcul du hashCode
    }
} 
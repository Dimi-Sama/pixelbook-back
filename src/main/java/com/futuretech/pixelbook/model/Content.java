package com.futuretech.pixelbook.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "contents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "bookshelf_id")
    private Bookshelf bookshelf;
    
    @ManyToOne
    @JoinColumn(name = "manga_id")
    private Manga manga;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date addedAt;
} 
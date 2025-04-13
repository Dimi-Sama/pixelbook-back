package com.futuretech.pixelbook.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JsonBackReference
    private Bookshelf bookshelf;
    
    @ManyToOne
    @JoinColumn(name = "volume_id")
    private Volume volume;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date addedAt;
} 
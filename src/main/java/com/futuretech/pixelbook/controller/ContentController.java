package com.futuretech.pixelbook.controller;

import com.futuretech.pixelbook.model.Bookshelf;
import com.futuretech.pixelbook.model.Content;
import com.futuretech.pixelbook.model.Volume;
import com.futuretech.pixelbook.repository.BookshelfRepository;
import com.futuretech.pixelbook.repository.ContentRepository;
import com.futuretech.pixelbook.repository.VolumeRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/contents")
@CrossOrigin(origins = "*")
@Tag(name = "Contenus", description = "API pour gérer les contenus des bibliothèques")
public class ContentController {

    @Autowired
    private ContentRepository contentRepository;
    
    @Autowired
    private BookshelfRepository bookshelfRepository;
    
    @Autowired
    private VolumeRepository volumeRepository;

    @Operation(summary = "Obtenir tous les contenus", description = "Récupère la liste de tous les contenus")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des contenus récupérée avec succès")
    })
    @GetMapping
    public List<Content> getAllContents() {
        return contentRepository.findAll();
    }

    @Operation(summary = "Obtenir un contenu par ID", description = "Récupère les détails d'un contenu spécifique par son ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contenu trouvé"),
        @ApiResponse(responseCode = "404", description = "Contenu non trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Content> getContentById(
            @Parameter(description = "ID du contenu") @PathVariable Long id) {
        Optional<Content> content = contentRepository.findById(id);
        return content.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Obtenir les contenus d'une bibliothèque", description = "Récupère tous les contenus associés à une bibliothèque spécifique")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des contenus récupérée avec succès")
    })
    @GetMapping("/bookshelf/{bookshelfId}")
    public List<Content> getContentsByBookshelfId(
            @Parameter(description = "ID de la bibliothèque") @PathVariable Long bookshelfId) {
        return contentRepository.findByBookshelfId(bookshelfId);
    }

    @Operation(summary = "Ajouter un volume à une bibliothèque", description = "Ajoute un volume spécifique à une bibliothèque")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Volume ajouté avec succès"),
        @ApiResponse(responseCode = "400", description = "Bibliothèque ou volume non trouvé"),
        @ApiResponse(responseCode = "409", description = "Le volume est déjà dans la bibliothèque")
    })
    @PostMapping("/bookshelf/{bookshelfId}/volume/{volumeId}")
    public ResponseEntity<Content> addVolumeToBookshelf(
            @Parameter(description = "ID de la bibliothèque") @PathVariable Long bookshelfId, 
            @Parameter(description = "ID du volume") @PathVariable Long volumeId) {
        Optional<Bookshelf> bookshelfOpt = bookshelfRepository.findById(bookshelfId);
        Optional<Volume> volumeOpt = volumeRepository.findById(volumeId);
        
        if (bookshelfOpt.isEmpty() || volumeOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Vérifier si le volume est déjà dans la bibliothèque
        if (contentRepository.existsByBookshelfIdAndVolumeId(bookshelfId, volumeId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        Content content = new Content();
        content.setBookshelf(bookshelfOpt.get());
        content.setVolume(volumeOpt.get());
        content.setAddedAt(new Date());
        
        Content savedContent = contentRepository.save(content);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedContent);
    }

    @Operation(summary = "Supprimer un contenu", description = "Supprime un contenu par son ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contenu supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Contenu non trouvé")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeContent(
            @Parameter(description = "ID du contenu") @PathVariable Long id) {
        return contentRepository.findById(id)
                .map(content -> {
                    contentRepository.delete(content);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Supprimer un volume d'une bibliothèque", 
              description = "Supprime un volume spécifique d'une bibliothèque")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Volume supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Volume non trouvé dans la bibliothèque")
    })
    @DeleteMapping("/bookshelf/{bookshelfId}/volume/{volumeId}")
    public ResponseEntity<Void> removeVolumeFromBookshelf(
            @Parameter(description = "ID de la bibliothèque") @PathVariable Long bookshelfId, 
            @Parameter(description = "ID du volume") @PathVariable Long volumeId) {
        if (!contentRepository.existsByBookshelfIdAndVolumeId(bookshelfId, volumeId)) {
            return ResponseEntity.notFound().build();
        }
        
        contentRepository.deleteByBookshelfIdAndVolumeId(bookshelfId, volumeId);
        return ResponseEntity.ok().build();
    }
} 
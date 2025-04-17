package com.futuretech.pixelbook.controller;

import com.futuretech.pixelbook.model.Bookshelf;
import com.futuretech.pixelbook.model.User;
import com.futuretech.pixelbook.repository.BookshelfRepository;
import com.futuretech.pixelbook.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookshelves")
@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true",
    allowedHeaders = "*",
    methods = {
        RequestMethod.GET,
        RequestMethod.POST,
        RequestMethod.PUT,
        RequestMethod.DELETE,
        RequestMethod.OPTIONS
    }
)
@Tag(name = "Bibliothèques", description = "API pour gérer les bibliothèques des utilisateurs")
public class BookshelfController {

    @Autowired
    private BookshelfRepository bookshelfRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Obtenir toutes les bibliothèques", description = "Récupère la liste de toutes les bibliothèques")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des bibliothèques récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = Bookshelf.class)))
    })
    @GetMapping
    public List<Bookshelf> getAllBookshelves() {
        return bookshelfRepository.findAll();
    }

    @Operation(summary = "Obtenir une bibliothèque par ID", description = "Récupère les détails d'une bibliothèque spécifique par son ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bibliothèque trouvée",
                    content = @Content(schema = @Schema(implementation = Bookshelf.class))),
        @ApiResponse(responseCode = "404", description = "Bibliothèque non trouvée")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Bookshelf> getBookshelfById(
            @Parameter(description = "ID de la bibliothèque") @PathVariable Long id) {
        Optional<Bookshelf> bookshelf = bookshelfRepository.findById(id);
        return bookshelf.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    // @Operation(summary = "Obtenir la bibliothèque d'un utilisateur", description = "Récupère la bibliothèque associée à un utilisateur spécifique")
    // @ApiResponses(value = {
    //     @ApiResponse(responseCode = "200", description = "Bibliothèque trouvée",
    //                 content = @Content(schema = @Schema(implementation = Bookshelf.class))),
    //     @ApiResponse(responseCode = "404", description = "Bibliothèque non trouvée")
    // })
    // @GetMapping("/user/{userId}")
    // public ResponseEntity<Bookshelf> getBookshelfByUserId(
    //         @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId) {
    //     Optional<Bookshelf> bookshelf = bookshelfRepository.findByUserId(userId);
    //     return bookshelf.map(ResponseEntity::ok)
    //             .orElseGet(() -> ResponseEntity.notFound().build());
    // }

    @Operation(summary = "Créer une nouvelle bibliothèque", description = "Crée une nouvelle bibliothèque pour un utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Bibliothèque créée avec succès",
                    content = @Content(schema = @Schema(implementation = Bookshelf.class))),
        @ApiResponse(responseCode = "400", description = "Utilisateur non trouvé"),
        @ApiResponse(responseCode = "409", description = "L'utilisateur a déjà une bibliothèque")
    })
    @PostMapping
    public ResponseEntity<Bookshelf> createBookshelf(
            @Parameter(description = "Détails de la bibliothèque à créer") @RequestBody Bookshelf bookshelf) {
        Long userId = bookshelf.getUser().getId();
        Optional<User> user = userRepository.findById(userId);
        
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        if (bookshelfRepository.findByUserId(userId).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        bookshelf.setUser(user.get());
        Bookshelf savedBookshelf = bookshelfRepository.save(bookshelf);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBookshelf);
    }

    @Operation(summary = "Supprimer une bibliothèque", description = "Supprime une bibliothèque par son ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bibliothèque supprimée avec succès"),
        @ApiResponse(responseCode = "404", description = "Bibliothèque non trouvée")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookshelf(
            @Parameter(description = "ID de la bibliothèque") @PathVariable Long id) {
        return bookshelfRepository.findById(id)
                .map(bookshelf -> {
                    bookshelfRepository.delete(bookshelf);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
} 
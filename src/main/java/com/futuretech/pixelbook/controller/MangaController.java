package com.futuretech.pixelbook.controller;

import com.futuretech.pixelbook.model.Manga;
import com.futuretech.pixelbook.repository.MangaRepository;

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
@RequestMapping("/api/mangas")
@CrossOrigin(origins = "*")
@Tag(name = "Mangas", description = "API pour gérer les mangas")
public class MangaController {

    @Autowired
    private MangaRepository mangaRepository;

    @Operation(summary = "Obtenir tous les mangas", description = "Récupère la liste de tous les mangas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des mangas récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = Manga.class)))
    })
    @GetMapping
    public List<Manga> getAllMangas() {
        return mangaRepository.findAll();
    }

    @Operation(summary = "Obtenir un manga par ID", description = "Récupère les détails d'un manga spécifique par son ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manga trouvé",
                    content = @Content(schema = @Schema(implementation = Manga.class))),
        @ApiResponse(responseCode = "404", description = "Manga non trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Manga> getMangaById(
            @Parameter(description = "ID du manga") @PathVariable Long id) {
        Optional<Manga> manga = mangaRepository.findById(id);
        return manga.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Rechercher des mangas", description = "Recherche des mangas par mot-clé")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recherche effectuée avec succès",
                    content = @Content(schema = @Schema(implementation = Manga.class)))
    })
    @GetMapping("/search")
    public List<Manga> searchMangas(
            @Parameter(description = "Mot-clé de recherche") @RequestParam String keyword) {
        List<Manga> results = mangaRepository.findByTitleContainingIgnoreCase(keyword);
        results.addAll(mangaRepository.findBySynopsisContainingIgnoreCase(keyword));
        return results;
    }

    @Operation(summary = "Créer un nouveau manga", description = "Crée un nouveau manga")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Manga créé avec succès",
                    content = @Content(schema = @Schema(implementation = Manga.class)))
    })
    @PostMapping
    public ResponseEntity<Manga> createManga(
            @Parameter(description = "Détails du manga à créer") @RequestBody Manga manga) {
        Manga savedManga = mangaRepository.save(manga);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedManga);
    }

    @Operation(summary = "Mettre à jour un manga", description = "Met à jour les informations d'un manga existant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manga mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = Manga.class))),
        @ApiResponse(responseCode = "404", description = "Manga non trouvé")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Manga> updateManga(
            @Parameter(description = "ID du manga") @PathVariable Long id, 
            @Parameter(description = "Détails du manga mis à jour") @RequestBody Manga manga) {
        return mangaRepository.findById(id)
                .map(existingManga -> {
                    manga.setId(id);
                    return ResponseEntity.ok(mangaRepository.save(manga));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Supprimer un manga", description = "Supprime un manga par son ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manga supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Manga non trouvé")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteManga(
            @Parameter(description = "ID du manga") @PathVariable Long id) {
        return mangaRepository.findById(id)
                .map(manga -> {
                    mangaRepository.delete(manga);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
} 
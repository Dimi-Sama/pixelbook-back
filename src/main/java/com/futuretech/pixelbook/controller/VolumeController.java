package com.futuretech.pixelbook.controller;

import com.futuretech.pixelbook.model.Volume;
import com.futuretech.pixelbook.repository.MangaRepository;
import com.futuretech.pixelbook.repository.VolumeRepository;

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

@RestController
@RequestMapping("/api/volumes")
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
@Tag(name = "Volumes", description = "API pour gérer les volumes de manga")
public class VolumeController {

    @Autowired
    private VolumeRepository volumeRepository;
    
    @Autowired
    private MangaRepository mangaRepository;
    
    @Operation(summary = "Obtenir tous les volumes", description = "Récupère la liste de tous les volumes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des volumes récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = Volume.class)))
    })
    @GetMapping
    public ResponseEntity<List<Volume>> getAllVolumes() {
        return ResponseEntity.ok(volumeRepository.findAll());
    }
    
    @Operation(summary = "Obtenir un volume par ID", description = "Récupère les détails d'un volume spécifique par son ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Volume trouvé",
                    content = @Content(schema = @Schema(implementation = Volume.class))),
        @ApiResponse(responseCode = "404", description = "Volume non trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Volume> getVolumeById(
            @Parameter(description = "ID du volume") @PathVariable Long id) {
        return volumeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Obtenir les volumes d'un manga", description = "Récupère tous les volumes associés à un manga spécifique")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des volumes récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = Volume.class)))
    })
    @GetMapping("/manga/{mangaId}")
    public ResponseEntity<List<Volume>> getVolumesByMangaId(
            @Parameter(description = "ID du manga") @PathVariable Long mangaId) {
        return ResponseEntity.ok(volumeRepository.findByMangaId(mangaId));
    }
    
    @Operation(summary = "Créer un nouveau volume", description = "Crée un nouveau volume")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Volume créé avec succès",
                    content = @Content(schema = @Schema(implementation = Volume.class)))
    })
    @PostMapping
    public ResponseEntity<Volume> createVolume(
            @Parameter(description = "Détails du volume à créer") @RequestBody Volume volume) {
        return ResponseEntity.status(HttpStatus.CREATED).body(volumeRepository.save(volume));
    }
    
    @Operation(summary = "Créer un volume pour un manga", description = "Crée un nouveau volume associé à un manga spécifique")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Volume créé avec succès",
                    content = @Content(schema = @Schema(implementation = Volume.class))),
        @ApiResponse(responseCode = "404", description = "Manga non trouvé")
    })
    @PostMapping("/manga/{mangaId}")
    public ResponseEntity<Volume> createVolumeForManga(
            @Parameter(description = "ID du manga") @PathVariable Long mangaId, 
            @Parameter(description = "Détails du volume à créer") @RequestBody Volume volume) {
        return mangaRepository.findById(mangaId)
                .map(manga -> {
                    volume.setManga(manga);
                    Volume savedVolume = volumeRepository.save(volume);
                    return ResponseEntity.status(HttpStatus.CREATED).body(savedVolume);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Mettre à jour un volume", description = "Met à jour les informations d'un volume existant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Volume mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = Volume.class))),
        @ApiResponse(responseCode = "404", description = "Volume non trouvé")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Volume> updateVolume(
            @Parameter(description = "ID du volume") @PathVariable Long id, 
            @Parameter(description = "Détails du volume mis à jour") @RequestBody Volume volume) {
        return volumeRepository.findById(id)
                .map(existingVolume -> {
                    volume.setId(id);
                    return ResponseEntity.ok(volumeRepository.save(volume));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Supprimer un volume", description = "Supprime un volume par son ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Volume supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Volume non trouvé")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVolume(
            @Parameter(description = "ID du volume") @PathVariable Long id) {
        return volumeRepository.findById(id)
                .map(volume -> {
                    volumeRepository.delete(volume);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
} 
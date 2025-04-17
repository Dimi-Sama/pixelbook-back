package com.futuretech.pixelbook.controller;

import com.futuretech.pixelbook.model.Manga;
import com.futuretech.pixelbook.model.Volume;
import com.futuretech.pixelbook.repository.MangaRepository;
import com.futuretech.pixelbook.repository.VolumeRepository;
import com.futuretech.pixelbook.service.JikanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/jikan")
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
@Tag(name = "Jikan API", description = "API pour interagir avec le service Jikan et gérer les mangas")
public class JikanController {

    @Autowired
    private JikanService jikanService;
    
    @Autowired
    private MangaRepository mangaRepository;
    
    @Autowired
    private VolumeRepository volumeRepository;
    
    @Operation(summary = "Rechercher des mangas", description = "Recherche des mangas par titre ou mots-clés")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recherche réussie", 
                    content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchMangas(@RequestParam String query) {
        List<Map<String, Object>> results = jikanService.searchMangas(query);
        return ResponseEntity.ok(results);
    }
    
    @Operation(summary = "Obtenir les mangas populaires", description = "Récupère une liste de mangas populaires avec pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Récupération réussie", 
                    content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/popular")
    public ResponseEntity<List<Map<String, Object>>> getPopularMangas(
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "1") int page, 
            @Parameter(description = "Nombre d'éléments par page") @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> results = jikanService.getPopularMangas(page, limit);
        return ResponseEntity.ok(results);
    }
    
    @Operation(summary = "Obtenir les détails d'un manga", description = "Récupère les informations détaillées d'un manga par son ID MAL")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Récupération réussie", 
                    content = @Content(schema = @Schema(implementation = Manga.class))),
        @ApiResponse(responseCode = "404", description = "Manga non trouvé")
    })
    @GetMapping("/manga/{malId}")
    public ResponseEntity<?> getMangaDetails(@PathVariable Long malId) {
        return ResponseEntity.ok(jikanService.fetchMangaDetails(malId));
    }
    
    @Operation(summary = "Obtenir les volumes d'un manga", description = "Récupère la liste des volumes d'un manga par son ID MAL")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Récupération réussie", 
                    content = @Content(schema = @Schema(implementation = Volume.class))),
        @ApiResponse(responseCode = "404", description = "Manga non trouvé")
    })
    @GetMapping("/manga/{malId}/volumes")
    public ResponseEntity<?> getMangaVolumes(@PathVariable Long malId) {
        return ResponseEntity.ok(jikanService.fetchMangaVolumes(malId));
    }
    
    @Operation(summary = "Importer un manga depuis Jikan", description = "Importe les détails d'un manga depuis Jikan et le sauvegarde dans la base de données")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Importation réussie", 
                    content = @Content(schema = @Schema(implementation = Manga.class))),
        @ApiResponse(responseCode = "404", description = "Manga non trouvé sur Jikan")
    })
    @PostMapping("/import/{malId}")
    public ResponseEntity<Manga> importMangaFromJikan(@PathVariable Long malId) {
        Optional<Manga> existingManga = mangaRepository.findByMalId(malId);
        if (existingManga.isPresent()) {
            return ResponseEntity.ok(existingManga.get());
        }
        
        Manga manga = jikanService.fetchMangaDetails(malId);
        if (manga == null) {
            return ResponseEntity.notFound().build();
        }
        
        Manga savedManga = mangaRepository.save(manga);
        return ResponseEntity.ok(savedManga);
    }
    
    @Operation(summary = "Importer les volumes d'un manga depuis Jikan", 
              description = "Importe les volumes d'un manga depuis Jikan et les sauvegarde dans la base de données")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Importation réussie", 
                    content = @Content(schema = @Schema(implementation = Volume.class))),
        @ApiResponse(responseCode = "404", description = "Manga non trouvé sur Jikan")
    })
    @PostMapping("/import/{malId}/volumes")
    public ResponseEntity<List<Volume>> importMangaVolumesFromJikan(@PathVariable Long malId) {
        // D'abord, on s'assure que le manga existe
        Optional<Manga> existingManga = mangaRepository.findByMalId(malId);
        final Manga manga; // Rendre manga effectivement final
        
        if (existingManga.isEmpty()) {
            Manga fetchedManga = jikanService.fetchMangaDetails(malId);
            if (fetchedManga == null) {
                return ResponseEntity.notFound().build();
            }
            manga = mangaRepository.save(fetchedManga);
        } else {
            manga = existingManga.get();
        }
        
        // Ensuite, on récupère les volumes
        List<Volume> volumes = jikanService.fetchMangaVolumes(malId);
        
        // On associe les volumes au manga
        volumes.forEach(volume -> volume.setManga(manga));
        
        // On sauvegarde tous les volumes
        volumes = volumeRepository.saveAll(volumes);
        
        return ResponseEntity.ok(volumes);
    }
    
    @Operation(summary = "Obtenir les détails d'un volume spécifique", 
              description = "Récupère les informations détaillées d'un volume spécifique d'un manga")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Récupération réussie", 
                    content = @Content(schema = @Schema(implementation = Volume.class))),
        @ApiResponse(responseCode = "404", description = "Volume ou manga non trouvé")
    })
    @GetMapping("/manga/{malId}/volume/{volumeNumber}")
    public ResponseEntity<?> getVolumeDetails(
            @Parameter(description = "ID MAL du manga") @PathVariable Long malId,
            @Parameter(description = "Numéro du volume") @PathVariable Integer volumeNumber) {
        return ResponseEntity.ok(jikanService.fetchVolumeDetails(malId, volumeNumber));
    }
} 
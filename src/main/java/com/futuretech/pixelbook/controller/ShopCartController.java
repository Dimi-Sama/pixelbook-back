package com.futuretech.pixelbook.controller;

import com.futuretech.pixelbook.model.ShopCart;
import com.futuretech.pixelbook.model.Volume;
import com.futuretech.pixelbook.repository.ShopCartRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shopcarts")
@CrossOrigin(origins = "*")
@Tag(name = "Paniers", description = "API pour gérer les paniers d'achat")
public class ShopCartController {

    @Autowired
    private ShopCartRepository shopCartRepository;
    
    @Autowired
    private VolumeRepository volumeRepository;
    
    @Autowired
    private JikanService jikanService;
    
    @Operation(summary = "Obtenir tous les paniers", description = "Récupère la liste de tous les paniers d'achat")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des paniers récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = ShopCart.class)))
    })
    @GetMapping
    public ResponseEntity<List<ShopCart>> getAllShopCarts() {
        return ResponseEntity.ok(shopCartRepository.findAll());
    }
    
    @Operation(summary = "Obtenir un panier par ID", description = "Récupère les détails d'un panier spécifique par son ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Panier trouvé",
                    content = @Content(schema = @Schema(implementation = ShopCart.class))),
        @ApiResponse(responseCode = "404", description = "Panier non trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ShopCart> getShopCartById(
            @Parameter(description = "ID du panier") @PathVariable Long id) {
        return shopCartRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Créer un nouveau panier", description = "Crée un nouveau panier d'achat")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Panier créé avec succès",
                    content = @Content(schema = @Schema(implementation = ShopCart.class)))
    })
    @PostMapping
    public ResponseEntity<ShopCart> createShopCart(
            @Parameter(description = "Détails du panier à créer") @RequestBody ShopCart shopCart) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shopCartRepository.save(shopCart));
    }
    
    @Operation(summary = "Supprimer un panier", description = "Supprime un panier par son ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Panier supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Panier non trouvé")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShopCart(
            @Parameter(description = "ID du panier") @PathVariable Long id) {
        return shopCartRepository.findById(id)
                .map(shopCart -> {
                    shopCartRepository.delete(shopCart);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    // Méthodes pour gérer les volumes
    @Operation(summary = "Ajouter un volume au panier", description = "Ajoute un volume spécifique au panier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Volume ajouté au panier avec succès",
                    content = @Content(schema = @Schema(implementation = ShopCart.class))),
        @ApiResponse(responseCode = "400", description = "Requête invalide"),
        @ApiResponse(responseCode = "409", description = "Le volume est déjà dans le panier")
    })
    @PostMapping("/{shopCartId}/volume/{volumeId}")
    public ResponseEntity<ShopCart> addVolumeToCart(
            @Parameter(description = "ID du panier") @PathVariable Long shopCartId, 
            @Parameter(description = "ID du volume") @PathVariable Long volumeId) {
        Optional<ShopCart> shopCartOpt = shopCartRepository.findById(shopCartId);
        Optional<Volume> volumeOpt = volumeRepository.findById(volumeId);
        
        if (shopCartOpt.isEmpty() || volumeOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        ShopCart shopCart = shopCartOpt.get();
        Volume volume = volumeOpt.get();
        
        // Vérifier si le volume est déjà dans le panier
        if (shopCart.getVolumes().stream().anyMatch(v -> v.getId().equals(volume.getId()))) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        shopCart.getVolumes().add(volume);
        ShopCart updatedShopCart = shopCartRepository.save(shopCart);
        
        return ResponseEntity.ok(updatedShopCart);
    }
    
    @Operation(summary = "Retirer un volume du panier", description = "Retire un volume spécifique du panier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Volume retiré du panier avec succès",
                    content = @Content(schema = @Schema(implementation = ShopCart.class))),
        @ApiResponse(responseCode = "400", description = "Requête invalide"),
        @ApiResponse(responseCode = "404", description = "Volume non trouvé dans le panier")
    })
    @DeleteMapping("/{shopCartId}/volume/{volumeId}")
    public ResponseEntity<ShopCart> removeVolumeFromCart(
            @Parameter(description = "ID du panier") @PathVariable Long shopCartId, 
            @Parameter(description = "ID du volume") @PathVariable Long volumeId) {
        Optional<ShopCart> shopCartOpt = shopCartRepository.findById(shopCartId);
        Optional<Volume> volumeOpt = volumeRepository.findById(volumeId);
        
        if (shopCartOpt.isEmpty() || volumeOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        ShopCart shopCart = shopCartOpt.get();
        Volume volume = volumeOpt.get();
        
        if (!shopCart.getVolumes().removeIf(v -> v.getId().equals(volume.getId()))) {
            return ResponseEntity.notFound().build();
        }
        
        ShopCart updatedShopCart = shopCartRepository.save(shopCart);
        return ResponseEntity.ok(updatedShopCart);
    }
    
    @Operation(summary = "Ajouter un volume au panier par ID MAL", 
              description = "Ajoute un volume spécifique d'un manga identifié par son ID MAL au panier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Volume ajouté au panier avec succès",
                    content = @Content(schema = @Schema(implementation = ShopCart.class))),
        @ApiResponse(responseCode = "400", description = "Requête invalide"),
        @ApiResponse(responseCode = "404", description = "Volume ou manga non trouvé")
    })
    @PostMapping("/{shopCartId}/mal/volume/{malId}/{volumeNumber}")
    public ResponseEntity<ShopCart> addVolumeToCartByMalId(
            @Parameter(description = "ID du panier") @PathVariable Long shopCartId, 
            @Parameter(description = "ID MAL du manga") @PathVariable Long malId,
            @Parameter(description = "Numéro du volume") @PathVariable Integer volumeNumber) {
        Optional<ShopCart> shopCartOpt = shopCartRepository.findById(shopCartId);
        
        if (shopCartOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Vérifier si le volume existe déjà dans notre base de données
        Optional<Volume> existingVolumeOpt = volumeRepository.findByMangaIdAndNumber(malId, volumeNumber);
        
        // Utiliser une variable temporaire pour manipuler le volume
        Volume volumeToAdd;
        
        if (existingVolumeOpt.isEmpty()) {
            // Le volume n'existe pas, on doit récupérer les volumes via Jikan
            List<Volume> volumes = jikanService.fetchMangaVolumes(malId);
            
            // Chercher le volume spécifique par son numéro
            Optional<Volume> matchingVolume = volumes.stream()
                .filter(v -> v.getNumber().equals(volumeNumber))
                .findFirst();
                
            if (matchingVolume.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Sauvegarder le volume dans la base de données
            volumeToAdd = volumeRepository.save(matchingVolume.get());
        } else {
            volumeToAdd = existingVolumeOpt.get();
        }
        
        ShopCart shopCart = shopCartOpt.get();
        
        // Vérifier si le volume est déjà dans le panier
        final Volume finalVolumeToAdd = volumeToAdd;
        if (shopCart.getVolumes().stream().anyMatch(v -> v.getId().equals(finalVolumeToAdd.getId()))) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        shopCart.getVolumes().add(volumeToAdd);
        ShopCart updatedShopCart = shopCartRepository.save(shopCart);
        
        return ResponseEntity.ok(updatedShopCart);
    }
    
    // Si vous aviez précédemment des méthodes qui manipulaient des mangas, 
    // elles ont été intentionnellement supprimées ou remplacées par des méthodes
    // équivalentes qui manipulent des volumes à la place.
} 
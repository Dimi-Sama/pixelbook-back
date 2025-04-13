package com.futuretech.pixelbook.controller;

import com.futuretech.pixelbook.model.Bookshelf;
import com.futuretech.pixelbook.model.Content;
import com.futuretech.pixelbook.model.Manga;
import com.futuretech.pixelbook.model.ShopCart;
import com.futuretech.pixelbook.model.User;
import com.futuretech.pixelbook.model.Volume;
import com.futuretech.pixelbook.repository.BookshelfRepository;
import com.futuretech.pixelbook.repository.ContentRepository;
import com.futuretech.pixelbook.repository.ShopCartRepository;
import com.futuretech.pixelbook.repository.UserRepository;
import com.futuretech.pixelbook.repository.VolumeRepository;
import com.futuretech.pixelbook.repository.MangaRepository;
import com.futuretech.pixelbook.service.JikanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Tag(name = "Utilisateurs", description = "API pour gérer les utilisateurs et leurs ressources")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookshelfRepository bookshelfRepository;
    
    @Autowired
    private ShopCartRepository shopCartRepository;

    @Autowired
    private MangaRepository mangaRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private VolumeRepository volumeRepository;

    @Autowired
    private JikanService jikanService;

    @PersistenceContext
    private EntityManager entityManager;

    @Operation(summary = "Obtenir tous les utilisateurs", description = "Récupère la liste de tous les utilisateurs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des utilisateurs récupérée avec succès")
    })

    @GetMapping
    public ResponseEntity<List<?>> getAllUsers(@RequestParam(required = false) String fetchMode) {
        List<User> users = userRepository.findAll();
        
        if ("basic".equals(fetchMode)) {
            // Créer une liste de DTOs simplifiés
            List<Map<String, Object>> basicUsers = users.stream().map(user -> {
                Map<String, Object> userDto = new HashMap<>();
                userDto.put("id", user.getId());
                userDto.put("email", user.getEmail());
                userDto.put("password", user.getPassword());
                userDto.put("createdAt", user.getCreatedAt());
                userDto.put("skinId", user.getSkinId());
                // Ne pas inclure bookshelf et shopCart
                return userDto;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(basicUsers);
        }
        
        // Initialiser les collections
        users.forEach(user -> {
            if (user.getBookshelf() != null) {
                Hibernate.initialize(user.getBookshelf());
            }
            if (user.getShopCart() != null) {
                Hibernate.initialize(user.getShopCart());
            }
        });
        
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Obtenir un utilisateur par ID", description = "Récupère les détails d'un utilisateur spécifique par son ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utilisateur trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, @RequestParam(required = false) String fetchMode) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        
        if ("basic".equals(fetchMode)) {
            // Créer un DTO simplifié sans les collections
            Map<String, Object> userDto = new HashMap<>();
            userDto.put("id", user.getId());
            userDto.put("email", user.getEmail());
            userDto.put("password", user.getPassword());
            userDto.put("createdAt", user.getCreatedAt());
            userDto.put("skinId", user.getSkinId());
            // Ne pas inclure bookshelf et shopCart
            return ResponseEntity.ok(userDto);
        }
        
        // Initialiser les collections si nécessaire
        if (user.getBookshelf() != null) {
            Hibernate.initialize(user.getBookshelf());
        }
        if (user.getShopCart() != null) {
            Hibernate.initialize(user.getShopCart());
        }
        
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Créer un nouvel utilisateur", description = "Crée un nouvel utilisateur avec une bibliothèque et un panier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès")
    })
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        // Définir la date de création
        user.setCreatedAt(new Date());
        
        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);
        
        // Créer et sauvegarder automatiquement une bibliothèque pour cet utilisateur
        Bookshelf bookshelf = new Bookshelf();
        bookshelf.setUser(savedUser);
        bookshelfRepository.save(bookshelf);
        
        // Créer et sauvegarder automatiquement un panier pour cet utilisateur
        ShopCart shopCart = new ShopCart();
        shopCart.setUser(savedUser);
        shopCartRepository.save(shopCart);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @Operation(summary = "Mettre à jour un utilisateur", description = "Met à jour les informations d'un utilisateur existant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour avec succès")
    })
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    user.setId(id);
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Supprimer un utilisateur", description = "Supprime un utilisateur par son ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utilisateur supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Ajouter un volume à la bibliothèque", description = "Ajoute un volume à la bibliothèque d'un utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Volume ajouté avec succès"),
        @ApiResponse(responseCode = "404", description = "Utilisateur ou volume non trouvé"),
        @ApiResponse(responseCode = "409", description = "Le volume est déjà dans la bibliothèque")
    })
    @PostMapping("/{userId}/bookshelf/volume/{volumeId}")
    public ResponseEntity<?> addVolumeToUserBookshelf(@PathVariable Long userId, @PathVariable Long volumeId) {
        // Vérifier l'utilisateur existe
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }
        
        // Vérifier que le volume existe
        Optional<Volume> volumeOpt = volumeRepository.findById(volumeId);
        if (volumeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Volume non trouvé");
        }
        
        // Trouver la bibliothèque de l'utilisateur
        Optional<Bookshelf> bookshelfOpt = bookshelfRepository.findByUserId(userId);
        if (bookshelfOpt.isEmpty()) {
            // Créer automatiquement une bibliothèque si elle n'existe pas
            Bookshelf newBookshelf = new Bookshelf();
            newBookshelf.setUser(userOpt.get());
            bookshelfOpt = Optional.of(bookshelfRepository.save(newBookshelf));
        }
        
        // Vérifier si le volume est déjà dans la bibliothèque
        Bookshelf bookshelf = bookshelfOpt.get();
        if (contentRepository.existsByBookshelfIdAndVolumeId(bookshelf.getId(), volumeId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ce volume est déjà dans votre bibliothèque");
        }
        
        try {
            // Créer le nouveau contenu
            Content content = new Content();
            content.setBookshelf(bookshelf);
            content.setVolume(volumeOpt.get());
            content.setAddedAt(new Date());
            
            Content savedContent = contentRepository.save(content);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedContent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'ajout du volume: " + e.getMessage());
        }
    }

    @Operation(summary = "Ajouter un volume au panier", description = "Ajoute un volume au panier d'un utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Volume ajouté au panier avec succès"),
        @ApiResponse(responseCode = "400", description = "Requête invalide"),
        @ApiResponse(responseCode = "409", description = "Le volume est déjà dans le panier")
    })
    @PostMapping("/{userId}/shopcart/volume/{volumeId}")
    public ResponseEntity<?> addVolumeToUserCart(@PathVariable Long userId, @PathVariable Long volumeId, @RequestParam(required = false) String fetchMode) {
        // Vérifier si l'utilisateur existe
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Vérifier si le volume existe
        Optional<Volume> volumeOpt = volumeRepository.findById(volumeId);
        if (volumeOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        User user = userOpt.get();
        Volume volume = volumeOpt.get();
        
        // Récupérer le panier de l'utilisateur
        Optional<ShopCart> shopCartOpt = shopCartRepository.findByUserId(userId);
        if (shopCartOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ShopCart shopCart = shopCartOpt.get();
        
        // Ajouter le volume au panier via une requête SQL directe
        // au lieu d'utiliser la collection
        try {
            shopCartRepository.addVolumeToCart(shopCart.getId(), volume.getId());
        } catch (Exception e) {
            // En cas d'erreur (par exemple, le volume est déjà dans le panier)
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        // Si fetchMode est "basic", renvoyer simplement l'ID du panier
        if ("basic".equals(fetchMode)) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", shopCart.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        
        // Sinon, recharger le panier pour avoir les données à jour
        shopCart = shopCartRepository.findById(shopCart.getId()).orElseThrow();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(shopCart);
    }

    @Operation(summary = "Ajouter un volume au panier par ID MAL", 
              description = "Ajoute un volume spécifique d'un manga identifié par son ID MAL au panier d'un utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Volume ajouté au panier avec succès"),
        @ApiResponse(responseCode = "404", description = "Utilisateur, panier ou manga non trouvé"),
        @ApiResponse(responseCode = "409", description = "Le volume est déjà dans le panier")
    })
    @PostMapping("/{userId}/shopcart/mal/volume/{malId}/{volumeNumber}")
    @Transactional
    public ResponseEntity<?> addVolumeToUserCartByMalId(
            @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId,
            @Parameter(description = "ID MAL du manga") @PathVariable Long malId,
            @Parameter(description = "Numéro du volume") @PathVariable Integer volumeNumber) {

        try {
            // 1. Vérifier si l'utilisateur existe
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
            }
            
            // 2. Vérifier si le panier existe
            Optional<ShopCart> shopCartOpt = shopCartRepository.findByUserId(userId);
            if (shopCartOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Panier non trouvé");
            }
            Long shopCartId = shopCartOpt.get().getId();
            
            // 3. Récupérer ou créer le manga
            Manga manga;
            Optional<Manga> existingManga = mangaRepository.findByMalId(malId);
            
            if (existingManga.isPresent()) {
                manga = existingManga.get();
            } else {
                manga = jikanService.fetchMangaDetails(malId);
                if (manga == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Manga non trouvé sur Jikan");
                }
                manga = mangaRepository.save(manga);
            }
            
            // 4. Récupérer ou créer le volume
            Volume volume;
            Optional<Volume> existingVolume = volumeRepository.findByMangaAndNumber(manga, volumeNumber);
            
            if (existingVolume.isPresent()) {
                volume = existingVolume.get();
            } else {
                // Utiliser le service JikanService pour récupérer les détails du volume
                Volume volumeFromApi = jikanService.fetchVolumeDetails(malId, volumeNumber);
                
                if (volumeFromApi == null) {
                    // Si l'API ne fournit pas les détails du volume, créer un volume avec des informations de base
                    Volume newVolume = new Volume();
                    newVolume.setManga(manga);
                    newVolume.setNumber(volumeNumber);
                    newVolume.setTitle(manga.getTitle() + " - Volume " + volumeNumber);
                    newVolume.setMalId(manga.getMalId());
                    
                    // Définir un prix basé sur une logique métier
                    newVolume.setPrice(9.99);
                    
                    // Utiliser la couverture du manga comme fallback
                    newVolume.setCoverUrl(manga.getCoverUrl());
                    
                    volume = volumeRepository.save(newVolume);
                } else {
                    // Associer le volume récupéré de l'API au manga
                    volumeFromApi.setManga(manga);
                    volume = volumeRepository.save(volumeFromApi);
                }
            }
            
            // 5. Vider complètement le contexte de persistance
            entityManager.flush();
            entityManager.clear();
            
            // 6. Exécuter une requête JPQL pour vérifier si le volume est déjà dans le panier
            Long count = (Long) entityManager.createQuery(
                    "SELECT COUNT(v) FROM ShopCart sc JOIN sc.volumes v WHERE sc.id = :cartId AND v.id = :volumeId")
                    .setParameter("cartId", shopCartId)
                    .setParameter("volumeId", volume.getId())
                    .getSingleResult();
            
            if (count == 0) {
                // 7. Exécuter une requête JPQL pour ajouter le volume au panier
                // Cette approche contourne complètement les problèmes de collection
                entityManager.createNativeQuery(
                        "INSERT INTO shop_cart_volume (shop_cart_id, volume_id) VALUES (:cartId, :volumeId)")
                        .setParameter("cartId", shopCartId)
                        .setParameter("volumeId", volume.getId())
                        .executeUpdate();
                
                return ResponseEntity.ok("Volume ajouté au panier");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ce volume est déjà dans le panier");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur: " + e.getMessage() + " - " + e.getClass().getName());
        }
    }

    @Operation(summary = "Valider le panier", description = "Transfère tous les volumes du panier vers la bibliothèque de l'utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Panier validé avec succès"),
        @ApiResponse(responseCode = "400", description = "Le panier est vide"),
        @ApiResponse(responseCode = "404", description = "Utilisateur ou panier non trouvé")
    })
    @PostMapping("/{userId}/shopcart/checkout")
    @Transactional
    public ResponseEntity<?> checkoutShopCart(@PathVariable Long userId) {
        try {
            // 1. Vérifier si l'utilisateur existe
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
            }
            User user = userOpt.get();
            
            // 2. Récupérer le panier de l'utilisateur
            Optional<ShopCart> shopCartOpt = shopCartRepository.findByUserId(userId);
            if (shopCartOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Panier non trouvé");
            }
            ShopCart shopCart = shopCartOpt.get();
            Long shopCartId = shopCart.getId();
            
            // 3. Récupérer directement les volumes du panier avec une requête JPQL
            List<Volume> cartVolumes = entityManager.createQuery(
                    "SELECT v FROM ShopCart sc JOIN sc.volumes v WHERE sc.id = :cartId", Volume.class)
                    .setParameter("cartId", shopCartId)
                    .getResultList();
            
            // Vérifier si le panier est vide
            if (cartVolumes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le panier est vide");
            }
            
            // 4. Récupérer la bibliothèque de l'utilisateur
            Optional<Bookshelf> bookshelfOpt = bookshelfRepository.findByUserId(userId);
            if (bookshelfOpt.isEmpty()) {
                // Créer une bibliothèque si elle n'existe pas
                Bookshelf newBookshelf = new Bookshelf();
                newBookshelf.setUser(user);
                bookshelfOpt = Optional.of(bookshelfRepository.save(newBookshelf));
            }
            Bookshelf bookshelf = bookshelfOpt.get();
            
            // 5. Ajouter chaque volume du panier à la bibliothèque
            List<Content> addedContents = new ArrayList<>();
            Date now = new Date();
            
            for (Volume volume : cartVolumes) {
                // Vérifier si le volume est déjà dans la bibliothèque
                if (!contentRepository.existsByBookshelfIdAndVolumeId(bookshelf.getId(), volume.getId())) {
                    // Créer un nouveau contenu
                    Content content = new Content();
                    content.setBookshelf(bookshelf);
                    content.setVolume(volume);
                    content.setAddedAt(now);
                    
                    Content savedContent = contentRepository.save(content);
                    addedContents.add(savedContent);
                }
            }
            
            // 6. Vider le panier
            // Utiliser une requête native pour éviter les problèmes de collection
            entityManager.createNativeQuery("DELETE FROM shop_cart_volume WHERE shop_cart_id = :cartId")
                    .setParameter("cartId", shopCartId)
                    .executeUpdate();
            
            // 7. Retourner le résultat
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Paiement validé avec succès");
            result.put("addedVolumes", addedContents.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace(); // Ajouter cette ligne pour voir l'erreur complète dans les logs
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la validation du panier: " + (e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @Operation(summary = "Obtenir le panier d'un utilisateur", description = "Récupère le contenu du panier d'un utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Panier récupéré avec succès"),
        @ApiResponse(responseCode = "404", description = "Utilisateur ou panier non trouvé")
    })
    @GetMapping("/{userId}/shopcart")
    public ResponseEntity<?> getUserShopCart(@PathVariable Long userId) {
        try {
            // Vérifier si l'utilisateur existe
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
            }
            
            // Récupérer le panier de l'utilisateur
            Optional<ShopCart> shopCartOpt = shopCartRepository.findByUserId(userId);
            if (shopCartOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Panier non trouvé");
            }
            
            ShopCart shopCart = shopCartOpt.get();
            Long shopCartId = shopCart.getId();
            
            // Récupérer les volumes du panier avec une requête JPQL pour éviter les problèmes de collection
            List<Volume> cartVolumes = entityManager.createQuery(
                    "SELECT v FROM ShopCart sc JOIN sc.volumes v WHERE sc.id = :cartId", Volume.class)
                    .setParameter("cartId", shopCartId)
                    .getResultList();
            
            // Créer un objet de réponse avec les informations du panier
            Map<String, Object> response = new HashMap<>();
            response.put("id", shopCart.getId());
            response.put("userId", userId);
            response.put("volumes", cartVolumes);
            response.put("totalItems", cartVolumes.size());
            
            // Calculer le prix total
            double totalPrice = cartVolumes.stream()
                    .mapToDouble(Volume::getPrice)
                    .sum();
            response.put("totalPrice", totalPrice);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération du panier: " + e.getMessage());
        }
    }

    @Operation(summary = "Obtenir la bibliothèque d'un utilisateur", description = "Récupère le contenu de la bibliothèque d'un utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bibliothèque récupérée avec succès"),
        @ApiResponse(responseCode = "404", description = "Utilisateur ou bibliothèque non trouvé")
    })
    @GetMapping("/{userId}/bookshelf")
    public ResponseEntity<?> getUserBookshelf(@PathVariable Long userId) {
        try {
            // Vérifier si l'utilisateur existe
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
            }
            
            // Récupérer la bibliothèque de l'utilisateur
            Optional<Bookshelf> bookshelfOpt = bookshelfRepository.findByUserId(userId);
            if (bookshelfOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bibliothèque non trouvée");
            }
            
            Bookshelf bookshelf = bookshelfOpt.get();
            
            // Récupérer les contenus de la bibliothèque avec les volumes associés
            List<Content> contents = contentRepository.findByBookshelfId(bookshelf.getId());
            
            // Créer un objet de réponse avec les informations de la bibliothèque
            Map<String, Object> response = new HashMap<>();
            response.put("id", bookshelf.getId());
            response.put("userId", userId);
            response.put("totalItems", contents.size());
            
            // Transformer les contenus pour inclure les informations des volumes
            List<Map<String, Object>> contentList = contents.stream().map(content -> {
                Map<String, Object> contentMap = new HashMap<>();
                contentMap.put("id", content.getId());
                contentMap.put("addedAt", content.getAddedAt());
                contentMap.put("volume", content.getVolume());
                return contentMap;
            }).collect(Collectors.toList());
            
            response.put("contents", contentList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération de la bibliothèque: " + e.getMessage());
        }
    }
} 
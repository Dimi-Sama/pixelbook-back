package com.futuretech.pixelbook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.futuretech.pixelbook.model.*;
import com.futuretech.pixelbook.repository.*;
import com.futuretech.pixelbook.service.JikanService;
import com.futuretech.pixelbook.util.PasswordEncoder;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import com.futuretech.pixelbook.dto.LoginDTO;

import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookshelfRepository bookshelfRepository;

    @Autowired
    private ShopCartRepository shopCartRepository;

    @Autowired
    private MangaRepository mangaRepository;

    @Autowired
    private VolumeRepository volumeRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JikanService jikanService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Bookshelf testBookshelf;
    private ShopCart testShopCart;
    private Manga testManga;
    private Volume testVolume;

    @BeforeEach
    void setUp() {
        // Supprimer toutes les données existantes dans le bon ordre pour éviter les violations de contraintes
        contentRepository.deleteAll();
        
        // Supprimer les relations entre ShopCart et Volume
        entityManager.createNativeQuery("DELETE FROM shop_cart_volume").executeUpdate();
        
        // Ensuite supprimer les entités
        shopCartRepository.deleteAll();
        volumeRepository.deleteAll();
        mangaRepository.deleteAll();
        bookshelfRepository.deleteAll();
        userRepository.deleteAll();

        // Créer un utilisateur
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setCreatedAt(new Date());
        testUser = userRepository.save(testUser);

        // Créer une bibliothèque
        testBookshelf = new Bookshelf();
        testBookshelf.setUser(testUser);
        testBookshelf = bookshelfRepository.save(testBookshelf);

        // Créer un panier
        testShopCart = new ShopCart();
        testShopCart.setUser(testUser);
        testShopCart = shopCartRepository.save(testShopCart);

        // Créer un manga
        testManga = new Manga();
        testManga.setTitle("Test Manga");
        testManga.setAuthor("Test Author");
        testManga.setSynopsis("Test Synopsis");
        testManga.setMalId(12345L);
        testManga = mangaRepository.save(testManga);

        // Créer un volume
        testVolume = new Volume();
        testVolume.setTitle("Test Volume");
        testVolume.setNumber(1);
        testVolume.setPrice(9.99);
        testVolume.setManga(testManga);
        testVolume = volumeRepository.save(testVolume);

        // Ajouter le volume au panier via une requête SQL directe
        entityManager.createNativeQuery(
                "INSERT INTO shop_cart_volume (shop_cart_id, volume_id) VALUES (?, ?)")
                .setParameter(1, testShopCart.getId())
                .setParameter(2, testVolume.getId())
                .executeUpdate();
        
        // Vider le contexte de persistance pour éviter les problèmes de cache
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testGetAllUsers() throws Exception {
        // Utiliser une requête qui ne charge pas les collections
        mockMvc.perform(get("/api/users")
                .param("fetchMode", "basic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$[0].bookshelf").doesNotExist()) // Vérifier que bookshelf n'est pas inclus
                .andExpect(jsonPath("$[0].shopCart").doesNotExist()); // Vérifier que shopCart n'est pas inclus
    }

    @Test
    void testGetUserById() throws Exception {
        // Utiliser une requête qui ne charge pas les collections lazy
        mockMvc.perform(get("/api/users/{id}", testUser.getId())
                .param("fetchMode", "basic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.email", is(testUser.getEmail())))
                .andExpect(jsonPath("$.bookshelf").doesNotExist()) // Vérifier que bookshelf n'est pas inclus
                .andExpect(jsonPath("$.shopCart").doesNotExist()); // Vérifier que shopCart n'est pas inclus
    }

    @Test
    void testGetUserByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateUser() throws Exception {
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("new@example.com")));
    }

    @Test
    void testCreateUserConflict() throws Exception {
        User conflictUser = new User();
        conflictUser.setEmail("test@example.com"); // Email déjà utilisé

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conflictUser)))
                .andExpect(status().isConflict());
    }

    @Test
    void testUpdateUser() throws Exception {
        testUser.setEmail("updated@example.com");

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("updated@example.com")));
    }

    @Test
    void testUpdateUserNotFound() throws Exception {
        User nonExistentUser = new User();
        nonExistentUser.setId(999999L);
        nonExistentUser.setEmail("nonexistent@example.com");

        mockMvc.perform(put("/api/users/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonExistentUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteUserNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddVolumeToUserBookshelf() throws Exception {
        // Créer un nouveau volume pour le test
        Volume newVolume = new Volume();
        newVolume.setTitle("New Test Volume");
        newVolume.setNumber(2);
        newVolume.setPrice(12.99);
        newVolume.setManga(testManga);
        newVolume = volumeRepository.save(newVolume);

        mockMvc.perform(post("/api/users/{userId}/bookshelf/volume/{volumeId}", 
                testUser.getId(), newVolume.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.volume.id", is(newVolume.getId().intValue())));
    }

    @Test
    void testAddVolumeToUserBookshelfConflict() throws Exception {
        // Ajouter d'abord le volume à la bibliothèque
        Content content = new Content();
        content.setBookshelf(testBookshelf);
        content.setVolume(testVolume);
        content.setAddedAt(new Date());
        contentRepository.save(content);

        mockMvc.perform(post("/api/users/{userId}/bookshelf/volume/{volumeId}", 
                testUser.getId(), testVolume.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    void testAddVolumeToUserBookshelfNotFound() throws Exception {
        mockMvc.perform(post("/api/users/999999/bookshelf/volume/{volumeId}", 
                testVolume.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddVolumeToUserShopCart() throws Exception {
        // Créer un nouveau volume pour le test
        Volume newVolume = new Volume();
        newVolume.setTitle("New Test Volume");
        newVolume.setNumber(2);
        newVolume.setPrice(12.99);
        newVolume.setManga(testManga);
        newVolume = volumeRepository.save(newVolume);

        // Vider le contexte de persistance
        entityManager.flush();
        entityManager.clear();

        // Vérifier que l'endpoint est correct - peut-être que c'est /api/users/{userId}/shopcart/volume/{volumeId}
        mockMvc.perform(post("/api/users/{userId}/shopcart/volume/{volumeId}", 
                testUser.getId(), newVolume.getId())
                .param("fetchMode", "basic"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @Transactional
    void testCheckoutShopCart() throws Exception {
        mockMvc.perform(post("/api/users/{userId}/shopcart/checkout", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("validé avec succès")));

        // Vérifier que le panier est vide
        mockMvc.perform(get("/api/users/{userId}/shopcart", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.volumes", hasSize(0)));

        // Vérifier que le volume est dans la bibliothèque
        mockMvc.perform(get("/api/users/{userId}/bookshelf", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contents", hasSize(1)))
                .andExpect(jsonPath("$.contents[0].volume.id", is(testVolume.getId().intValue())));
    }

    @Test
    void testGetUserShopCart() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/shopcart", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.volumes", hasSize(1)))
                .andExpect(jsonPath("$.volumes[0].id", is(testVolume.getId().intValue())));
    }

    @Test
    void testGetUserShopCartNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999999/shopcart"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUserBookshelf() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/bookshelf", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testBookshelf.getId().intValue())));
    }

    @Test
    void testGetUserBookshelfNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999999/bookshelf"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Créer un utilisateur avec mot de passe haché
        User user = new User();
        user.setEmail("login@example.com");
        String rawPassword = "testPassword123";
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setCreatedAt(new Date());
        userRepository.save(user);

        // Créer le DTO de login
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("login@example.com");
        loginDTO.setPassword(rawPassword);

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("login@example.com")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.password").doesNotExist()); // Le mot de passe ne doit pas être renvoyé
    }

    @Test
    void testLoginWrongPassword() throws Exception {
        // Créer un utilisateur avec mot de passe haché
        User user = new User();
        user.setEmail("login@example.com");
        user.setPassword(passwordEncoder.encode("correctPassword"));
        user.setCreatedAt(new Date());
        userRepository.save(user);

        // Créer le DTO de login avec mauvais mot de passe
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("login@example.com");
        loginDTO.setPassword("wrongPassword");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginUserNotFound() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("nonexistent@example.com");
        loginDTO.setPassword("anyPassword");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateUserWithHashedPassword() throws Exception {
        User newUser = new User();
        newUser.setEmail("newhashed@example.com");
        String rawPassword = "password123";
        newUser.setPassword(rawPassword);

        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("newhashed@example.com")))
                .andReturn();

        // Vérifier que le mot de passe est bien haché dans la base de données
        String responseJson = result.getResponse().getContentAsString();
        User createdUser = objectMapper.readValue(responseJson, User.class);
        User savedUser = userRepository.findById(createdUser.getId()).orElseThrow();
        
        assertNotEquals(rawPassword, savedUser.getPassword());
        assertTrue(passwordEncoder.matches(rawPassword, savedUser.getPassword()));
    }
} 
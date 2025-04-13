package com.futuretech.pixelbook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futuretech.pixelbook.model.Bookshelf;
import com.futuretech.pixelbook.model.Content;
import com.futuretech.pixelbook.model.User;
import com.futuretech.pixelbook.model.Volume;
import com.futuretech.pixelbook.model.Manga;
import com.futuretech.pixelbook.repository.BookshelfRepository;
import com.futuretech.pixelbook.repository.ContentRepository;
import com.futuretech.pixelbook.repository.VolumeRepository;
import com.futuretech.pixelbook.repository.MangaRepository;
import com.futuretech.pixelbook.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private BookshelfRepository bookshelfRepository;

    @Autowired
    private VolumeRepository volumeRepository;
    
    @Autowired
    private MangaRepository mangaRepository;
    
    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Bookshelf testBookshelf;
    private Manga testManga;
    private Volume testVolume;
    private Content testContent;

    @BeforeEach
    void setUp() {
        // Nettoyer les données
        contentRepository.deleteAll();
        volumeRepository.deleteAll();
        bookshelfRepository.deleteAll();
        mangaRepository.deleteAll();
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

        // Créer un contenu
        testContent = new Content();
        testContent.setBookshelf(testBookshelf);
        testContent.setVolume(testVolume);
        testContent.setAddedAt(new Date());
        testContent = contentRepository.save(testContent);
    }

    @Test
    void testGetAllContents() throws Exception {
        mockMvc.perform(get("/api/contents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id", is(testContent.getId().intValue())));
    }

    @Test
    void testGetContentById() throws Exception {
        mockMvc.perform(get("/api/contents/{id}", testContent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testContent.getId().intValue())));
    }

    @Test
    void testGetContentByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/contents/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetContentsByBookshelfId() throws Exception {
        mockMvc.perform(get("/api/contents/bookshelf/{bookshelfId}", testBookshelf.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").exists())
                .andDo(result -> {
                    System.out.println("Response: " + result.getResponse().getContentAsString());
                });
    }

    @Test
    void testAddVolumeToBookshelf() throws Exception {
        // Créer un nouveau volume qui n'est pas encore dans la bibliothèque
        Volume newVolume = new Volume();
        newVolume.setTitle("New Test Volume");
        newVolume.setNumber(2);
        newVolume.setPrice(12.99);
        newVolume.setManga(testManga);
        newVolume = volumeRepository.save(newVolume);

        // Maintenant utiliser ce nouveau volume pour le test
        mockMvc.perform(post("/api/contents/bookshelf/{bookshelfId}/volume/{volumeId}", 
                testBookshelf.getId(), newVolume.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andDo(result -> {
                    System.out.println("Response: " + result.getResponse().getContentAsString());
                });
    }

    @Test
    void testAddVolumeToBookshelfConflict() throws Exception {
        mockMvc.perform(post("/api/contents/bookshelf/{bookshelfId}/volume/{volumeId}", 
                testBookshelf.getId(), testVolume.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    void testAddVolumeToBookshelfBadRequest() throws Exception {
        mockMvc.perform(post("/api/contents/bookshelf/999999/volume/{volumeId}", testVolume.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRemoveContent() throws Exception {
        mockMvc.perform(delete("/api/contents/{id}", testContent.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/contents/{id}", testContent.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRemoveContentNotFound() throws Exception {
        mockMvc.perform(delete("/api/contents/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRemoveVolumeFromBookshelf() throws Exception {
        mockMvc.perform(delete("/api/contents/bookshelf/{bookshelfId}/volume/{volumeId}", 
                testBookshelf.getId(), testVolume.getId()))
                .andExpect(status().isOk());

        // Vérifier que le contenu n'existe plus
        boolean contentExists = contentRepository.existsByBookshelfIdAndVolumeId(
                testBookshelf.getId(), testVolume.getId());
        assert(!contentExists);
    }

    @Test
    void testRemoveVolumeFromBookshelfNotFound() throws Exception {
        mockMvc.perform(delete("/api/contents/bookshelf/{bookshelfId}/volume/999999", 
                testBookshelf.getId()))
                .andExpect(status().isNotFound());
    }
} 
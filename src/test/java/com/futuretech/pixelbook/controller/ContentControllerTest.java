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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContentRepository contentRepository;

    @MockBean
    private BookshelfRepository bookshelfRepository;

    @MockBean
    private VolumeRepository volumeRepository;
    
    @MockBean
    private MangaRepository mangaRepository;
    
    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private Bookshelf testBookshelf;
    private Manga testManga;
    private Volume testVolume;
    private Content testContent;

    @BeforeEach
    void setUp() {
        // Créer les objets de test
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setCreatedAt(new Date());

        testBookshelf = new Bookshelf();
        testBookshelf.setId(1L);
        testBookshelf.setUser(testUser);

        testVolume = new Volume();
        testVolume.setId(1L);
        testVolume.setTitle("Test Volume");
        testVolume.setNumber(1);

        testContent = new Content();
        testContent.setId(1L);
        testContent.setBookshelf(testBookshelf);
        testContent.setVolume(testVolume);
        testContent.setRead(false);
        testContent.setAddedAt(new Date());

        // Configuration des mocks
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookshelfRepository.findById(1L)).thenReturn(Optional.of(testBookshelf));
        when(volumeRepository.findById(1L)).thenReturn(Optional.of(testVolume));
        when(contentRepository.findById(1L)).thenReturn(Optional.of(testContent));
        when(contentRepository.findAll()).thenReturn(List.of(testContent));
        when(contentRepository.findByBookshelfId(1L)).thenReturn(List.of(testContent));
        when(bookshelfRepository.findByUserId(1L)).thenReturn(Optional.of(testBookshelf));
        when(contentRepository.findByBookshelfIdAndVolumeId(1L, 1L)).thenReturn(Optional.of(testContent));
        when(contentRepository.save(any(Content.class))).thenReturn(testContent);
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
        // Créer un nouveau volume pour le test
        Volume newVolume = new Volume();
        newVolume.setId(2L);
        newVolume.setTitle("New Test Volume");
        newVolume.setNumber(2);
        
        // Mock pour le nouveau volume
        when(volumeRepository.save(any(Volume.class))).thenReturn(newVolume);
        when(volumeRepository.findById(2L)).thenReturn(Optional.of(newVolume));
        
        // Mock pour éviter le conflit (pas de contenu existant)
        when(contentRepository.findByBookshelfIdAndVolumeId(1L, 2L))
            .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/contents/bookshelf/1/volume/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testAddVolumeToBookshelfBadRequest() throws Exception {
        mockMvc.perform(post("/api/contents/bookshelf/999999/volume/{volumeId}", testVolume.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRemoveContent() throws Exception {
        // Mock pour la suppression
        when(contentRepository.findById(1L)).thenReturn(Optional.of(testContent));
        
        mockMvc.perform(delete("/api/contents/1"))
                .andExpect(status().isOk());

        // Simuler que le contenu n'existe plus après la suppression
        when(contentRepository.findById(1L)).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/contents/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRemoveContentNotFound() throws Exception {
        mockMvc.perform(delete("/api/contents/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRemoveVolumeFromBookshelf() throws Exception {
        // Configuration plus complète des mocks
        when(bookshelfRepository.findById(1L)).thenReturn(Optional.of(testBookshelf));
        when(volumeRepository.findById(1L)).thenReturn(Optional.of(testVolume));
        when(contentRepository.findByBookshelfIdAndVolumeId(1L, 1L))
            .thenReturn(Optional.of(testContent));
        
        // Mock pour la vérification de l'existence
        when(contentRepository.existsByBookshelfIdAndVolumeId(1L, 1L))
            .thenReturn(true);  // Le contenu existe avant la suppression

        mockMvc.perform(delete("/api/contents/bookshelf/1/volume/1"))
                .andExpect(status().isOk())
                .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()));
    }

    @Test
    void testRemoveVolumeFromBookshelfNotFound() throws Exception {
        mockMvc.perform(delete("/api/contents/bookshelf/{bookshelfId}/volume/999999", 
                testBookshelf.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateReadStatus() throws Exception {
        mockMvc.perform(put("/api/contents/bookshelf/1/volume/1/read/true")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void testGetReadStatus() throws Exception {
        testContent.setRead(true);
        testContent.setReadAt(new Date());

        mockMvc.perform(get("/api/contents/bookshelf/1/volume/1/read")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.readAt").exists());
    }

    @Test
    void testGetUserReadStatus() throws Exception {
        mockMvc.perform(get("/api/contents/user/1/volume/1/read")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(false));
    }

    @Test
    void testGetUserReadStatusBookshelfNotFound() throws Exception {
        when(bookshelfRepository.findByUserId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/contents/user/1/volume/1/read")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUserReadStatusContentNotFound() throws Exception {
        when(contentRepository.findByBookshelfIdAndVolumeId(1L, 1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/contents/user/1/volume/1/read")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
} 
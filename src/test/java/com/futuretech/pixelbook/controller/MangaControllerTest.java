package com.futuretech.pixelbook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futuretech.pixelbook.model.Manga;
import com.futuretech.pixelbook.repository.MangaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class MangaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MangaRepository mangaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Manga testManga;

    @BeforeEach
    void setUp() {
        mangaRepository.deleteAll();

        testManga = new Manga();
        testManga.setTitle("One Piece");
        testManga.setAuthor("Eiichiro Oda");
        testManga.setSynopsis("L'histoire du pirate qui deviendra le roi des pirates");
        testManga.setMalId(21L);
        testManga = mangaRepository.save(testManga);

        // Ajouter un second manga pour les tests de recherche
        Manga secondManga = new Manga();
        secondManga.setTitle("Naruto");
        secondManga.setAuthor("Masashi Kishimoto");
        secondManga.setSynopsis("L'histoire d'un jeune ninja");
        secondManga.setMalId(20L);
        mangaRepository.save(secondManga);
    }

    @Test
    void testGetAllMangas() throws Exception {
        mockMvc.perform(get("/api/mangas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].title", notNullValue()));
    }

    @Test
    void testGetMangaById() throws Exception {
        mockMvc.perform(get("/api/mangas/{id}", testManga.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testManga.getId().intValue())))
                .andExpect(jsonPath("$.title", is("One Piece")))
                .andExpect(jsonPath("$.author", is("Eiichiro Oda")));
    }

    @Test
    void testGetMangaByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/mangas/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchMangas() throws Exception {
        mockMvc.perform(get("/api/mangas/search")
                .param("keyword", "ninja"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", is("Naruto")));
    }

    @Test
    void testCreateManga() throws Exception {
        Manga newManga = new Manga();
        newManga.setTitle("Dragon Ball");
        newManga.setAuthor("Akira Toriyama");
        newManga.setSynopsis("L'histoire de Goku");
        newManga.setMalId(223L);

        mockMvc.perform(post("/api/mangas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newManga)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Dragon Ball")))
                .andExpect(jsonPath("$.author", is("Akira Toriyama")));
    }

    @Test
    void testUpdateManga() throws Exception {
        testManga.setTitle("One Piece Updated");
        testManga.setSynopsis("La quête du One Piece");

        mockMvc.perform(put("/api/mangas/{id}", testManga.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testManga)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("One Piece Updated")))
                .andExpect(jsonPath("$.synopsis", is("La quête du One Piece")));
    }

    @Test
    void testUpdateMangaNotFound() throws Exception {
        Manga nonExistentManga = new Manga();
        nonExistentManga.setId(999999L);
        nonExistentManga.setTitle("Non Existent");

        mockMvc.perform(put("/api/mangas/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonExistentManga)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteManga() throws Exception {
        mockMvc.perform(delete("/api/mangas/{id}", testManga.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/mangas/{id}", testManga.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteMangaNotFound() throws Exception {
        mockMvc.perform(delete("/api/mangas/999999"))
                .andExpect(status().isNotFound());
    }
} 
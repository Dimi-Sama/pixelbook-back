package com.futuretech.pixelbook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futuretech.pixelbook.model.Manga;
import com.futuretech.pixelbook.model.Volume;
import com.futuretech.pixelbook.repository.MangaRepository;
import com.futuretech.pixelbook.repository.VolumeRepository;

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
public class VolumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VolumeRepository volumeRepository;

    @Autowired
    private MangaRepository mangaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Manga testManga;
    private Volume testVolume;

    @BeforeEach
    void setUp() {
        volumeRepository.deleteAll();
        mangaRepository.deleteAll();

        // Créer un manga
        testManga = new Manga();
        testManga.setTitle("One Piece");
        testManga.setAuthor("Eiichiro Oda");
        testManga.setSynopsis("L'histoire du pirate qui deviendra le roi des pirates");
        testManga.setMalId(21L);
        testManga = mangaRepository.save(testManga);

        // Créer un volume
        testVolume = new Volume();
        testVolume.setTitle("One Piece Volume 1");
        testVolume.setNumber(1);
        testVolume.setPrice(9.99);
        testVolume.setManga(testManga);
        testVolume = volumeRepository.save(testVolume);

        // Ajouter un second volume
        Volume secondVolume = new Volume();
        secondVolume.setTitle("One Piece Volume 2");
        secondVolume.setNumber(2);
        secondVolume.setPrice(9.99);
        secondVolume.setManga(testManga);
        volumeRepository.save(secondVolume);
    }

    @Test
    void testGetAllVolumes() throws Exception {
        mockMvc.perform(get("/api/volumes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].title", notNullValue()));
    }

    @Test
    void testGetVolumeById() throws Exception {
        mockMvc.perform(get("/api/volumes/{id}", testVolume.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testVolume.getId().intValue())))
                .andExpect(jsonPath("$.title", is("One Piece Volume 1")))
                .andExpect(jsonPath("$.number", is(1)));
    }

    @Test
    void testGetVolumeByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/volumes/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetVolumesByMangaId() throws Exception {
        mockMvc.perform(get("/api/volumes/manga/{mangaId}", testManga.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].manga.id", is(testManga.getId().intValue())));
    }

    @Test
    void testCreateVolume() throws Exception {
        Volume newVolume = new Volume();
        newVolume.setTitle("One Piece Volume 3");
        newVolume.setNumber(3);
        newVolume.setPrice(9.99);
        newVolume.setManga(testManga);

        mockMvc.perform(post("/api/volumes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newVolume)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("One Piece Volume 3")))
                .andExpect(jsonPath("$.number", is(3)));
    }

    @Test
    void testCreateVolumeForManga() throws Exception {
        Volume newVolume = new Volume();
        newVolume.setTitle("One Piece Volume 4");
        newVolume.setNumber(4);
        newVolume.setPrice(9.99);

        mockMvc.perform(post("/api/volumes/manga/{mangaId}", testManga.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newVolume)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("One Piece Volume 4")))
                .andExpect(jsonPath("$.number", is(4)))
                .andExpect(jsonPath("$.manga.id", is(testManga.getId().intValue())));
    }

    @Test
    void testCreateVolumeForMangaNotFound() throws Exception {
        Volume newVolume = new Volume();
        newVolume.setTitle("Non Existent Manga Volume");
        newVolume.setNumber(1);
        newVolume.setPrice(9.99);

        mockMvc.perform(post("/api/volumes/manga/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newVolume)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateVolume() throws Exception {
        testVolume.setTitle("One Piece Volume 1 Updated");
        testVolume.setPrice(12.99);

        mockMvc.perform(put("/api/volumes/{id}", testVolume.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testVolume)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("One Piece Volume 1 Updated")))
                .andExpect(jsonPath("$.price", is(12.99)));
    }

    @Test
    void testUpdateVolumeNotFound() throws Exception {
        Volume nonExistentVolume = new Volume();
        nonExistentVolume.setId(999999L);
        nonExistentVolume.setTitle("Non Existent");

        mockMvc.perform(put("/api/volumes/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonExistentVolume)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteVolume() throws Exception {
        mockMvc.perform(delete("/api/volumes/{id}", testVolume.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/volumes/{id}", testVolume.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteVolumeNotFound() throws Exception {
        mockMvc.perform(delete("/api/volumes/999999"))
                .andExpect(status().isNotFound());
    }
} 
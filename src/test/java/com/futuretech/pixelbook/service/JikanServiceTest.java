package com.futuretech.pixelbook.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.futuretech.pixelbook.repository.MangaRepository;
import com.futuretech.pixelbook.repository.VolumeRepository;
import com.futuretech.pixelbook.model.Manga;
import org.springframework.core.ParameterizedTypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JikanServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MangaRepository mangaRepository;

    @Mock
    private VolumeRepository volumeRepository;

    @InjectMocks
    private JikanService jikanService;

    @Test
    void testSearchMangas() {
        // Préparation des données de test
        Map<String, Object> mockResponse = new HashMap<>();
        List<Map<String, Object>> mockData = new ArrayList<>();
        Map<String, Object> mockManga = new HashMap<>();
        mockManga.put("mal_id", 1);
        mockManga.put("title", "One Piece");
        mockManga.put("volumes", "100");
        
        Map<String, Object> images = new HashMap<>();
        Map<String, Object> jpg = new HashMap<>();
        jpg.put("large_image_url", "http://example.com/image.jpg");
        images.put("jpg", jpg);
        mockManga.put("images", images);
        
        mockData.add(mockManga);
        mockResponse.put("data", mockData);

        // Configuration du mock
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Exécution du test
        List<Map<String, Object>> result = jikanService.searchMangas("one piece");

        // Vérifications
        assertNotNull(result);
        assertEquals(1, result.size());
        Map<String, Object> resultManga = result.get(0);
        assertEquals(1L, ((Number) resultManga.get("mal_id")).longValue());
        assertEquals("One Piece", resultManga.get("title"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> volumes = (List<Map<String, Object>>) resultManga.get("volumesList");
        assertNotNull(volumes);
        assertEquals(100, volumes.size());
    }

    @Test
    void testFetchMangaDetails() {
        // Préparation des données de test
        Long malId = 1L;
        Map<String, Object> mockResponse = new HashMap<>();
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("title", "One Piece");
        mockData.put("synopsis", "L'histoire d'un pirate");
        
        Map<String, Object> images = new HashMap<>();
        Map<String, Object> jpg = new HashMap<>();
        jpg.put("large_image_url", "http://example.com/image.jpg");
        images.put("jpg", jpg);
        mockData.put("images", images);
        
        mockResponse.put("data", mockData);

        // Configuration du mock
        when(restTemplate.exchange(
            eq("https://api.jikan.moe/v4/manga/" + malId + "/full"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Exécution du test
        Manga result = jikanService.fetchMangaDetails(malId);

        // Vérifications
        assertNotNull(result);
        assertEquals("One Piece", result.getTitle());
        assertEquals("L'histoire d'un pirate", result.getSynopsis());
        assertEquals("http://example.com/image.jpg", result.getCoverUrl());
    }

    @Test
    void testGetPopularMangas() {
        // Préparation des données de test
        Map<String, Object> mockResponse = new HashMap<>();
        List<Map<String, Object>> mockData = new ArrayList<>();
        Map<String, Object> mockManga = new HashMap<>();
        mockManga.put("mal_id", 1);
        mockManga.put("title", "One Piece");
        mockManga.put("volumes", "100");
        
        Map<String, Object> images = new HashMap<>();
        Map<String, Object> jpg = new HashMap<>();
        jpg.put("large_image_url", "http://example.com/image.jpg");
        images.put("jpg", jpg);
        mockManga.put("images", images);
        
        mockData.add(mockManga);
        mockResponse.put("data", mockData);

        // Configuration du mock
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Exécution du test
        List<Map<String, Object>> result = jikanService.getPopularMangas(1, 10);

        // Vérifications
        assertNotNull(result);
        assertEquals(1, result.size());
        Map<String, Object> resultManga = result.get(0);
        assertEquals("One Piece", resultManga.get("mangaTitle"));
        assertEquals(9.99, resultManga.get("price"));
    }
} 
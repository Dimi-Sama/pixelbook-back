package com.futuretech.pixelbook.service;

import com.futuretech.pixelbook.model.Manga;
import com.futuretech.pixelbook.model.Volume;
import com.futuretech.pixelbook.repository.MangaRepository;
import com.futuretech.pixelbook.repository.VolumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class JikanService {
    
    private static final String JIKAN_BASE_URL = "https://api.jikan.moe/v4";
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private MangaRepository mangaRepository;
    
    @Autowired
    private VolumeRepository volumeRepository;
    
    /**
     * Recherche des mangas par mot-clé et inclut les informations sur les volumes
     */
    public List<Map<String, Object>> searchMangas(String query) {
        String url = JIKAN_BASE_URL + "/manga?q=" + query;
        
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("data")) {
            return new ArrayList<>();
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
        
        // Enrichir les résultats avec des informations sur les volumes
        for (Map<String, Object> mangaData : data) {
            // Récupérer le mal_id du manga
            Long malId = ((Number) mangaData.get("mal_id")).longValue();
            
            // Récupérer le nombre de volumes depuis les données de l'API
            Integer volumeCount = 1; // Par défaut, au moins 1 volume
            if (mangaData.containsKey("volumes") && mangaData.get("volumes") != null) {
                try {
                    volumeCount = Integer.parseInt(mangaData.get("volumes").toString());
                } catch (NumberFormatException e) {
                    // En cas d'erreur, on garde la valeur par défaut
                }
            }
            
            // Créer une liste de volumes simplifiée pour l'affichage
            List<Map<String, Object>> volumesList = new ArrayList<>();
            for (int i = 1; i <= volumeCount; i++) {
                Map<String, Object> volumeInfo = new HashMap<>();
                volumeInfo.put("number", i);
                volumeInfo.put("title", mangaData.get("title") + " Volume " + i);
                
                // Récupérer l'URL de l'image du manga pour l'utiliser comme couverture du volume
                @SuppressWarnings("unchecked")
                Map<String, Object> images = (Map<String, Object>) mangaData.get("images");
                if (images != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> jpg = (Map<String, Object>) images.get("jpg");
                    if (jpg != null) {
                        volumeInfo.put("coverUrl", jpg.get("large_image_url"));
                    }
                }
                
                // Générer un ISBN fictif mais réaliste
                volumeInfo.put("isbn", "978-" + (1000000000 + malId * 100 + i));
                
                // Estimer le nombre de pages (entre 150 et 250 pages)
                volumeInfo.put("pageCount", 150 + (int)(Math.random() * 100));
                
                // Définir un prix standard
                volumeInfo.put("price", 9.99);
                
                volumesList.add(volumeInfo);
            }
            
            // Ajouter la liste des volumes au résultat du manga
            mangaData.put("volumesList", volumesList);
        }
        
        return data;
    }
    
    /**
     * Récupère les mangas populaires avec un volume par manga
     */
    public List<Map<String, Object>> getPopularMangas(int page, int limit) {
        String url = JIKAN_BASE_URL + "/top/manga?page=" + page + "&limit=" + limit;
        
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("data")) {
            return new ArrayList<>();
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
        
        // Créer une liste pour stocker les résultats transformés
        List<Map<String, Object>> transformedResults = new ArrayList<>();
        
        // Transformer chaque manga en un volume
        for (Map<String, Object> mangaData : data) {
            // Récupérer le mal_id du manga
            Long malId = ((Number) mangaData.get("mal_id")).longValue();
            
            // Récupérer le nombre de volumes depuis les données de l'API
            Integer volumeCount = 1; // Par défaut, au moins 1 volume
            if (mangaData.containsKey("volumes") && mangaData.get("volumes") != null) {
                try {
                    volumeCount = Integer.parseInt(mangaData.get("volumes").toString());
                } catch (NumberFormatException e) {
                    // En cas d'erreur, on garde la valeur par défaut
                }
            }
            
            // Choisir un volume aléatoire (ou le dernier)
            int volumeNumber = volumeCount; // Dernier volume
            
            // Créer un objet volume basé sur les données du manga
            Map<String, Object> volumeInfo = new HashMap<>();
            volumeInfo.put("malId", malId);
            volumeInfo.put("mangaTitle", mangaData.get("title"));
            volumeInfo.put("number", volumeNumber);
            volumeInfo.put("title", mangaData.get("title") + " Volume " + volumeNumber);
            
            // Récupérer l'URL de l'image du manga pour l'utiliser comme couverture du volume
            @SuppressWarnings("unchecked")
            Map<String, Object> images = (Map<String, Object>) mangaData.get("images");
            if (images != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> jpg = (Map<String, Object>) images.get("jpg");
                if (jpg != null) {
                    volumeInfo.put("coverUrl", jpg.get("large_image_url"));
                }
            }
            
            // Générer un ISBN fictif mais réaliste
            volumeInfo.put("isbn", "978-" + (1000000000 + malId * 100 + volumeNumber));
            
            // Estimer le nombre de pages (entre 150 et 250 pages)
            volumeInfo.put("pageCount", 150 + (int)(Math.random() * 100));
            
            // Définir un prix standard
            volumeInfo.put("price", 9.99);
            
            // Ajouter des informations supplémentaires du manga
            volumeInfo.put("synopsis", mangaData.get("synopsis"));
            volumeInfo.put("score", mangaData.get("score"));
            volumeInfo.put("status", mangaData.get("status"));
            
            // Ajouter le volume à la liste des résultats
            transformedResults.add(volumeInfo);
        }
        
        return transformedResults;
    }
    
    /**
     * Récupère les détails d'un manga spécifique
     */
    public Manga fetchMangaDetails(Long malId) {
        String url = JIKAN_BASE_URL + "/manga/" + malId + "/full";
        
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("data")) {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
        
        Manga manga = new Manga();
        manga.setMalId(malId);
        manga.setTitle((String) data.get("title"));
        
        
        // Récupérer l'URL de l'image
        @SuppressWarnings("unchecked")
        Map<String, Object> images = (Map<String, Object>) data.get("images");
        if (images != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> jpg = (Map<String, Object>) images.get("jpg");
            if (jpg != null) {
                manga.setCoverUrl((String) jpg.get("large_image_url"));
            }
        }
        
        manga.setSynopsis((String) data.get("synopsis"));
        
        return manga;
    }
    
    /**
     * Récupère les détails des tomes d'un manga et crée les entités Volume
     */
    public List<Volume> fetchMangaVolumes(Long malId) {
        try {
            // Récupérer le manga depuis l'API
            Manga mangaFromApi = fetchMangaDetails(malId);
            if (mangaFromApi == null) {
                return new ArrayList<>();
            }
            
            // Vérifier si le manga existe déjà en base de données
            Manga mangaToUse = mangaRepository.findByMalId(malId)
                    .orElseGet(() -> mangaRepository.save(mangaFromApi));
            
            // Récupérer les détails complets du manga depuis l'API Jikan
            String url = JIKAN_BASE_URL + "/manga/" + malId + "/full";
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("data")) {
                return new ArrayList<>();
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            
            // Récupérer le nombre total de volumes
            Integer totalVolumes = 1; // Par défaut, au moins 1 volume
            if (data.containsKey("volumes") && data.get("volumes") != null) {
                try {
                    totalVolumes = Integer.parseInt(data.get("volumes").toString());
                } catch (NumberFormatException e) {
                    // En cas d'erreur, on garde la valeur par défaut
                }
            }
            
            // Récupérer les dates de publication pour estimer les dates de sortie des volumes
            String startDate = null;
            String endDate = null;
            
            if (data.containsKey("published") && data.get("published") != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> published = (Map<String, Object>) data.get("published");
                
                if (published.containsKey("from") && published.get("from") != null) {
                    startDate = published.get("from").toString().substring(0, 10); // Format YYYY-MM-DD
                }
                
                if (published.containsKey("to") && published.get("to") != null) {
                    endDate = published.get("to").toString().substring(0, 10); // Format YYYY-MM-DD
                }
            }
            
            // Créer les volumes avec des informations plus détaillées
            List<Volume> volumes = new ArrayList<>();
            
            for (int i = 1; i <= totalVolumes; i++) {
                // Vérifier si le volume existe déjà en base de données
                Optional<Volume> existingVolume = volumeRepository.findByMangaAndNumber(mangaToUse, i);
                
                if (existingVolume.isPresent()) {
                    volumes.add(existingVolume.get());
                    continue;
                }
                
                Volume volume = new Volume();
                volume.setNumber(i);
                volume.setTitle(mangaToUse.getTitle() + " Volume " + i);
                volume.setMalId(malId);
                volume.setCoverUrl(mangaToUse.getCoverUrl()); // Utiliser la même image pour tous les volumes
                volume.setManga(mangaToUse);
                
                // Générer un ISBN fictif mais réaliste
                volume.setIsbn("978-" + (1000000000 + malId * 100 + i));
                
                // Estimer le nombre de pages (entre 150 et 250 pages)
                volume.setPageCount(150 + (int)(Math.random() * 100));
                
                // Définir un prix standard
                volume.setPrice(9.99);
                
                // Estimer la date de sortie en fonction des dates de publication
                if (startDate != null && endDate != null) {
                    try {
                        // Calculer la durée entre le début et la fin de publication
                        java.time.LocalDate start = java.time.LocalDate.parse(startDate);
                        java.time.LocalDate end = java.time.LocalDate.parse(endDate);
                        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                        
                        // Répartir les volumes uniformément sur cette période
                        long daysPerVolume = totalDays / totalVolumes;
                        java.time.LocalDate volumeDate = start.plusDays(daysPerVolume * (i - 1));
                        
                        volume.setReleaseDate(volumeDate.toString());
                    } catch (Exception e) {
                        // En cas d'erreur, utiliser une date approximative
                        if (startDate != null) {
                            volume.setReleaseDate(startDate);
                        }
                    }
                }
                
                volumes.add(volume);
            }
            
            return volumes;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public Volume fetchVolumeDetails(Long malId, Integer volumeNumber) {
        try {
            // D'abord, récupérer les informations du manga
            Manga manga = fetchMangaDetails(malId);
            if (manga == null) {
                return null;
            }
            
            // Vérifier si le volume demandé est dans la plage valide
            String url = JIKAN_BASE_URL + "/manga/" + malId + "/full";
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("data")) {
                return null;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            
            // Récupérer le nombre total de volumes
            Integer totalVolumes = 1; // Par défaut, au moins 1 volume
            if (data.containsKey("volumes") && data.get("volumes") != null) {
                try {
                    totalVolumes = Integer.parseInt(data.get("volumes").toString());
                } catch (NumberFormatException e) {
                    // En cas d'erreur, on garde la valeur par défaut
                }
            }
            
            // Vérifier si le volume demandé existe
            if (volumeNumber > totalVolumes) {
                return null;
            }
            
            // Créer le volume avec les informations disponibles
            Volume volume = new Volume();
            volume.setNumber(volumeNumber);
            volume.setMalId(malId);
            volume.setTitle(manga.getTitle() + " - Volume " + volumeNumber);
            volume.setCoverUrl(manga.getCoverUrl());
            
            // Générer un ISBN fictif mais réaliste
            volume.setIsbn("978-" + (1000000000 + malId * 100 + volumeNumber));
            
            // Estimer le nombre de pages (entre 150 et 250 pages)
            volume.setPageCount(150 + (int)(Math.random() * 100));
            
            // Définir un prix standard
            volume.setPrice(9.99);
            
            // Estimer une date de sortie basée sur les dates du manga
            if (data.containsKey("published") && data.get("published") != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> published = (Map<String, Object>) data.get("published");
                if (published.containsKey("string")) {
                    String publishedString = published.get("string").toString();
                    // Extraire l'année de début
                    if (publishedString.length() > 4) {
                        try {
                            int startYear = Integer.parseInt(publishedString.substring(publishedString.length() - 4));
                            // Estimer la date de sortie du volume (ajouter quelques mois par volume)
                            volume.setReleaseDate(startYear + "-" + 
                                    String.format("%02d", (1 + volumeNumber % 12)) + "-01");
                        } catch (Exception e) {
                            // En cas d'erreur, pas de date
                        }
                    }
                }
            }
            
            return volume;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
} 
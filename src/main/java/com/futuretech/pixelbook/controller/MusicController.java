package com.futuretech.pixelbook.controller;


import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

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
@RestController
@RequestMapping("/api/music")
public class MusicController {
    private static final Logger logger = LoggerFactory.getLogger(MusicController.class);

    @PostMapping(value = "/download", produces = "audio/mpeg")
    public ResponseEntity<Resource> downloadMusic(@RequestBody Map<String, String> body) throws IOException, InterruptedException {
        logger.info("Requête reçue pour télécharger: {}", body);
        
        String url = body.get("url");
        if (url == null || url.isEmpty()) {
            logger.error("URL manquante ou vide");
            return ResponseEntity.badRequest().build();
        }
        
        logger.info("URL valide: {}", url);

        // Générer un nom de fichier unique basé sur l'URL et le timestamp
        String uniqueId = String.valueOf(System.currentTimeMillis());
        String videoId = extractVideoId(url);
        if (videoId != null) {
            uniqueId = videoId + "_" + uniqueId;
        }
        
        String filename = "track_" + uniqueId + ".mp3";
        String webmFilename = "track_" + uniqueId + ".webm";
        
        logger.info("Nom de fichier unique généré: {}", filename);

        // Créer un dossier avec un chemin absolu
        String workingDir = System.getProperty("user.dir");
        File output = new File(workingDir + "/downloads/" + filename);
        File webmFile = new File(workingDir + "/downloads/" + webmFilename);
        output.getParentFile().mkdirs();
        
        logger.info("Dossier de sortie créé: {}", output.getParentFile().getAbsolutePath());

        // Chemin vers yt-dlp selon l'OS
        String os = System.getProperty("os.name").toLowerCase();
        String ytDlpPath;
        String ffmpegPath;
        
        if (os.contains("win")) {
            // Windows - utilise yt-dlp.exe
            ytDlpPath = workingDir + "/yt-dlp.exe";
            ffmpegPath = workingDir + "/ffmpeg/bin/ffmpeg.exe";
        } else {
            // Linux/Mac - utilise yt-dlp depuis /usr/local/bin
            ytDlpPath = "/usr/local/bin/yt-dlp";
            ffmpegPath = "/usr/local/bin/ffmpeg";
        }
        
        logger.info("Chemin vers yt-dlp: {}", ytDlpPath);
        
        // Vérifier que yt-dlp existe
        File ytDlpFile = new File(ytDlpPath);
        if (!ytDlpFile.exists()) {
            logger.error("yt-dlp n'existe pas à l'emplacement: {}", ytDlpPath);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ByteArrayResource("yt-dlp non trouvé".getBytes()));
        }
        
        // Vérifier si FFmpeg existe
        File ffmpegFile = new File(ffmpegPath);
        if (!ffmpegFile.exists()) {
            logger.error("FFmpeg non trouvé à : {}", ffmpegFile.getAbsolutePath());
        } else {
            logger.info("FFmpeg trouvé à : {}", ffmpegFile.getAbsolutePath());
        }
        
        // Télécharger d'abord le fichier sans conversion
        ProcessBuilder downloadProcessBuilder;
        
        if (os.contains("win")) {
            // Windows - utilise cmd
            String downloadCommand = String.format("\"%s\" -o \"%s\" %s", 
                                          ytDlpPath, webmFile.getAbsolutePath(), url);
            logger.info("Commande de téléchargement Windows : {}", downloadCommand);
            downloadProcessBuilder = new ProcessBuilder("cmd", "/c", downloadCommand);
        } else {
            // Linux/Mac - utilise directement la commande
            logger.info("Commande de téléchargement Linux : {} -o {} {}", ytDlpPath, webmFile.getAbsolutePath(), url);
            downloadProcessBuilder = new ProcessBuilder(ytDlpPath, "-o", webmFile.getAbsolutePath(), url);
        }
        downloadProcessBuilder.redirectErrorStream(true);
        Process downloadProcess = downloadProcessBuilder.start();
        
        // Capturer la sortie
        StringBuilder downloadOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(downloadProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                downloadOutput.append(line).append("\n");
                logger.info("yt-dlp download output: {}", line);
            }
        }
        
        int downloadExit = downloadProcess.waitFor();
        logger.info("Processus de téléchargement terminé avec code: {}", downloadExit);
        
        if (downloadExit != 0) {
            logger.error("Erreur lors du téléchargement: {}", downloadOutput.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ByteArrayResource(("Erreur de téléchargement: " + downloadOutput.toString()).getBytes()));
        }
        
        // Vérifier si le fichier webm a été téléchargé
        if (!webmFile.exists()) {
            logger.error("Le fichier webm n'a pas été téléchargé");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ByteArrayResource("Fichier webm non téléchargé".getBytes()));
        }
        
        logger.info("Fichier webm téléchargé avec succès: {}", webmFile.getAbsolutePath());
        
        // Convertir le fichier webm en mp3 avec FFmpeg
        ProcessBuilder convertProcessBuilder = new ProcessBuilder(
            ffmpegPath,
            "-i", webmFile.getAbsolutePath(),
            "-vn", "-ab", "128k", "-ar", "44100", "-y", output.getAbsolutePath()
        );
        convertProcessBuilder.redirectErrorStream(true);
        logger.info("Commande de conversion: {}", String.join(" ", convertProcessBuilder.command()));

        Process convertProcess = convertProcessBuilder.start();
        
        // Capturer la sortie de FFmpeg
        StringBuilder convertOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(convertProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                convertOutput.append(line).append("\n");
                logger.info("FFmpeg output: {}", line);
            }
        }
        
        int convertExit = convertProcess.waitFor();
        logger.info("Processus de conversion terminé avec code: {}", convertExit);
        
        if (convertExit != 0 || !output.exists()) {
            logger.error("Échec de la conversion: {}", convertOutput.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ByteArrayResource(("Échec de la conversion: " + convertOutput.toString()).getBytes()));
        }
        
        logger.info("Fichier mp3 créé avec succès: {} (taille: {} bytes)", output.getAbsolutePath(), output.length());
        
        // Créer une ressource à partir du fichier
        byte[] fileContent = Files.readAllBytes(output.toPath());
        ByteArrayResource resource = new ByteArrayResource(fileContent);
        
        logger.info("Ressource créée avec succès, taille: {} bytes", fileContent.length);
        
        // Configurer les en-têtes de la réponse
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        
        logger.info("Envoi du fichier au client");
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(fileContent.length)
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(resource);
    }

    /**
     * Extrait l'ID de la vidéo YouTube à partir de l'URL
     */
    private String extractVideoId(String url) {
        try {
            if (url.contains("youtube.com") || url.contains("youtu.be")) {
                // Format: https://www.youtube.com/watch?v=VIDEO_ID
                if (url.contains("?v=")) {
                    return url.split("\\?v=")[1].split("&")[0];
                }
                // Format: https://youtu.be/VIDEO_ID
                else if (url.contains("youtu.be/")) {
                    return url.split("youtu\\.be/")[1].split("\\?")[0];
                }
            }
        } catch (Exception e) {
            logger.warn("Impossible d'extraire l'ID de la vidéo de l'URL: {}", url);
        }
        return null;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchYouTube(@RequestParam String query) {
        logger.info("Recherche YouTube pour: {}", query);
        
        try {
            // Chemin vers yt-dlp selon l'OS
            String os = System.getProperty("os.name").toLowerCase();
            String ytDlpPath;
            
            if (os.contains("win")) {
                // Windows - utilise yt-dlp.exe
                String workingDir = System.getProperty("user.dir");
                ytDlpPath = workingDir + "/yt-dlp.exe";
            } else {
                // Linux/Mac - utilise yt-dlp depuis /usr/local/bin
                ytDlpPath = "/usr/local/bin/yt-dlp";
            }
            
            // Vérifier que yt-dlp existe
            File ytDlpFile = new File(ytDlpPath);
            if (!ytDlpFile.exists()) {
                logger.error("yt-dlp n'existe pas à l'emplacement: {}", ytDlpPath);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("yt-dlp non trouvé");
            }
            
            // Utilisez yt-dlp pour rechercher des vidéos
            ProcessBuilder processBuilder = new ProcessBuilder(
                ytDlpPath, 
                "--format=bestaudio", 
                "--dump-json",
                "--flat-playlist",
                "--max-downloads=10",
                "ytsearch10:" + query
            );
            
            logger.info("Commande de recherche: {}", String.join(" ", processBuilder.command()));
            
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            // Capturer la sortie standard et d'erreur
            StringBuilder processOutput = new StringBuilder();
            List<Map<String, Object>> results = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    processOutput.append(line).append("\n");
                    logger.debug("yt-dlp search output: {}", line);
                    
                    try {
                        Map<String, Object> videoInfo = mapper.readValue(line, Map.class);
                        Map<String, Object> result = new HashMap<>();
                        result.put("title", videoInfo.get("title"));
                        result.put("url", "https://www.youtube.com/watch?v=" + videoInfo.get("id"));
                        
                        // Gestion des miniatures (thumbnails)
                        if (videoInfo.containsKey("thumbnail")) {
                            result.put("thumbnail", videoInfo.get("thumbnail"));
                        } else if (videoInfo.containsKey("thumbnails") && videoInfo.get("thumbnails") instanceof List) {
                            List<Map<String, Object>> thumbnails = (List<Map<String, Object>>) videoInfo.get("thumbnails");
                            if (!thumbnails.isEmpty()) {
                                result.put("thumbnail", thumbnails.get(0).get("url"));
                            }
                        }
                        
                        // Ajouter des informations supplémentaires utiles
                        if (videoInfo.containsKey("duration")) {
                            result.put("duration", videoInfo.get("duration"));
                        }
                        if (videoInfo.containsKey("uploader")) {
                            result.put("uploader", videoInfo.get("uploader"));
                        }
                        
                        results.add(result);
                    } catch (Exception e) {
                        logger.warn("Erreur lors du parsing d'une ligne JSON: {}", e.getMessage());
                    }
                }
            }
            
            int exitCode = process.waitFor();
            logger.info("Processus de recherche terminé avec code: {}", exitCode);
            
            if (exitCode != 0) {
                logger.error("Erreur lors de la recherche: {}", processOutput.toString());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur lors de la recherche: " + processOutput.toString());
            }
            
            if (results.isEmpty()) {
                logger.warn("Aucun résultat trouvé pour la requête: {}", query);
                return ResponseEntity.ok(results); // Retourne une liste vide plutôt qu'une erreur
            }
            
            logger.info("Recherche réussie, {} résultats trouvés", results.size());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Exception lors de la recherche: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur: " + e.getMessage());
        }
    }
}

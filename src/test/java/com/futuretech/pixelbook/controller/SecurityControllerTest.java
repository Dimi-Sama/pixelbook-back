package com.futuretech.pixelbook.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futuretech.pixelbook.dto.LoginDTO;
import com.futuretech.pixelbook.model.User;
import com.futuretech.pixelbook.repository.UserRepository;
import com.futuretech.pixelbook.util.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // Sans désactiver les filtres pour les tests de sécurité
@ActiveProfiles("test")
@Transactional
public class SecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLoginReturnsJwtToken() throws Exception {
        // Créer un utilisateur avec mot de passe haché
        User user = new User();
        user.setEmail("jwt@example.com");
        String rawPassword = "testPassword123";
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setCreatedAt(new Date());
        userRepository.save(user);

        // Créer le DTO de login
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("jwt@example.com");
        loginDTO.setPassword(rawPassword);

        // Faire la requête de login
        MvcResult result = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email", is("jwt@example.com")))
                .andReturn();

        // Vérifier que le token est bien formaté (structure JWT : xxx.yyy.zzz)
        String response = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(response);
        String token = jsonResponse.get("token").asText();
        assertTrue(token.split("\\.").length == 3, "Le token devrait être au format JWT (xxx.yyy.zzz)");
    }

    @Test
    void testProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testProtectedEndpointWithValidToken() throws Exception {
        // Créer un utilisateur et obtenir un token
        User user = new User();
        user.setEmail("protected@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setCreatedAt(new Date());
        userRepository.save(user);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("protected@example.com");
        loginDTO.setPassword("password123");

        // Faire la requête de login pour obtenir le token
        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(response);
        String token = jsonResponse.get("token").asText();

        // Utiliser le token pour accéder à un endpoint protégé
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpointWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }
} 
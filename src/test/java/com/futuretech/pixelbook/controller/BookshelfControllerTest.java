package com.futuretech.pixelbook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futuretech.pixelbook.model.Bookshelf;
import com.futuretech.pixelbook.model.User;
import com.futuretech.pixelbook.repository.BookshelfRepository;
import com.futuretech.pixelbook.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class BookshelfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookshelfRepository bookshelfRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllBookshelves() throws Exception {
        // Créer un utilisateur
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setCreatedAt(new Date());
        user = userRepository.save(user);

        // Créer une bibliothèque
        Bookshelf bookshelf = new Bookshelf();
        bookshelf.setUser(user);
        bookshelf = bookshelfRepository.save(bookshelf);

        // Tester l'API
        mockMvc.perform(get("/api/bookshelves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void testGetBookshelfById() throws Exception {
        // Créer un utilisateur
        User user = new User();
        user.setEmail("test2@example.com");
        user.setPassword("password");
        user.setCreatedAt(new Date());
        user = userRepository.save(user);

        // Créer une bibliothèque
        Bookshelf bookshelf = new Bookshelf();
        bookshelf.setUser(user);
        bookshelf = bookshelfRepository.save(bookshelf);

        // Tester l'API
        mockMvc.perform(get("/api/bookshelves/{id}", bookshelf.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookshelf.getId().intValue())));
    }

    @Test
    void testDeleteBookshelf() throws Exception {
        // Créer un utilisateur
        User user = new User();
        user.setEmail("test4@example.com");
        user.setPassword("password");
        user.setCreatedAt(new Date());
        user = userRepository.save(user);

        // Créer une bibliothèque
        Bookshelf bookshelf = new Bookshelf();
        bookshelf.setUser(user);
        bookshelf = bookshelfRepository.save(bookshelf);

        // Tester l'API
        mockMvc.perform(delete("/api/bookshelves/{id}", bookshelf.getId()))
                .andExpect(status().isOk());
    }
} 
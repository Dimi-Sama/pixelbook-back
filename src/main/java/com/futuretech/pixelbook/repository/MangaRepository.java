package com.futuretech.pixelbook.repository;

import com.futuretech.pixelbook.model.Manga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MangaRepository extends JpaRepository<Manga, Long> {
    Optional<Manga> findByTitle(String title);
    List<Manga> findByAuthor(String author);
    List<Manga> findByTitleContainingIgnoreCase(String keyword);
    List<Manga> findByAuthorContainingIgnoreCase(String keyword);
    List<Manga> findBySynopsisContainingIgnoreCase(String keyword);
} 
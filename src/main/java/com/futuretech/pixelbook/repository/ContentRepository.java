package com.futuretech.pixelbook.repository;

import com.futuretech.pixelbook.model.Bookshelf;
import com.futuretech.pixelbook.model.Content;
import com.futuretech.pixelbook.model.Manga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByBookshelf(Bookshelf bookshelf);
    List<Content> findByBookshelfId(Long bookshelfId);
    List<Content> findByManga(Manga manga);
    List<Content> findByMangaId(Long mangaId);
    Optional<Content> findByBookshelfAndManga(Bookshelf bookshelf, Manga manga);
    Optional<Content> findByBookshelfIdAndMangaId(Long bookshelfId, Long mangaId);
} 
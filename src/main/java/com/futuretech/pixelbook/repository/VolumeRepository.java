package com.futuretech.pixelbook.repository;

import com.futuretech.pixelbook.model.Manga;
import com.futuretech.pixelbook.model.Volume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VolumeRepository extends JpaRepository<Volume, Long> {
    List<Volume> findByMangaId(Long mangaId);
    Optional<Volume> findByMangaIdAndNumber(Long mangaId, Integer number);
    Optional<Volume> findByMangaAndNumber(Manga manga, Integer number);
    Optional<Volume> findByMalId(Long malId);
    Optional<Volume> findByIsbn(String isbn);
} 
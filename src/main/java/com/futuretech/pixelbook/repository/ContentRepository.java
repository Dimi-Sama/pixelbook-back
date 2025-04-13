package com.futuretech.pixelbook.repository;

import com.futuretech.pixelbook.model.Bookshelf;
import com.futuretech.pixelbook.model.Content;
import com.futuretech.pixelbook.model.Volume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByBookshelf(Bookshelf bookshelf);
    List<Content> findByBookshelfId(Long bookshelfId);
    List<Content> findByVolume(Volume volume);
    List<Content> findByVolumeId(Long volumeId);
    
    Optional<Content> findByBookshelfIdAndVolumeId(Long bookshelfId, Long volumeId);
    boolean existsByBookshelfIdAndVolumeId(Long bookshelfId, Long volumeId);
    
    @Transactional
    void deleteByBookshelfIdAndVolumeId(Long bookshelfId, Long volumeId);
} 
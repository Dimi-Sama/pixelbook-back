package com.futuretech.pixelbook.repository;

import com.futuretech.pixelbook.model.Bookshelf;
import com.futuretech.pixelbook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookshelfRepository extends JpaRepository<Bookshelf, Long> {
    Optional<Bookshelf> findByUser(User user);
    Optional<Bookshelf> findByUserId(Long userId);
} 
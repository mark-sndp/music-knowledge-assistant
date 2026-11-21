package com.musicknowledge.assistant.catalog.repository;

import com.musicknowledge.assistant.catalog.domain.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {}

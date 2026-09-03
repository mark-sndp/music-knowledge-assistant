package com.musicknowledge.assistant.catalog.repository;

import com.musicknowledge.assistant.catalog.domain.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
}
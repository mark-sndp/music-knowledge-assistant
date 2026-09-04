package com.musicknowledge.assistant.catalog.repository;

import com.musicknowledge.assistant.catalog.domain.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

	java.util.Optional<Artist> findByNameIgnoreCase(String name);
}

package com.musicknowledge.assistant.catalog.repository;

import com.musicknowledge.assistant.catalog.domain.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
}

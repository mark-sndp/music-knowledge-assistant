package com.musicknowledge.assistant.catalog.repository;

import com.musicknowledge.assistant.catalog.domain.BillboardChartEntry;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillboardChartEntryRepository extends JpaRepository<BillboardChartEntry, Long> {

  Optional<BillboardChartEntry> findByYearAndWeekAndRank(Integer year, Integer week, Integer rank);
}

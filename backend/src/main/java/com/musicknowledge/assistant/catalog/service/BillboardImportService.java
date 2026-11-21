package com.musicknowledge.assistant.catalog.service;

import com.musicknowledge.assistant.catalog.domain.Album;
import com.musicknowledge.assistant.catalog.domain.Artist;
import com.musicknowledge.assistant.catalog.domain.BillboardChartEntry;
import com.musicknowledge.assistant.catalog.domain.Track;
import com.musicknowledge.assistant.catalog.repository.AlbumRepository;
import com.musicknowledge.assistant.catalog.repository.ArtistRepository;
import com.musicknowledge.assistant.catalog.repository.BillboardChartEntryRepository;
import com.musicknowledge.assistant.catalog.repository.TrackRepository;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillboardImportService {

  private final ArtistRepository artistRepository;
  private final AlbumRepository albumRepository;
  private final TrackRepository trackRepository;
  private final BillboardChartEntryRepository chartEntryRepository;

  public BillboardImportService(
      ArtistRepository artistRepository,
      AlbumRepository albumRepository,
      TrackRepository trackRepository,
      BillboardChartEntryRepository chartEntryRepository) {
    this.artistRepository = artistRepository;
    this.albumRepository = albumRepository;
    this.trackRepository = trackRepository;
    this.chartEntryRepository = chartEntryRepository;
  }

  @Transactional
  public ImportReport importCsv(Reader reader) {
    List<BillboardRow> rows = parse(reader);
    int imported = 0;
    int skipped = 0;

    for (BillboardRow row : rows) {
      if (chartEntryRepository
          .findByYearAndWeekAndRank(row.year(), row.week(), row.rank())
          .isPresent()) {
        skipped++;
        continue;
      }

      Artist artist =
          artistRepository
              .findByNameIgnoreCase(row.artist())
              .orElseGet(
                  () -> {
                    Artist created = new Artist();
                    created.setName(row.artist());
                    return artistRepository.save(created);
                  });

      Album album = null;
      if (!row.album().isBlank()) {
        album =
            albumRepository
                .findByTitleIgnoreCaseAndArtistId(row.album(), artist.getId())
                .orElseGet(
                    () -> {
                      Album created = new Album();
                      created.setTitle(row.album());
                      created.setArtist(artist);
                      return albumRepository.save(created);
                    });
      }

      final Album resolvedAlbum = album;
      Track track =
          trackRepository
              .findByTitleIgnoreCaseAndAlbumId(
                  row.title(), resolvedAlbum == null ? null : resolvedAlbum.getId())
              .orElseGet(
                  () -> {
                    Track created = new Track();
                    created.setTitle(row.title());
                    created.setDuration(0);
                    created.setAlbum(resolvedAlbum);
                    return trackRepository.save(created);
                  });

      BillboardChartEntry entry = new BillboardChartEntry();
      entry.setYear(row.year());
      entry.setWeek(row.week());
      entry.setRank(row.rank());
      entry.setTrack(track);
      chartEntryRepository.save(entry);
      imported++;
    }
    return new ImportReport(imported, skipped);
  }

  private List<BillboardRow> parse(Reader reader) {
    List<BillboardRow> rows = new ArrayList<>();
    try (CSVParser parser =
        CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .get()
            .parse(reader)) {
      requireHeaders(parser.getHeaderMap());
      for (CSVRecord record : parser) {
        BillboardRow row =
            new BillboardRow(
                integer(record, "year"),
                integer(record, "week"),
                integer(record, "rank"),
                required(record, "title"),
                required(record, "artist"),
                parserHasAlbum(record) ? record.get("album").trim() : "");
        validate(row, record.getRecordNumber());
        rows.add(row);
      }
    } catch (IOException exception) {
      throw new IllegalArgumentException("Unable to read Billboard CSV", exception);
    }
    return rows;
  }

  private void requireHeaders(java.util.Map<String, Integer> headers) {
    for (String required : List.of("year", "week", "rank", "title", "artist")) {
      if (!headers.containsKey(required)) {
        throw new IllegalArgumentException("Missing Billboard CSV column: " + required);
      }
    }
  }

  private boolean parserHasAlbum(CSVRecord record) {
    return record.isMapped("album");
  }

  private String required(CSVRecord record, String column) {
    String value = record.get(column).trim();
    if (value.isEmpty()) {
      throw new IllegalArgumentException(
          "Blank Billboard CSV value for " + column + " on row " + record.getRecordNumber());
    }
    return value;
  }

  private int integer(CSVRecord record, String column) {
    try {
      return Integer.parseInt(required(record, column));
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException(
          "Invalid Billboard number for " + column + " on row " + record.getRecordNumber(),
          exception);
    }
  }

  private void validate(BillboardRow row, long recordNumber) {
    if (row.year() < 1980 || row.year() > 1989) {
      throw new IllegalArgumentException(
          "Billboard year must be between 1980 and 1989 on row " + recordNumber);
    }
    if (row.week() < 1 || row.week() > 53 || row.rank() < 1 || row.rank() > 100) {
      throw new IllegalArgumentException("Invalid Billboard week or rank on row " + recordNumber);
    }
  }

  public record ImportReport(int importedRows, int skippedRows) {}

  private record BillboardRow(
      int year, int week, int rank, String title, String artist, String album) {}
}

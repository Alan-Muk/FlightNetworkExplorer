package com.backend.backend.service;

import com.backend.backend.exception.AirlineImportException;
import com.backend.backend.model.Airline;
import com.backend.backend.repository.AirlineRepository;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Imports airline data from the OpenFlights {@code airlines.dat} file into the database on
 * application startup.
 *
 * <p>Column layout of {@code airlines.dat} (0-indexed):
 *
 * <pre>
 *   0: airline ID
 *   1: airline name
 *   2: alias
 *   3: IATA code             (may be absent or malformed)
 *   4: ICAO code             (may be absent or malformed)
 *   5: callsign              (unused)
 *   6: country
 *   7: active                ("Y" or "N")
 * </pre>
 *
 * <p>IATA codes are validated to be exactly 3 uppercase letters; ICAO codes to be exactly 4. Any
 * value that does not match (including OpenFlights null markers such as {@code \N}, escaped
 * apostrophes such as {@code \\'}, or the placeholder {@code N/A}) is treated as {@code null}.
 */
@Component
public class AirlineImportService implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(AirlineImportService.class);

  private static final int COL_ID = 0;
  private static final int COL_NAME = 1;
  private static final int COL_ALIAS = 2;
  private static final int COL_IATA = 3;
  private static final int COL_ICAO = 4;
  private static final int COL_COUNTRY = 6;
  private static final int COL_ACTIVE = 7;
  private static final int MIN_COLUMNS = 8;

  private final AirlineRepository repository;
  private final Path airlinesFile;

  public AirlineImportService(
      AirlineRepository repository,
      @Value("${airlines.file:../data/raw/airlines.dat}") String airlinesFile) {
    this.repository = repository;
    this.airlinesFile = Path.of(airlinesFile).toAbsolutePath().normalize();
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    long existing = repository.count();
    if (existing > 0) {
      log.info("Airlines already loaded ({} rows) — skipping import", existing);
      return;
    }

    if (!Files.exists(airlinesFile)) {
      throw new AirlineImportException("Airlines file not found: " + airlinesFile);
    }

    log.info("Importing airlines from {}", airlinesFile);

    List<Airline> airlines = new ArrayList<>();
    int parsed = 0;
    int skipped = 0;
    int droppedIata = 0;
    int droppedIcao = 0;

    try (Reader reader = Files.newBufferedReader(airlinesFile, StandardCharsets.UTF_8);
        CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {

      for (CSVRecord record : parser) {
        if (record.size() < MIN_COLUMNS) {
          skipped++;
          continue;
        }

        try {
          String rawIata = record.get(COL_IATA);
          String rawIcao = record.get(COL_ICAO);

          String iata = cleanCode(rawIata, 3);
          String icao = cleanCode(rawIcao, 4);

          if (iata == null && rawIata != null && !rawIata.isBlank()) droppedIata++;
          if (icao == null && rawIcao != null && !rawIcao.isBlank()) droppedIcao++;

          Airline airline = new Airline();
          airline.setId(Long.parseLong(cleanRaw(record.get(COL_ID))));
          airline.setName(cleanRaw(record.get(COL_NAME)));
          airline.setAlias(cleanOrNull(record.get(COL_ALIAS)));
          airline.setIata(iata);
          airline.setIcao(icao);
          airline.setCountry(cleanOrNull(record.get(COL_COUNTRY)));
          airline.setActive(cleanCode(record.get(COL_ACTIVE), 1));
          airlines.add(airline);
          parsed++;

        } catch (Exception e) {
          skipped++;
          log.warn(
              "Skipping malformed airline at line {}: {}",
              parser.getCurrentLineNumber(),
              e.getMessage());
        }
      }
    } catch (IOException e) {
      throw new AirlineImportException("Failed to read airlines file: " + airlinesFile, e);
    }

    if (airlines.isEmpty()) {
      log.warn("No valid airlines found in {} — nothing imported", airlinesFile);
      return;
    }

    repository.saveAll(airlines);
    log.info(
        "Airline import completed: {} imported, {} skipped, {} invalid IATA dropped, {} invalid"
            + " ICAO dropped",
        parsed,
        skipped,
        droppedIata,
        droppedIcao);
  }

  // -------------------------------------------------------------------------
  // Cleaning helpers
  // -------------------------------------------------------------------------

  /** Trims and strips quotes but keeps the value non-null (for required fields). */
  private static String cleanRaw(String value) {
    return value == null ? null : value.replace("\"", "").trim();
  }

  /** Returns null for empty values and placeholder markers, otherwise trimmed (case preserved). */
  private static String cleanOrNull(String value) {
    String cleaned = cleanRaw(value);
    if (cleaned == null || cleaned.isEmpty() || isNullMarker(cleaned)) {
      return null;
    }
    return cleaned;
  }

  /**
   * Validates a code field strictly. Returns null unless the cleaned value is exactly {@code len}
   * uppercase letters (A-Z). This rejects OpenFlights null markers, escaped apostrophes, and any
   * other malformed value.
   */
  private static String cleanCode(String value, int len) {
    String cleaned = cleanRaw(value);
    if (cleaned == null || cleaned.isEmpty()) return null;
    cleaned = cleaned.toUpperCase();
    if (cleaned.length() != len) return null;
    if (!cleaned.matches("[A-Z]+")) return null;
    return cleaned;
  }

  private static boolean isNullMarker(String value) {
    if (value.matches("\\\\+N")) return true;
    return "N/A".equals(value) || "-".equals(value);
  }
}

package com.backend.backend.service;

import com.backend.backend.model.Airline;
import com.backend.backend.repository.AirlineRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.Reader;

@Service
public class AirlineImportService {

    private final AirlineRepository repository;

    public AirlineImportService(AirlineRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void importAirlines() {

        if (repository.count() > 0) {
            System.out.println("Airlines already loaded");
            return;
        }

        try (
            Reader reader = new FileReader("../data/raw/airlines.dat");
            CSVParser parser = CSVFormat.DEFAULT.parse(reader)
        ) {

            for (CSVRecord record : parser) {

                if (record.size() < 8) {
                    continue;
                }

                Airline airline = new Airline();

                airline.setId(
                    Long.parseLong(record.get(0))
                );

                airline.setName(
                    clean(record.get(1))
                );

                airline.setAlias(
                    clean(record.get(2))
                );

                airline.setIata(
                    clean(record.get(3))
                );

                airline.setIcao(
                    clean(record.get(4))
                );

                airline.setCountry(
                    clean(record.get(6))
                );

                airline.setActive(
                    clean(record.get(7))
                );

                repository.save(airline);
            }

            System.out.println(
                "Airline import completed: "
                + repository.count()
            );

        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to import airlines",
                e
            );
        }
    }

    private String clean(String value) {
        return value
                .replace("\"", "")
                .trim();
    }
}

/**
 * Service responsible for importing airline data from the OpenFlights
 * {@code airlines.dat} file into the database when the application starts.
 *
 * This service executes automatically after the Spring bean is initialized
 * via the {@link jakarta.annotation.PostConstruct} annotation. Before importing,
 * it checks whether airline records already exist to prevent duplicate imports.
 * If data is already present, the import process is skipped.
 *
 * During the import process, each CSV record is parsed, cleaned, mapped to
 * an {@link com.backend.backend.model.Airline} entity, and persisted using the
 * {@link com.backend.backend.repository.AirlineRepository}. Records with
 * insufficient fields are ignored.
 *
 * Any quotation marks surrounding field values are removed and whitespace is
 * trimmed before the values are stored. If an error occurs while reading or
 * processing the file, a {@link RuntimeException} is thrown to indicate that
 * the import failed.
 *
 * Data Source: {@code ../data/raw/airlines.dat}
 *
 * @author Your Name
 * @since 1.0
 */

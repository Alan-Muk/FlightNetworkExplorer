package com.backend.backend.service;

import com.backend.backend.model.Airport;
import com.backend.backend.repository.AirportRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.Reader;

@Service
public class AirportImportService {

    private final AirportRepository repository;

    public AirportImportService(AirportRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void importAirports() {

        if (repository.count() > 0) {
            System.out.println("Airports already loaded");
            return;
        }

        try (
            Reader reader = new FileReader("../data/raw/airports.dat");
            CSVParser parser = CSVFormat.DEFAULT
                    .parse(reader)
        ) {

            for (CSVRecord record : parser) {

                if (record.size() < 8) {
                    continue;
                }

                Airport airport = new Airport();

                airport.setId(
                    Long.parseLong(record.get(0))
                );

                airport.setName(
                    clean(record.get(1))
                );

                airport.setCity(
                    clean(record.get(2))
                );

                airport.setCountry(
                    clean(record.get(3))
                );

                airport.setIata(
                    clean(record.get(4))
                );

                airport.setIcao(
                    clean(record.get(5))
                );

                airport.setLatitude(
                    Double.parseDouble(record.get(6))
                );

                airport.setLongitude(
                    Double.parseDouble(record.get(7))
                );

                repository.save(airport);
            }

            System.out.println(
                "Airport import completed: "
                + repository.count()
            );

        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to import airports",
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
package com.backend.backend.service;

import com.backend.backend.model.Route;
import com.backend.backend.repository.RouteRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;


@Service
public class RouteImportService {


    private final RouteRepository repository;


    public RouteImportService(
            RouteRepository repository
    ) {

        this.repository = repository;

    }



    @PostConstruct
    public void importRoutes() {


        if (repository.count() > 0) {

            System.out.println(
                    "Routes already loaded"
            );

            return;

        }



        List<Route> routes =
                new ArrayList<>();


        try (

            Reader reader =
                    new FileReader(
                            "../data/raw/routes.dat"
                    );

            CSVParser parser =
                    CSVFormat.DEFAULT.parse(reader)

        ) {



            for (CSVRecord record : parser) {


                if (record.size() < 9) {
                    continue;
                }



                String sourceIata =
                        clean(record.get(3));


                String destinationIata =
                        clean(record.get(5));



                if (

                    sourceIata.isBlank()
                    ||
                    destinationIata.isBlank()
                    ||
                    sourceIata.equals("\\N")
                    ||
                    destinationIata.equals("\\N")

                ) {

                    continue;

                }



                Route route =
                        new Route();



                route.setAirline(
                        clean(record.get(0))
                );



                route.setSourceAirport(
                        clean(record.get(2))
                );


                route.setSourceIata(
                        sourceIata
                );



                route.setDestinationAirport(
                        clean(record.get(4))
                );


                route.setDestinationIata(
                        destinationIata
                );



                routes.add(route);


            }



            repository.saveAll(routes);



            System.out.println(
                    "Route import completed: "
                    + repository.count()
            );



        } catch (Exception e) {


            throw new RuntimeException(
                    "Failed to import routes",
                    e
            );

        }

    }




    private String clean(
            String value
    ) {

        return value
                .replace("\"", "")
                .trim()
                .toUpperCase();

    }

}

/**
 * Service responsible for importing route data from the OpenFlights
 * {@code routes.dat} file into the database when the application starts.
 *
 * This service executes automatically after the Spring bean is initialized
 * via the {@link jakarta.annotation.PostConstruct} annotation. Before
 * importing, it checks whether route records already exist to prevent
 * duplicate imports. If route data is already present, the import process is
 * skipped.
 *
 * Each record from the data file is validated, cleaned, mapped to a
 * {@link com.backend.backend.model.Route} entity, and stored using the
 * {@link com.backend.backend.repository.RouteRepository}. Records with missing
 * fields or invalid source or destination IATA codes are ignored.
 *
 * String values are cleaned by removing quotation marks, trimming
 * whitespace, and converting text to uppercase before being stored. If an
 * error occurs while reading or processing the file, a
 * {@link RuntimeException} is thrown.
 *
 * Data Source: {@code ../data/raw/routes.dat}
 *
 * 
 * 
 */

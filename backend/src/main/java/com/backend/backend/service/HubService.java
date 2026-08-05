package com.backend.backend.service;


import com.backend.backend.model.Airport;
import com.backend.backend.repository.AirportRepository;
import com.backend.backend.repository.RouteRepository;
import com.backend.backend.dto.HubDTO;

import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class HubService {


    private final AirportRepository airportRepository;

    private final RouteRepository routeRepository;



    public HubService(
            AirportRepository airportRepository,
            RouteRepository routeRepository
    ) {

        this.airportRepository =
                airportRepository;

        this.routeRepository =
                routeRepository;

    }




public List<HubDTO> getMajorHubs() {

        Map<String, Integer> scores =
                new HashMap<>();


        routeRepository
                .findTopDepartureAirports()
                .forEach(row -> {

                    String iata =
                            (String) row[0];

                    Integer count =
                            ((Long) row[1]).intValue();

                    scores.merge(
                            iata,
                            count,
                            Integer::sum
                    );
                });


        routeRepository
                .findTopArrivalAirports()
                .forEach(row -> {

                    String iata =
                            (String) row[0];

                    Integer count =
                            ((Long) row[1]).intValue();

                    scores.merge(
                            iata,
                            count,
                            Integer::sum
                    );
                });


        List<String> hubIatas =
                scores.entrySet()
                        .stream()
                        .sorted(
                                Map.Entry
                                        .<String,Integer>
                                        comparingByValue()
                                        .reversed()
                        )
                        .limit(150)
                        .map(Map.Entry::getKey)
                        .toList();


        Map<String, Airport> airports =
                airportRepository
                        .findByIataIn(hubIatas)
                        .stream()
                        .collect(
                                java.util.stream.Collectors
                                        .toMap(
                                                Airport::getIata,
                                                airport -> airport
                                        )
                        );


        return hubIatas.stream()
                .map(iata -> {

                    Airport airport =
                            airports.get(iata);


                    if (airport == null) {
                        return null;
                    }


                    return new HubDTO(

                            airport.getIata(),

                            airport.getName(),

                            airport.getCity(),

                            airport.getCountry(),

                            airport.getLatitude(),

                            airport.getLongitude(),

                            scores.get(iata)

                    );

                })
                .filter(Objects::nonNull)
                .toList();
    }

}

/**
 * Service responsible for identifying the busiest airport hubs in the airline
 * network based on route activity.
 *
 * The service calculates a hub score for each airport by combining its
 * total number of departing and arriving routes. Airports are then ranked by
 * this score, and the highest-ranking airports are returned as
 * {@link com.backend.backend.dto.HubDTO} objects containing airport details
 * and their calculated hub score.
 *
 * The resulting data can be used to visualize or analyze the major hubs
 * within the global airline network.
 *
 * 
 * 
 */

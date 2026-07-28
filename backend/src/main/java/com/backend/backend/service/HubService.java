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
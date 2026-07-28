package com.backend.backend.service;


import com.backend.backend.dto.AirportStatsResponse;
import com.backend.backend.model.Airport;
import com.backend.backend.model.Route;
import com.backend.backend.repository.AirportRepository;
import com.backend.backend.repository.RouteRepository;

import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;



@Service
public class AirportStatsService {


    private final AirportRepository airportRepository;

    private final RouteRepository routeRepository;



    public AirportStatsService(

            AirportRepository airportRepository,

            RouteRepository routeRepository

    ){

        this.airportRepository =
                airportRepository;

        this.routeRepository =
                routeRepository;

    }




    public AirportStatsResponse getStats(
            String iata
    ){


        Airport airport =
                airportRepository
                .findByIata(
                    iata.toUpperCase()
                )
                .orElseThrow();



        List<Route> routes =
                routeRepository.findAll();



        List<Route> related =
                routes.stream()

                .filter(route ->

                    route.getSourceIata()
                    .equalsIgnoreCase(iata)

                    ||

                    route.getDestinationIata()
                    .equalsIgnoreCase(iata)

                )

                .toList();



        AirportStatsResponse response =
                new AirportStatsResponse();


        response.setIata(
                airport.getIata()
        );


        response.setName(
                airport.getName()
        );


        response.setConnections(
                related.size()
        );


        response.setOutgoingRoutes(

            (int) related.stream()

            .filter(r ->
                r.getSourceIata()
                .equalsIgnoreCase(iata)
            )

            .count()

        );



        response.setIncomingRoutes(

            (int) related.stream()

            .filter(r ->
                r.getDestinationIata()
                .equalsIgnoreCase(iata)
            )

            .count()

        );



        response.setTopDestinations(

            related.stream()

            .map(Route::getDestinationIata)

            .filter(
                Objects::nonNull
            )

            .distinct()

            .limit(10)

            .toList()

        );



        response.setAirlines(

            related.stream()

            .map(Route::getAirline)

            .filter(
                Objects::nonNull
            )

            .distinct()

            .limit(10)

            .toList()

        );


        return response;

    }

}
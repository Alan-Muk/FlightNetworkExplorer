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

/**
 * Service responsible for generating statistical information about a specific
 * airport based on the available route data.
 *
 * <p>The service retrieves an airport using its IATA code and analyzes all
 * stored routes to calculate airport-related statistics. These include the
 * total number of connected routes, incoming and outgoing route counts,
 * the most common destination airports, and the airlines operating routes
 * associated with the airport.</p>
 *
 * <p>The calculated statistics are returned as an
 * {@link com.backend.backend.dto.AirportStatsResponse} object for use by the
 * application's REST controllers.</p>
 *
 * @author Your Name
 * @since 1.0
 */
/**
 * Retrieves statistical information for the airport identified by the
 * specified IATA code.
 *
 * The method searches for the airport, identifies all routes where the
 * airport is either the source or destination, and calculates:
 * 
 *   Total number of connected routes
 *   Number of outgoing routes
 *   Number of incoming routes
 *   Up to ten unique destination airports
 *   Up to ten unique airlines serving the airport
 * 
 * The results are returned as an {@link AirportStatsResponse}.
 *
 * @param iata the IATA airport code
 * @return an {@link AirportStatsResponse} containing airport statistics
 * @throws java.util.NoSuchElementException if no airport exists with the
 *         specified IATA code
 */

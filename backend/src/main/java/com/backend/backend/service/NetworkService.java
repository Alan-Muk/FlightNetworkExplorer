package com.backend.backend.service;

import com.backend.backend.dto.NetworkResponse;
import com.backend.backend.model.Airport;
import com.backend.backend.model.Route;
import com.backend.backend.repository.AirportRepository;
import com.backend.backend.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NetworkService {

    private final AirportRepository airportRepository;
    private final RouteRepository routeRepository;


    public NetworkService(
            AirportRepository airportRepository,
            RouteRepository routeRepository
    ) {
        this.airportRepository = airportRepository;
        this.routeRepository = routeRepository;
    }


public NetworkResponse getNetwork(String iata) {

    String airportIata = iata.toUpperCase();


        List<Route> routes =
                routeRepository.findBySourceIata(airportIata);


        Airport source =
                airportRepository
                        .findByIata(airportIata)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Airport not found: " + airportIata
                                )
                        );


        Map<String, NetworkResponse.AirportNode> nodes =
                new HashMap<>();


        List<NetworkResponse.RouteEdge> edges =
                new ArrayList<>();


        nodes.put(
                source.getIata(),
                toNode(source)
        );


        List<String> destinationIatas =
                routes.stream()
                        .map(Route::getDestinationIata)
                        .distinct()
                        .toList();


        List<Airport> destinations =
                airportRepository.findByIataIn(
                        destinationIatas
                );


        for (Airport destination : destinations) {

            nodes.put(
                    destination.getIata(),
                    toNode(destination)
            );
        }


        for (Route route : routes) {

            if (nodes.containsKey(route.getDestinationIata())) {

                NetworkResponse.RouteEdge edge =
                        new NetworkResponse.RouteEdge();

                edge.setFrom(airportIata);

                edge.setTo(
                        route.getDestinationIata()
                );

                edges.add(edge);
            }
        }


        NetworkResponse response =
                new NetworkResponse();

        response.setAirport(airportIata);

        response.setNodes(
                new ArrayList<>(nodes.values())
        );

        response.setEdges(edges);


        return response;
    }


    private NetworkResponse.AirportNode toNode(
            Airport airport
    ) {

        NetworkResponse.AirportNode node =
                new NetworkResponse.AirportNode();

        node.setIata(
                airport.getIata()
        );

        node.setName(
                airport.getName()
        );

        node.setLatitude(
                airport.getLatitude()
        );

        node.setLongitude(
                airport.getLongitude()
        );


        return node;
    }


    public List<Airport> getHubAirports() {

        return airportRepository
                .findTop200ByOrderByIdAsc();

    }

}
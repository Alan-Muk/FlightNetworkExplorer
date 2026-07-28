package com.backend.backend.service;

import com.backend.backend.dto.NetworkResponse;
import com.backend.backend.model.Airport;
import com.backend.backend.model.Route;
import com.backend.backend.repository.AirportRepository;
import com.backend.backend.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GlobalNetworkService {

    private final AirportRepository airports;
    private final RouteRepository routes;


    public GlobalNetworkService(
            AirportRepository airports,
            RouteRepository routes
    ) {
        this.airports = airports;
        this.routes = routes;
    }


    public NetworkResponse getAll() {

        NetworkResponse response =
                new NetworkResponse();


        List<NetworkResponse.AirportNode> nodes =
                airports.findAll()
                        .stream()
                        .filter(a -> a.getIata() != null)
                        .map(this::node)
                        .collect(Collectors.toList());


        List<NetworkResponse.RouteEdge> edges =
                routes.findAll()
                        .stream()
                        .map(this::edge)
                        .collect(Collectors.toList());


        response.setNodes(nodes);
        response.setEdges(edges);

        return response;
    }


    private NetworkResponse.AirportNode node(
            Airport airport
    ) {

        NetworkResponse.AirportNode n =
                new NetworkResponse.AirportNode();

        n.setIata(airport.getIata());
        n.setName(airport.getName());
        n.setLatitude(airport.getLatitude());
        n.setLongitude(airport.getLongitude());

        return n;
    }


    private NetworkResponse.RouteEdge edge(
            Route route
    ) {

        NetworkResponse.RouteEdge e =
                new NetworkResponse.RouteEdge();

        e.setFrom(route.getSourceIata());
        e.setTo(route.getDestinationIata());

        return e;
    }
}
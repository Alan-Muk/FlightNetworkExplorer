package com.backend.backend.service;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GraphServiceClient {


    private final RestTemplate restTemplate =
            new RestTemplate();


    private final String graphUrl =
            "http://localhost:8000";



    public Map connections(
            String airport
    ) {

        return restTemplate.getForObject(

                graphUrl
                + "/connections/"
                + airport.toUpperCase(),

                Map.class

        );

    }


public Map path(
        String source,
        String destination
) {

    try {

        return restTemplate.getForObject(

                graphUrl
                + "/path/"
                + source.toUpperCase()
                + "/"
                + destination.toUpperCase(),

                Map.class

        );


    } catch (
            HttpClientErrorException e
    ) {


        if (
            e.getStatusCode()
             .equals(HttpStatusCode.valueOf(404))
        ) {

            return Map.of(

                    "from",
                    source.toUpperCase(),

                    "to",
                    destination.toUpperCase(),

                    "path",
                    java.util.List.of()

            );

        }


        throw e;

    }

}


public Map paths(
        String source,
        String destination
) {

    try {

        return restTemplate.getForObject(

                graphUrl
                + "/paths/"
                + source.toUpperCase()
                + "/"
                + destination.toUpperCase(),

                Map.class

        );


    } catch (
            HttpClientErrorException e
    ) {


        if (
            e.getStatusCode()
             .equals(HttpStatusCode.valueOf(404))
        ) {

            return Map.of(
                    "paths",
                    java.util.List.of()
            );

        }


        throw e;

    }

}

}

/**
 * Service responsible for communicating with the external graph analysis
 * service via REST API calls.
 *
 * This service acts as a client for the Python graph service running on
 * {@code http://localhost:8000}. It provides methods for retrieving airport
 * connection information, the shortest path between two airports, and all
 * available paths between two airports.
 *
 * If the graph service returns a 404 (Not Found) for path-related requests,
 * the service returns an empty result instead of propagating the exception,
 * allowing the application to handle missing routes gracefully.
 *
 * 
 * 
 */

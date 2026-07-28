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
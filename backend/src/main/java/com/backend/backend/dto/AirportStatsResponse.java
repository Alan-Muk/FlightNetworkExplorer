package com.backend.backend.dto;


import lombok.Data;

import java.util.List;


@Data
public class AirportStatsResponse {


    private String iata;

    private String name;


    private int connections;

    private int incomingRoutes;

    private int outgoingRoutes;


    private List<String> topDestinations;


    private List<String> airlines;

}
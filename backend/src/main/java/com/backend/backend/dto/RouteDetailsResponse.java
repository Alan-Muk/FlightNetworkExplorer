package com.backend.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class RouteDetailsResponse {

    private String from;

    private String to;

    private double distanceKm;

    private String estimatedFlightTime;

    private List<String> airlines;

    private boolean direct;

}
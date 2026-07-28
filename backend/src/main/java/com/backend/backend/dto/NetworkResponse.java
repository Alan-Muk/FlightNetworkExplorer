package com.backend.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class NetworkResponse {

    private String airport;

    private List<AirportNode> nodes;

    private List<RouteEdge> edges;


    @Data
    public static class AirportNode {

        private String iata;

        private String name;

        private double latitude;

        private double longitude;
    }


    @Data
    public static class RouteEdge {

        private String from;

        private String to;
    }
}
package com.backend.backend.dto;


import java.util.List;


public class RouteOption {


    private String id;


    private String from;


    private String to;


    private double distanceKm;


    private String estimatedFlightTime;


    private boolean fastest;


    private boolean shortest;


    private boolean mostConnected;


    private boolean leastConnected;


    private String colour;


    private List<String> airlines;

    private List<String> airports;
    
    private int stops;


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }



    public String getFrom() {
        return from;
    }


    public void setFrom(String from) {
        this.from = from;
    }



    public String getTo() {
        return to;
    }


    public void setTo(String to) {
        this.to = to;
    }



    public double getDistanceKm() {
        return distanceKm;
    }


    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }



    public String getEstimatedFlightTime() {
        return estimatedFlightTime;
    }


    public void setEstimatedFlightTime(String estimatedFlightTime) {
        this.estimatedFlightTime = estimatedFlightTime;
    }



    public boolean isFastest() {
        return fastest;
    }


    public void setFastest(boolean fastest) {
        this.fastest = fastest;
    }



    public boolean isShortest() {
        return shortest;
    }


    public void setShortest(boolean shortest) {
        this.shortest = shortest;
    }



    public boolean isMostConnected() {
        return mostConnected;
    }


    public void setMostConnected(boolean mostConnected) {
        this.mostConnected = mostConnected;
    }



    public boolean isLeastConnected() {
        return leastConnected;
    }


    public void setLeastConnected(boolean leastConnected) {
        this.leastConnected = leastConnected;
    }



    public String getColour() {
        return colour;
    }


    public void setColour(String colour) {
        this.colour = colour;
    }



    public List<String> getAirlines() {
        return airlines;
    }


    public void setAirlines(List<String> airlines) {
        this.airlines = airlines;
    }

    public List<String> getAirports() {
    return airports;
    }


    public void setAirports(List<String> airports) {
        this.airports = airports;
    }


    public int getStops() {
        return stops;
    }


    public void setStops(int stops) {
        this.stops = stops;
    }

}
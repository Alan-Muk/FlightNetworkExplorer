package com.backend.backend.dto;


public class HubDTO {


    private String iata;

    private String name;

    private String city;

    private String country;

    private double latitude;

    private double longitude;

    private int connections;



    public HubDTO(
            String iata,
            String name,
            String city,
            String country,
            double latitude,
            double longitude,
            int connections
    ) {

        this.iata = iata;
        this.name = name;
        this.city = city;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.connections = connections;

    }



    public String getIata() {
        return iata;
    }


    public String getName() {
        return name;
    }


    public String getCity() {
        return city;
    }


    public String getCountry() {
        return country;
    }


    public double getLatitude() {
        return latitude;
    }


    public double getLongitude() {
        return longitude;
    }


    public int getConnections() {
        return connections;
    }

}
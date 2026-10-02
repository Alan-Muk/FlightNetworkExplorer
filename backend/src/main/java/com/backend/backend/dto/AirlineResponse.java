package com.backend.backend.dto;

/** Public representation of an airline. */
public record AirlineResponse(
    Long id, String iata, String icao, String name, String alias, String country, String active) {}

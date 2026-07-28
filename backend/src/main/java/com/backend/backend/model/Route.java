package com.backend.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "routes")
@Data
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String airline;


    private String sourceAirport;


    private String destinationAirport;


    private String sourceIata;


    private String destinationIata;


    private Double distanceKm;



    /**
     * Returns airlines as a list.
     * Supports multiple airlines stored as:
     * "KQ,ET,BA"
     */
    public List<String> getAirlines() {

        if (airline == null || airline.isBlank()) {

            return List.of();

        }


        return Arrays.stream(
                    airline.split(",")
                )
                .map(String::trim)
                .toList();

    }

}
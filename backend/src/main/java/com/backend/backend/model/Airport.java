package com.backend.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Airport {

    @Id
    private Long id;

    private String name;

    private String city;

    private String country;

    private String iata;

    private String icao;

    private double latitude;

    private double longitude;
}
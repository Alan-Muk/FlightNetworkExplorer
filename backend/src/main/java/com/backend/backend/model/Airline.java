package com.backend.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "airlines")
@Data
public class Airline {

    @Id
    private Long id;

    private String name;

    private String alias;

    private String iata;

    private String icao;

    private String country;

    private String active;
}
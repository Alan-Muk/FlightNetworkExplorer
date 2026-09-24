package com.backend.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * A direct flight route between two airports, identified by their IATA codes.
 *
 * <p>One row per (airline, source, destination) triple, matching the OpenFlights {@code routes.dat}
 * granularity. The same physical connection served by multiple airlines is stored as multiple rows.
 */
@Entity
@Table(name = "routes")
@Getter
@Setter
public class Route {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Airline IATA code, or a comma-separated list of codes for pre-aggregated data. */
  private String airline;

  @Column(name = "source_iata", nullable = false, length = 3)
  private String sourceIata;

  @Column(name = "destination_iata", nullable = false, length = 3)
  private String destinationIata;

  private Double distanceKm;

  /** Returns airlines as a list. Supports multiple airlines stored as: "KQ,ET,BA". */
  @Transient
  @JsonIgnore
  public List<String> getAirlines() {
    if (airline == null || airline.isBlank()) {
      return List.of();
    }
    return Arrays.stream(airline.split(",")).map(String::trim).toList();
  }
}

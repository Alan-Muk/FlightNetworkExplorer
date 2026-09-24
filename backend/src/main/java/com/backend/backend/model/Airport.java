package com.backend.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * An airport in the global flight network.
 *
 * <p>Airports are keyed by their OpenFlights airport ID, which is assigned during import from the
 * source CSV. There is intentionally no {@code @GeneratedValue} strategy: the ID is externally
 * assigned and must be stable across re-imports so that routes remain consistent.
 */
@Entity
@Table(name = "airports")
@Getter
@Setter
public class Airport {

  /** Externally assigned (OpenFlights airport ID). Never generated, never updated. */
  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(length = 100)
  private String city;

  @Column(length = 100)
  private String country;

  @Column(nullable = false, length = 3)
  private String iata;

  @Column(length = 4)
  private String icao;

  private double latitude;

  private double longitude;
}

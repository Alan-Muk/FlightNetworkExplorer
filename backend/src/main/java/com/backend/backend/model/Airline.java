package com.backend.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * An airline operating flights in the network.
 *
 * <p>Airlines are keyed by their OpenFlights airline ID, assigned during import from the source
 * CSV. There is intentionally no {@code @GeneratedValue} strategy.
 *
 * <p>IATA and ICAO codes may be absent for some airlines (e.g. small regional carriers). Missing
 * codes are stored as {@code null}, not empty strings, so the {@code unique} constraint behaves
 * correctly.
 */
@Entity
@Table(name = "airlines")
@Getter
@Setter
public class Airline {

  /** Externally assigned (OpenFlights airline ID). Never generated, never updated. */
  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(nullable = false)
  private String name;

  private String alias;

  @Column(length = 3)
  private String iata;

  @Column(length = 4)
  private String icao;

  @Column(length = 100)
  private String country;

  /** OpenFlights uses "Y"/"N" here rather than a boolean. */
  @Column(length = 1)
  private String active;
}

package com.backend.backend.repository;

import com.backend.backend.model.Airport;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AirportRepository extends JpaRepository<Airport, Long> {

  Optional<Airport> findByIata(String iata);

  List<Airport> findByIataIn(Collection<String> iatas);

  boolean existsByIata(String iata);

  /** Airports directly reachable from the given source IATA. */
  @Query(
      """
      SELECT a FROM Airport a
      WHERE a.iata IN (
        SELECT r.destinationIata FROM Route r WHERE r.sourceIata = :iata
      )
      """)
  List<Airport> findDestinationsFrom(@Param("iata") String iata);
}

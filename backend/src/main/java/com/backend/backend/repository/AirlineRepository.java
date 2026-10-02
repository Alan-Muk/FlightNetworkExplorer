package com.backend.backend.repository;

import com.backend.backend.model.Airline;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirlineRepository extends JpaRepository<Airline, Long> {

  Optional<Airline> findByIata(String iata);

  Optional<Airline> findByIcao(String icao);
}

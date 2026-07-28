package com.backend.backend.repository;

import com.backend.backend.model.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface AirportRepository
        extends JpaRepository<Airport, Long> {


    Optional<Airport> findByIata(String iata);


    List<Airport> findByIataIn(List<String> iatas);


    List<Airport> findTop200ByOrderByIdAsc();

}
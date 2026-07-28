package com.backend.backend.repository;

import com.backend.backend.model.Airline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirlineRepository extends JpaRepository<Airline, Long> {

}
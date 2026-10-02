package com.backend.backend.controller;

import com.backend.backend.exception.AirportNotFoundException;
import com.backend.backend.model.Airport;
import com.backend.backend.repository.AirportRepository;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/airports", produces = MediaType.APPLICATION_JSON_VALUE)
public class AirportController {

  private final AirportRepository repository;

  public AirportController(AirportRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<Airport> all() {
    return repository.findAll();
  }

  @GetMapping("/{iata}")
  public Airport get(@PathVariable String iata) {
    String code = iata == null ? "" : iata.trim().toUpperCase();
    return repository.findByIata(code).orElseThrow(() -> new AirportNotFoundException(code));
  }
}

package com.backend.backend.controller;

import com.backend.backend.model.Airport;
import com.backend.backend.repository.AirportRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airports")
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
        return repository
                .findByIata(iata.toUpperCase())
                .orElseThrow();
    }
}
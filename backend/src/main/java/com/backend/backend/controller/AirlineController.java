package com.backend.backend.controller;

import com.backend.backend.dto.AirlineResponse;
import com.backend.backend.dto.RouteSummaryResponse;
import com.backend.backend.service.AirlineService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/airlines", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class AirlineController {

  private static final int MAX_PAGE_SIZE = 200;

  private final AirlineService airlineService;

  public AirlineController(AirlineService airlineService) {
    this.airlineService = airlineService;
  }

  @GetMapping
  public Page<AirlineResponse> all(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "50") @Min(1) @Max(MAX_PAGE_SIZE) int size) {
    return airlineService.findAll(PageRequest.of(page, size));
  }

  @GetMapping("/{iata}")
  public AirlineResponse byIata(
      @PathVariable
          @Pattern(regexp = "^[A-Za-z0-9]{2,3}$", message = "Airline IATA must be 2-3 characters")
          String iata) {
    return airlineService.findByIata(iata);
  }

  @GetMapping("/{iata}/routes")
  public Page<RouteSummaryResponse> routes(
      @PathVariable
          @Pattern(regexp = "^[A-Za-z0-9]{2,3}$", message = "Airline IATA must be 2-3 characters")
          String iata,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size) {
    return airlineService.findRoutes(iata, PageRequest.of(page, size));
  }
}

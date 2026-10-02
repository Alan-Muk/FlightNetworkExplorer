package com.backend.backend.service;

import com.backend.backend.dto.AirlineResponse;
import com.backend.backend.dto.RouteSummaryResponse;
import com.backend.backend.exception.AirlineNotFoundException;
import com.backend.backend.model.Airline;
import com.backend.backend.model.Route;
import com.backend.backend.repository.AirlineRepository;
import com.backend.backend.repository.RouteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read-only queries about airlines and the routes they operate. */
@Service
public class AirlineService {

  private final AirlineRepository airlineRepository;
  private final RouteRepository routeRepository;

  public AirlineService(AirlineRepository airlineRepository, RouteRepository routeRepository) {
    this.airlineRepository = airlineRepository;
    this.routeRepository = routeRepository;
  }

  @Transactional(readOnly = true)
  public Page<AirlineResponse> findAll(Pageable pageable) {
    return airlineRepository.findAll(pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public AirlineResponse findByIata(String iata) {
    String code = iata == null ? "" : iata.trim().toUpperCase();
    return airlineRepository
        .findByIata(code)
        .map(this::toResponse)
        .orElseThrow(() -> new AirlineNotFoundException(code));
  }

  @Transactional(readOnly = true)
  public Page<RouteSummaryResponse> findRoutes(String iata, Pageable pageable) {
    String code = iata == null ? "" : iata.trim().toUpperCase();

    // Ensure the airline exists before listing its routes
    if (airlineRepository.findByIata(code).isEmpty()) {
      throw new AirlineNotFoundException(code);
    }

    return routeRepository.findByAirline(code, pageable).map(this::toRouteSummary);
  }

  private AirlineResponse toResponse(Airline airline) {
    return new AirlineResponse(
        airline.getId(),
        airline.getIata(),
        airline.getIcao(),
        airline.getName(),
        airline.getAlias(),
        airline.getCountry(),
        airline.getActive());
  }

  private RouteSummaryResponse toRouteSummary(Route route) {
    return new RouteSummaryResponse(
        route.getId(),
        route.getAirline(),
        route.getSourceIata(),
        route.getDestinationIata(),
        route.getDistanceKm());
  }
}

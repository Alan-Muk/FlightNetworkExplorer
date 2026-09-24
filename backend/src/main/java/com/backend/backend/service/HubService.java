package com.backend.backend.service;

import com.backend.backend.dto.HubDTO;
import com.backend.backend.model.Airport;
import com.backend.backend.repository.AirportRepository;
import com.backend.backend.repository.RouteRepository;
import com.backend.backend.repository.RouteRepository.AirportCount;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HubService {

  private static final Logger log = LoggerFactory.getLogger(HubService.class);
  private static final int DEFAULT_LIMIT = 150;
  private static final int SCORE_CANDIDATE_POOL = 500;

  private final AirportRepository airportRepository;
  private final RouteRepository routeRepository;

  public HubService(AirportRepository airportRepository, RouteRepository routeRepository) {
    this.airportRepository = airportRepository;
    this.routeRepository = routeRepository;
  }

  @Transactional(readOnly = true)
  public List<HubDTO> getMajorHubs() {
    return getMajorHubs(DEFAULT_LIMIT);
  }

  @Transactional(readOnly = true)
  public List<HubDTO> getMajorHubs(int limit) {
    int poolSize = Math.max(limit * 2, SCORE_CANDIDATE_POOL);

    Map<String, Integer> scores = new HashMap<>();

    for (AirportCount row : routeRepository.findTopDepartureAirports(PageRequest.of(0, poolSize))) {
      scores.merge(row.getIata(), row.getCount().intValue(), Integer::sum);
    }
    for (AirportCount row : routeRepository.findTopArrivalAirports(PageRequest.of(0, poolSize))) {
      scores.merge(row.getIata(), row.getCount().intValue(), Integer::sum);
    }

    List<String> hubIatas =
        scores.entrySet().stream()
            .sorted(
                Map.Entry.<String, Integer>comparingByValue()
                    .reversed()
                    .thenComparing(Map.Entry.comparingByKey()))
            .limit(limit)
            .map(Map.Entry::getKey)
            .toList();

    log.debug("Computed top {} hubs across {} scored airports", hubIatas.size(), scores.size());

    Map<String, Airport> airports =
        airportRepository.findByIataIn(hubIatas).stream()
            .collect(Collectors.toMap(Airport::getIata, a -> a, (a, b) -> a));

    return hubIatas.stream()
        .map(airports::get)
        .filter(Objects::nonNull)
        .map(
            airport ->
                new HubDTO(
                    airport.getIata(),
                    airport.getName(),
                    airport.getCity(),
                    airport.getCountry(),
                    airport.getLatitude(),
                    airport.getLongitude(),
                    scores.get(airport.getIata())))
        .toList();
  }
}

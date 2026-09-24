package com.backend.backend.repository;

import com.backend.backend.model.Route;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RouteRepository extends JpaRepository<Route, Long> {

  List<Route> findBySourceIata(String sourceIata);

  List<Route> findByDestinationIata(String destinationIata);

  List<Route> findBySourceIataAndDestinationIata(String sourceIata, String destinationIata);

  long countBySourceIata(String sourceIata);

  long countByDestinationIata(String destinationIata);

  /** Projection for airport degree counts. */
  interface AirportCount {
    String getIata();

    Long getCount();
  }

  /**
   * Distinct departure destinations per source airport, ranked by count descending.
   *
   * <p>Uses {@code COUNT(DISTINCT destinationIata)} so multiple airlines flying the same physical
   * route count once. This measures connectivity, not route-row volume.
   */
  @Query(
      """
      SELECT r.sourceIata AS iata, COUNT(DISTINCT r.destinationIata) AS count
      FROM Route r
      GROUP BY r.sourceIata
      ORDER BY COUNT(DISTINCT r.destinationIata) DESC
      """)
  List<AirportCount> findTopDepartureAirports(Pageable pageable);

  /**
   * Distinct arrival origins per destination airport, ranked by count descending.
   *
   * <p>Uses {@code COUNT(DISTINCT sourceIata)} for the same reason as departures.
   */
  @Query(
      """
      SELECT r.destinationIata AS iata, COUNT(DISTINCT r.sourceIata) AS count
      FROM Route r
      GROUP BY r.destinationIata
      ORDER BY COUNT(DISTINCT r.sourceIata) DESC
      """)
  List<AirportCount> findTopArrivalAirports(Pageable pageable);
}

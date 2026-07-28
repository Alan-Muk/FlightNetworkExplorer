package com.backend.backend.repository;

import com.backend.backend.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface RouteRepository
        extends JpaRepository<Route, Long> {


    List<Route> findBySourceIata(
            String sourceIata
    );


    List<Route> findByDestinationIata(
            String destinationIata
    );


    List<Route> findBySourceIataAndDestinationIata(
            String sourceIata,
            String destinationIata
    );


    @Query("""
        SELECT r.sourceIata, COUNT(r)
        FROM Route r
        GROUP BY r.sourceIata
        ORDER BY COUNT(r) DESC
    """)
    List<Object[]> findTopDepartureAirports();



    @Query("""
        SELECT r.destinationIata, COUNT(r)
        FROM Route r
        GROUP BY r.destinationIata
        ORDER BY COUNT(r) DESC
    """)
    List<Object[]> findTopArrivalAirports();

}
package com.backend.backend.service;

import com.backend.backend.dto.RouteComparisonResponse;
import com.backend.backend.dto.RouteDetailsResponse;
import com.backend.backend.dto.RouteOption;
import com.backend.backend.model.Airport;
import com.backend.backend.model.Route;
import com.backend.backend.repository.AirportRepository;
import com.backend.backend.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.*;

import java.util.stream.Collectors;


@Service
public class RouteService {


    private final AirportRepository airportRepository;

    private final RouteRepository routeRepository;

    private final GraphServiceClient graphServiceClient;



    public RouteService(
            AirportRepository airportRepository,
            RouteRepository routeRepository,
            GraphServiceClient graphServiceClient
    ) {

        this.airportRepository = airportRepository;
        this.routeRepository = routeRepository;
        this.graphServiceClient = graphServiceClient;

    }



    public RouteDetailsResponse getRoute(
            String from,
            String to
    ) {


        Airport origin =
                airportRepository.findByIata(from)
                        .orElseThrow();


        Airport destination =
                airportRepository.findByIata(to)
                        .orElseThrow();



        List<Route> routes =
                routeRepository
                        .findBySourceIataAndDestinationIata(
                                from,
                                to
                        );



        double distance =
                haversine(
                        origin.getLatitude(),
                        origin.getLongitude(),
                        destination.getLatitude(),
                        destination.getLongitude()
                );



        RouteDetailsResponse response =
                new RouteDetailsResponse();



        response.setFrom(from);

        response.setTo(to);

        response.setDistanceKm(
                Math.round(distance)
        );

        response.setEstimatedFlightTime(
                estimateTime(distance)
        );


        response.setDirect(
                !routes.isEmpty()
        );



        response.setAirlines(

                routes.stream()

                        .map(Route::getAirline)

                        .filter(Objects::nonNull)

                        .distinct()

                        .sorted()

                        .toList()

        );


        return response;

    }





public RouteComparisonResponse compareRoutes(
        String from,
        String to
) {


    Map pathResponse =
            graphServiceClient.paths(
                    from,
                    to
            );


    List<List<String>> paths =
            (List<List<String>>)
                    pathResponse.get("paths");


    RouteComparisonResponse response =
            new RouteComparisonResponse();



    if (
        paths == null ||
        paths.isEmpty()
    ) {

        response.setRoutes(
                List.of()
        );

        return response;

    }



    List<RouteOption> options =
            paths.stream()

                    .map(path ->
                            buildOptionFromAirports(
                                    from,
                                    to,
                                    path
                            )
                    )

                    .collect(
                            Collectors.toList()
                    );



    rankRoutes(options);


    response.setRoutes(options);


    return response;

}




    private RouteOption buildOptionFromAirports(
            String from,
            String to,
            List<String> airports
    ) {

        RouteOption option =
                new RouteOption();


        double distance = 0;

        List<String> airlines =
                new ArrayList<>();


        for (int i = 0; i < airports.size() - 1; i++) {


            String source =
                    airports.get(i);

            String destination =
                    airports.get(i + 1);


            List<Route> routes =
                    routeRepository
                            .findBySourceIataAndDestinationIata(
                                    source,
                                    destination
                            );


            for (Route route : routes) {

                if (route.getDistanceKm() != null) {

                    distance += route.getDistanceKm();

                }


                airlines.addAll(
                        route.getAirlines()
                );

            }

        }


        option.setId(
                String.join(
                        "-",
                        airports
                )
        );


        option.setFrom(from);

        option.setTo(to);

        option.setAirports(
                airports
        );


        option.setStops(
                Math.max(
                        0,
                        airports.size() - 2
                )
        );


        option.setDistanceKm(
                distance
        );


        option.setEstimatedFlightTime(
                estimateTime(distance)
        );


        option.setAirlines(
                airlines.stream()
                        .distinct()
                        .sorted()
                        .toList()
        );


        option.setColour(
                "#ffffff"
        );


        return option;

    }





    private RouteOption buildOption(
            String from,
            String to,
            List<Route> path
    ) {


        RouteOption option =
                new RouteOption();



        List<String> airports =
                new ArrayList<>();


        airports.add(from);



        double distance = 0;


        List<String> airlines =
                new ArrayList<>();



        for(Route route : path) {


            airports.add(
                    route.getDestinationIata()
            );


            if(route.getDistanceKm()!=null) {

                distance +=
                        route.getDistanceKm();

            }



            airlines.addAll(
                    route.getAirlines()
            );

        }




        option.setId(
                String.join(
                        "-",
                        airports
                )
        );


        option.setFrom(from);

        option.setTo(to);


        option.setAirports(
                airports
        );


        option.setStops(
                Math.max(
                        0,
                        airports.size()-2
                )
        );


        option.setDistanceKm(
                distance
        );


        option.setEstimatedFlightTime(
                estimateTime(distance)
        );


        option.setAirlines(

                airlines.stream()
                        .distinct()
                        .sorted()
                        .toList()

        );


        option.setColour(
                "#ffffff"
        );


        return option;

    }







    private void rankRoutes(
            List<RouteOption> routes
    ) {


        if(routes.isEmpty())
            return;




        RouteOption shortest =
                routes.stream()
                        .min(
                                Comparator.comparingDouble(
                                        RouteOption::getDistanceKm
                                )
                        )
                        .orElse(null);



        if(shortest!=null) {

            shortest.setShortest(true);

            shortest.setColour(
                    "#00ff88"
            );

        }





        RouteOption longest =
                routes.stream()
                        .max(
                                Comparator.comparingDouble(
                                        RouteOption::getDistanceKm
                                )
                        )
                        .orElse(null);



        if(longest!=null) {

            longest.setColour(
                    "#ff4444"
            );

        }






        RouteOption fastest =
                routes.stream()
                        .min(
                                Comparator.comparing(
                                        RouteOption::getEstimatedFlightTime
                                )
                        )
                        .orElse(null);



        if(fastest!=null) {

            fastest.setFastest(true);

            fastest.setColour(
                    "#00ffff"
            );

        }





        RouteOption fewestStops =
                routes.stream()

                        .min(
                                Comparator.comparingInt(
                                        RouteOption::getStops
                                )
                        )

                        .orElse(null);



        if(fewestStops!=null) {

            fewestStops.setLeastConnected(true);

        }





        RouteOption mostStops =
                routes.stream()

                        .max(
                                Comparator.comparingInt(
                                        RouteOption::getStops
                                )
                        )

                        .orElse(null);



        if(mostStops!=null) {

            mostStops.setMostConnected(true);

        }

    }








    private double haversine(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {


        double R = 6371;


        double dLat =
                Math.toRadians(
                        lat2-lat1
                );


        double dLon =
                Math.toRadians(
                        lon2-lon1
                );



        double a =
                Math.sin(dLat/2)
                        *
                Math.sin(dLat/2)

                +

                Math.cos(
                        Math.toRadians(lat1)
                )
                *
                Math.cos(
                        Math.toRadians(lat2)
                )
                *
                Math.sin(dLon/2)
                *
                Math.sin(dLon/2);



        return R *
                2 *
                Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1-a)
                );

    }






    private String estimateTime(
            double distance
    ) {


        int hours =
                (int)Math.ceil(
                        distance / 800
                );


        return hours + "h";

    }

}
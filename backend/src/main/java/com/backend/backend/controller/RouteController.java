package com.backend.backend.controller;

import com.backend.backend.dto.RouteDetailsResponse;
import com.backend.backend.dto.RouteComparisonResponse;
import com.backend.backend.service.RouteService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "http://localhost:5173")
public class RouteController {


    private final RouteService service;



    public RouteController(
            RouteService service
    ) {

        this.service = service;

    }




    @GetMapping("/{from}/{to}")
    public RouteDetailsResponse route(

            @PathVariable String from,

            @PathVariable String to

    ) {


        return service.getRoute(

                from.toUpperCase(),

                to.toUpperCase()

        );

    }





    @GetMapping("/compare/{from}/{to}")
    public RouteComparisonResponse compare(

            @PathVariable String from,

            @PathVariable String to

    ) {


        return service.compareRoutes(

                from.toUpperCase(),

                to.toUpperCase()

        );

    }


}
package com.backend.backend.controller;


import com.backend.backend.dto.AirportStatsResponse;
import com.backend.backend.service.AirportStatsService;

import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/airport")
public class StatsController {


    private final AirportStatsService service;



    public StatsController(
            AirportStatsService service
    ){

        this.service = service;

    }



    @GetMapping("/{iata}/stats")
    public AirportStatsResponse stats(

            @PathVariable String iata

    ){

        return service.getStats(iata);

    }

}
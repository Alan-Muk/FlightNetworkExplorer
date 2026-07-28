package com.backend.backend.controller;

import com.backend.backend.dto.NetworkResponse;
import com.backend.backend.service.NetworkService;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/network")
public class NetworkController {


    private final NetworkService networkService;


    public NetworkController(
            NetworkService networkService
    ) {

        this.networkService = networkService;

    }



    @GetMapping("/{iata}")
    public NetworkResponse getNetwork(
            @PathVariable String iata
    ) {

        return networkService.getNetwork(iata);

    }

}
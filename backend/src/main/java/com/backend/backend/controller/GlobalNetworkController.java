package com.backend.backend.controller;

import com.backend.backend.dto.NetworkResponse;
import com.backend.backend.service.GlobalNetworkService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/network")
public class GlobalNetworkController {


    private final GlobalNetworkService service;


    public GlobalNetworkController(
            GlobalNetworkService service
    ) {
        this.service = service;
    }


    @GetMapping("/all")
    public NetworkResponse all() {

        return service.getAll();

    }
}
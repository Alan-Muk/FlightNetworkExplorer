package com.backend.backend.controller;

import com.backend.backend.service.GraphServiceClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/graph")
public class GraphController {

    private final GraphServiceClient graph;

    public GraphController(
            GraphServiceClient graph
    ) {
        this.graph = graph;
    }


    @GetMapping("/connections/{airport}")
    public Map connections(
            @PathVariable String airport
    ) {
        return graph.connections(
                airport.toUpperCase()
        );
    }


    @GetMapping("/path/{from}/{to}")
    public Map path(
            @PathVariable String from,
            @PathVariable String to
    ) {
        return graph.path(
                from.toUpperCase(),
                to.toUpperCase()
        );
    }
}
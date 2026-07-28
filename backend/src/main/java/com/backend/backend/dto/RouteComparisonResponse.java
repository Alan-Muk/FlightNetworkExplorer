package com.backend.backend.dto;


import java.util.List;


public class RouteComparisonResponse {


    private List<RouteOption> routes;



    public List<RouteOption> getRoutes() {

        return routes;

    }



    public void setRoutes(
            List<RouteOption> routes
    ) {

        this.routes = routes;

    }

}
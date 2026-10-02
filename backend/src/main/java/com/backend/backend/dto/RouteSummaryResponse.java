package com.backend.backend.dto;

/**
 * Lightweight route representation, used when listing routes in bulk (e.g. all routes operated by
 * an airline).
 */
public record RouteSummaryResponse(
    Long id, String airline, String sourceIata, String destinationIata, Double distanceKm) {}

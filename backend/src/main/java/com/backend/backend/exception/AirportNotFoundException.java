package com.backend.backend.exception;

/** Thrown when an airport lookup fails because no airport matches the given IATA code. */
public class AirportNotFoundException extends RuntimeException {

  public AirportNotFoundException(String iata) {
    super("Airport not found: " + iata);
  }
}

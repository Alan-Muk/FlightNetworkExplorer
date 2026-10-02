package com.backend.backend.exception;

/** Thrown when an airline lookup fails because no airline matches the given IATA code. */
public class AirlineNotFoundException extends RuntimeException {

  public AirlineNotFoundException(String iata) {
    super("Airline not found: " + iata);
  }
}

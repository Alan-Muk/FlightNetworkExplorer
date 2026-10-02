package com.backend.backend.exception;

/** Thrown when importing airports from the source dataset fails. */
public class AirportImportException extends RuntimeException {

  public AirportImportException(String message) {
    super(message);
  }

  public AirportImportException(String message, Throwable cause) {
    super(message, cause);
  }
}

package com.backend.backend.exception;

/** Thrown when importing airlines from the source dataset fails. */
public class AirlineImportException extends RuntimeException {

  public AirlineImportException(String message) {
    super(message);
  }

  public AirlineImportException(String message, Throwable cause) {
    super(message, cause);
  }
}

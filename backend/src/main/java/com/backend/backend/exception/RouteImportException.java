package com.backend.backend.exception;

/** Thrown when importing routes from the source dataset fails. */
public class RouteImportException extends RuntimeException {

  public RouteImportException(String message) {
    super(message);
  }

  public RouteImportException(String message, Throwable cause) {
    super(message, cause);
  }
}

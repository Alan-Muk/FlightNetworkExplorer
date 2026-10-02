package com.backend.backend.exception;

/** Thrown when the external Python graph service cannot be reached or returns a server error. */
public class GraphServiceUnavailableException extends RuntimeException {

  public GraphServiceUnavailableException(String url, Throwable cause) {
    super("Graph service at " + url + " is unavailable. Please ensure it is running.", cause);
  }
}

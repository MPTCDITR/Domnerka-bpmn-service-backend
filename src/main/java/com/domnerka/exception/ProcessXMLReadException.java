package com.domnerka.exception;

public class ProcessXMLReadException extends RuntimeException {
    public ProcessXMLReadException(String message) {
        super(message);
    }
  public ProcessXMLReadException(String message, Throwable cause) {
    super(message, cause);
  }
}

package org.gusdb.wsf.service;

public class WsfServiceException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -9161875379915097906L;

  public WsfServiceException() {
  }

  public WsfServiceException(String message) {
    super(message);
  }

  public WsfServiceException(Throwable cause) {
    super(cause);
  }

  public WsfServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public WsfServiceException(String message, Throwable cause,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
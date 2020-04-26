package org.kobjects.asde.lang.exceptions;

/**
 * Thrown when the user requested a program stop or it was forced extenally somehow.
 */
public class ForcedStopException extends RuntimeException {
  public ForcedStopException(InterruptedException cause) {
    super(cause);
  }
}

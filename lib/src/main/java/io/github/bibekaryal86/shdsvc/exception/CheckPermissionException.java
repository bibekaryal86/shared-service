package io.github.bibekaryal86.shdsvc.exception;

public class CheckPermissionException extends RuntimeException {
  public CheckPermissionException(final String message) {
    super("Permission Denied: " + message);
  }
}

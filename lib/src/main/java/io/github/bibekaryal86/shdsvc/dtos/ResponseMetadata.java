package io.github.bibekaryal86.shdsvc.dtos;

import java.io.Serializable;

public record ResponseMetadata(ResponseStatusInfo responseStatusInfo) implements Serializable {
  @Override
  public String toString() {
    return "ResponseMetadata{" + "responseStatusInfo=" + responseStatusInfo.errMsg() + '}';
  }

  public record ResponseStatusInfo(String errMsg) implements Serializable {}
}

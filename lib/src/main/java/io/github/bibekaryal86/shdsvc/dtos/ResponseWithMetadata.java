package io.github.bibekaryal86.shdsvc.dtos;

import java.util.Objects;

public class ResponseWithMetadata {
  private ResponseMetadata responseMetadata;

  public ResponseMetadata getResponseMetadata() {
    return responseMetadata;
  }

  public void setResponseMetadata(ResponseMetadata responseMetadata) {
    this.responseMetadata = responseMetadata;
  }

  public ResponseWithMetadata() {}

  public ResponseWithMetadata(ResponseMetadata responseMetadata) {
    this.responseMetadata = responseMetadata;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof ResponseWithMetadata that)) return false;
    return Objects.equals(responseMetadata, that.responseMetadata);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(responseMetadata);
  }

  @Override
  public String toString() {
    return "ResponseWithMetadata{" + "responseMetadata=" + responseMetadata + '}';
  }
}

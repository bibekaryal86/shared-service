package io.github.bibekaryal86.shdsvc.dtos;

import java.io.Serializable;

public record ResponseMetadata(
    ResponseStatusInfo responseStatusInfo,
    ResponseCrudInfo responseCrudInfo,
    ResponsePageInfo responsePageInfo)
    implements Serializable {
  @Override
  public String toString() {
    return "ResponseMetadata{"
        + "responseStatusInfo="
        + responseStatusInfo
        + ", responseCrudInfo="
        + responseCrudInfo
        + ", responsePageInfo="
        + responsePageInfo
        + '}';
  }

  public record ResponseStatusInfo(String errMsg) implements Serializable {}

  public record ResponseCrudInfo(
      int insertedRowsCount, int updatedRowsCount, int deletedRowsCount, int restoredRowsCount)
      implements Serializable {}

  public record ResponsePageInfo(int totalItems, int totalPages, int pageNumber, int perPage)
      implements Serializable {}
}

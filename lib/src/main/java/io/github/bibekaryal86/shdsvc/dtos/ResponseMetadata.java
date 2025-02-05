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

  public ResponseMetadata emptyResponseMetadata() {
    return new ResponseMetadata(emptyResponseStatusInfo(), emptyResponseCrudInfo(), emptyResponsePageInfo());
  }

  public ResponseStatusInfo emptyResponseStatusInfo() {
    return new ResponseStatusInfo("");
  }

  public ResponseCrudInfo emptyResponseCrudInfo() {
    return new ResponseCrudInfo(0, 0, 0, 0);
  }

  public ResponsePageInfo emptyResponsePageInfo() {
    return new ResponsePageInfo(0, 0, 0, 0);
  }
}

package shared.service.dtos;

import java.io.Serializable;

public record HttpResponse<T>(int statusCode, T responseBody) implements Serializable {}

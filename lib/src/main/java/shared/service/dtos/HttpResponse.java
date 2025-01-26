package shared.service.dtos;

public record HttpResponse<T>(int statusCode, T responseBody) {}

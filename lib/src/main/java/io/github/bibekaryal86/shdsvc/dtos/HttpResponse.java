package io.github.bibekaryal86.shdsvc.dtos;

import java.io.Serializable;
import java.util.Map;

public record HttpResponse<T>(int statusCode, T responseBody, Map<String, String> xResponseHeaders)
    implements Serializable {}

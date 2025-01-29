package io.github.bibekaryal86.shdsvc.dtos;

import java.io.Serializable;

public record HttpResponse<T>(int statusCode, T responseBody) implements Serializable {}

package io.github.bibekaryal86.shdsvc.dtos;

import java.io.Serializable;

public record EmailResponse(String requestId, int status, int count, int total, String rawResponse)
    implements Serializable {}

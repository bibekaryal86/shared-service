package io.github.bibekaryal86.shdsvc.helpers;

import java.io.IOException;
import java.util.UUID;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OkHttpLogging implements Interceptor {

  private static final Logger logger = LoggerFactory.getLogger(OkHttpLogging.class);

  @NotNull
  @Override
  public Response intercept(final Chain chain) throws IOException {
    long startTime = System.nanoTime();
    UUID requestId = UUID.randomUUID();

    Request request = chain.request();
    logger.debug("[{}] Request: [{}] [{}]", requestId, request.method(), request.url());

    Response response = chain.proceed(request);
    long endTime = System.nanoTime();

    logger.debug(
        "[{}] Response: [{}] in [{}s]",
        requestId,
        response.code(),
        String.format("%.2f", (endTime - startTime) / 1e9d));
    return response;
  }
}

package io.github.bibekaryal86.shdsvc;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.bibekaryal86.shdsvc.dtos.Enums;
import io.github.bibekaryal86.shdsvc.dtos.HttpResponse;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import io.github.bibekaryal86.shdsvc.helpers.OkHttpLogging;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connector {
  private static final Logger logger = LoggerFactory.getLogger(Connector.class);

  private static final OkHttpClient okHttpClient =
      new OkHttpClient.Builder()
          .connectTimeout(5, TimeUnit.SECONDS)
          .readTimeout(15, TimeUnit.SECONDS)
          .writeTimeout(15, TimeUnit.SECONDS)
          .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
          .addInterceptor(new OkHttpLogging())
          .build();

  public static <T> HttpResponse<T> sendRequest(
      final String url,
      final Enums.HttpMethod method,
      final TypeReference<T> valueTypeRef,
      final String authorization,
      final Map<String, String> headers,
      final Object requestBody) {
    Request request = buildRequest(method, url, authorization, headers, requestBody);

    try (Response response = okHttpClient.newCall(request).execute()) {
      int responseCode = response.code();

      Map<String, String> xResponseHeaders =
          response.headers().toMultimap().entrySet().stream()
              .filter(entry -> entry.getKey().toLowerCase().startsWith("x-"))
              .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getFirst()));

      if (response.body() == null || response.body().contentLength() == 0) {
        return new HttpResponse<>(responseCode, null, xResponseHeaders);
      }

      T responseBody =
          CommonUtilities.objectMapperProvider().readValue(response.body().string(), valueTypeRef);
      return new HttpResponse<>(responseCode, responseBody, xResponseHeaders);
    } catch (Exception ex) {
      logger.error("Send Request: [{}]|[{}]", method, url, ex);
      return new HttpResponse<>(503, null, Collections.emptyMap());
    }
  }

  private static Request buildRequest(
      final Enums.HttpMethod method,
      final String url,
      final String authorization,
      final Map<String, String> headers,
      final Object requestBody) {
    final RequestBody body =
        (method.equals(Enums.HttpMethod.GET) || Objects.isNull(requestBody))
            ? null
            : RequestBody.create(
                CommonUtilities.writeValueAsStringNoEx(requestBody),
                MediaType.parse("application/json"));
    final Request.Builder requestBuilder =
        new Request.Builder().url(url).method(method.name(), body);

    if (!CommonUtilities.isEmpty(authorization)) {
      requestBuilder.header("Authorization", authorization);
    }

    if (!CommonUtilities.isEmpty(headers)) {
      for (final Map.Entry<String, String> entry : headers.entrySet()) {
        requestBuilder.header(entry.getKey(), entry.getValue());
      }
    }

    return requestBuilder.build();
  }
}

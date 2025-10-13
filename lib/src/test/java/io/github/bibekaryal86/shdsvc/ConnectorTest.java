package io.github.bibekaryal86.shdsvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.bibekaryal86.shdsvc.dtos.Enums;
import io.github.bibekaryal86.shdsvc.dtos.HttpResponse;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.util.List;
import java.util.Map;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class ConnectorTest {

  private OkHttpClient mockClient;
  private Call mockCall;
  private Response mockResponse;

  @BeforeEach
  public void setup() {
    mockClient = mock(OkHttpClient.class);
    mockCall = mock(Call.class);
    mockResponse = mock(Response.class);
    Connector.overrideClient(mockClient);
  }

  @Test
  public void testSendRequest_successfulJsonResponse() throws Exception {
    String url = "https://example.com/api";
    String json = "{\"message\":\"ok\"}";
    Map<String, List<String>> headers = Map.of("x-trace-id", List.of("abc123"));

    // Mock response body
    ResponseBody body = ResponseBody.create(json, MediaType.get("application/json"));
    Headers okHeaders = Headers.of("x-trace-id", "abc123");

    when(mockResponse.code()).thenReturn(200);
    when(mockResponse.body()).thenReturn(body);
    when(mockResponse.headers()).thenReturn(okHeaders);
    when(mockResponse.request()).thenReturn(new Request.Builder().url(url).build());

    when(mockCall.execute()).thenReturn(mockResponse);
    when(mockClient.newCall(any())).thenReturn(mockCall);

    try (MockedStatic<CommonUtilities> utilities = mockStatic(CommonUtilities.class)) {
      utilities.when(CommonUtilities::objectMapperProvider).thenCallRealMethod();
      utilities.when(() -> CommonUtilities.writeValueAsStringNoEx(any())).thenReturn("{}");
      utilities.when(() -> CommonUtilities.isEmpty(anyString())).thenReturn(true);
      utilities.when(() -> CommonUtilities.isEmpty(anyMap())).thenReturn(false);

      TypeReference<Map<String, String>> typeRef = new TypeReference<>() {};
      HttpResponse<Map<String, String>> response =
          Connector.sendRequest(
              url,
              Enums.HttpMethod.POST,
              typeRef,
              "Bearer token",
              Map.of("X-Custom", "yes"),
              Map.of("key", "value"));

      assertEquals(200, response.statusCode());
      assertEquals("ok", response.responseBody().get("message"));
      assertEquals("abc123", response.xResponseHeaders().get("x-trace-id"));
    }
  }

  @Test
  public void testSendRequestNoEx_handlesExceptionGracefully() throws Exception {
    String url = "https://example.com/fail";

    when(mockCall.execute()).thenThrow(new RuntimeException("Simulated failure"));
    when(mockClient.newCall(any())).thenReturn(mockCall);

    try (MockedStatic<CommonUtilities> utilities = mockStatic(CommonUtilities.class)) {
      utilities.when(() -> CommonUtilities.writeValueAsStringNoEx(any())).thenReturn("{}");
      utilities.when(() -> CommonUtilities.isEmpty(anyString())).thenReturn(false);
      utilities.when(() -> CommonUtilities.isEmpty(anyMap())).thenReturn(false);

      HttpResponse<Object> response =
          Connector.sendRequestNoEx(
              url, Enums.HttpMethod.GET, new TypeReference<>() {}, null, Map.of(), null);

      assertEquals(503, response.statusCode());
      assertNull(response.responseBody());
      assertTrue(response.xResponseHeaders().isEmpty());
    }
  }

  @Test
  public void testSendRequest_emptyResponseBody() throws Exception {
    String url = "https://example.com/empty";
    ResponseBody emptyBody = ResponseBody.create("", MediaType.get("application/json"));
    Headers headers = Headers.of("x-trace-id", "abc123");

    when(mockResponse.code()).thenReturn(204);
    when(mockResponse.body()).thenReturn(emptyBody);
    when(mockResponse.headers()).thenReturn(headers);
    when(mockResponse.request()).thenReturn(new Request.Builder().url(url).build());
    when(mockCall.execute()).thenReturn(mockResponse);
    when(mockClient.newCall(any())).thenReturn(mockCall);

    try (MockedStatic<CommonUtilities> utilities = mockStatic(CommonUtilities.class)) {
      utilities.when(CommonUtilities::objectMapperProvider).thenCallRealMethod();
      utilities.when(() -> CommonUtilities.writeValueAsStringNoEx(any())).thenReturn("{}");
      utilities.when(() -> CommonUtilities.isEmpty(anyString())).thenReturn(false);
      utilities.when(() -> CommonUtilities.isEmpty(anyMap())).thenReturn(false);

      HttpResponse<Object> response =
          Connector.sendRequest(
              url, Enums.HttpMethod.GET, new TypeReference<>() {}, "test-auth", Map.of(), null);

      assertEquals(204, response.statusCode());
      assertNull(response.responseBody());
      assertEquals("abc123", response.xResponseHeaders().get("x-trace-id"));
    }
  }

  @Test
  public void testSendRequest_noXHeaders() throws Exception {
    String url = "https://example.com/noheaders";
    ResponseBody body =
        ResponseBody.create("{\"status\":\"ok\"}", MediaType.get("application/json"));
    Headers headers = Headers.of("Content-Type", "application/json");

    when(mockResponse.code()).thenReturn(200);
    when(mockResponse.body()).thenReturn(body);
    when(mockResponse.headers()).thenReturn(headers);
    when(mockResponse.request()).thenReturn(new Request.Builder().url(url).build());
    when(mockCall.execute()).thenReturn(mockResponse);
    when(mockClient.newCall(any())).thenReturn(mockCall);

    try (MockedStatic<CommonUtilities> utilities = mockStatic(CommonUtilities.class)) {
      utilities.when(CommonUtilities::objectMapperProvider).thenCallRealMethod();
      utilities.when(() -> CommonUtilities.writeValueAsStringNoEx(any())).thenReturn("{}");
      utilities.when(() -> CommonUtilities.isEmpty(anyString())).thenReturn(false);
      utilities.when(() -> CommonUtilities.isEmpty(anyMap())).thenReturn(false);

      HttpResponse<Map<String, String>> response =
          Connector.sendRequest(
              url, Enums.HttpMethod.GET, new TypeReference<>() {}, "test-auth", Map.of(), null);

      assertEquals(200, response.statusCode());
      assertTrue(response.xResponseHeaders().isEmpty());
    }
  }

  @Test
  public void testSendRequest_nullRequestBodyWithPost_shouldThrow() throws Exception {
    String url = "https://example.com/post";

    try (MockedStatic<CommonUtilities> utilities = mockStatic(CommonUtilities.class)) {
      utilities.when(() -> CommonUtilities.writeValueAsStringNoEx(null)).thenReturn(null);
      utilities.when(() -> CommonUtilities.isEmpty(anyString())).thenReturn(true);
      utilities.when(() -> CommonUtilities.isEmpty(anyMap())).thenReturn(true);

      assertThrows(
          IllegalArgumentException.class,
          () -> {
            Connector.sendRequest(
                url, Enums.HttpMethod.POST, new TypeReference<>() {}, null, Map.of(), null);
          });
    }
  }
}

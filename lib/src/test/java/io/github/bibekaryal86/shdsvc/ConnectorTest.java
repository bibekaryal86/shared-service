package io.github.bibekaryal86.shdsvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.bibekaryal86.shdsvc.dtos.Enums;
import io.github.bibekaryal86.shdsvc.dtos.HttpResponse;
import java.io.IOException;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

public class ConnectorTest {

  private MockWebServer mockWebServer;
  private String mockUrl;

  @BeforeEach
  void setup() throws Exception {
    try (var ignored = MockitoAnnotations.openMocks(this)) {
      mockWebServer = new MockWebServer();
      mockWebServer.start();
      mockUrl = mockWebServer.url("/").toString();
    }
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  void testSendRequestSuccess() throws IOException {
    // Arrange
    MockResponse response = new MockResponse().setResponseCode(200).setBody("{\"key\":\"value\"}");
    mockWebServer.enqueue(response);

    // Act
    HttpResponse<Map<String, String>> result =
        Connector.sendRequest(
            mockUrl, Enums.HttpMethod.GET, new TypeReference<>() {}, null, null, null);

    // Assert
    assertEquals(200, result.statusCode());
    assertNotNull(result.responseBody());
  }

  @Test
  void testSendRequestFailure() {
    // Arrange
    MockResponse response =
        new MockResponse().setResponseCode(500).setBody("{\"error\":\"message\"}");
    mockWebServer.enqueue(response);

    // Act and Assert
    HttpResponse<Map<String, String>> result =
        Connector.sendRequest(
            mockUrl, Enums.HttpMethod.GET, new TypeReference<>() {}, null, null, null);
    assertEquals(500, result.statusCode());
    assertNotNull(result.responseBody());
  }

  @Test
  void testSendRequestException() {
    // Arrange
    mockWebServer.enqueue(new MockResponse().setResponseCode(503));

    // Act and Assert
    HttpResponse<Map<String, String>> result =
        Connector.sendRequest(
            mockUrl, Enums.HttpMethod.GET, new TypeReference<>() {}, null, null, null);
    assertEquals(503, result.statusCode());
    assertNotNull(result.responseBody());
  }

  @Test
  void testSendRequestInvalidUrl() {
    // Act and Assert
    assertThrows(
        Exception.class,
        () ->
            Connector.sendRequest(
                "invalid-url", Enums.HttpMethod.GET, new TypeReference<>() {}, null, null, null));
  }
}

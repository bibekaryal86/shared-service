package io.github.bibekaryal86.shdsvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.bibekaryal86.shdsvc.dtos.EnvDetailsResponse;
import io.github.bibekaryal86.shdsvc.dtos.HttpResponse;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class AppEnvPropertyTest {

  private MockedStatic<Connector> mockConnectorStatic;
  private MockedStatic<CommonUtilities> mockCommonUtilitiesStatic;

  @BeforeEach
  void setUp() {
    // Mock CommonUtilities
    mockCommonUtilitiesStatic = mockStatic(CommonUtilities.class);
    when(CommonUtilities.getSystemEnvProperty(AppEnvProperty.ENVSVC_BASE_URL))
        .thenReturn("http://mock-api-url.com");
    when(CommonUtilities.getSystemEnvProperty(AppEnvProperty.ENVSVC_USR)).thenReturn("mockUser");
    when(CommonUtilities.getSystemEnvProperty(AppEnvProperty.ENVSVC_PWD)).thenReturn("mockPass");
    when(CommonUtilities.getBasicAuth("mockUser", "mockPass")).thenReturn("mockAuth");

    // Mock Connector
    mockConnectorStatic = mockStatic(Connector.class);
  }

  @AfterEach
  void cleanUp() {
    // Close static mocks to deregister them
    if (mockConnectorStatic != null) {
      mockConnectorStatic.close();
    }
    if (mockCommonUtilitiesStatic != null) {
      mockCommonUtilitiesStatic.close();
    }
  }

  @Test
  void testGetEnvDetailsList_Success() {
    // Arrange
    EnvDetailsResponse envDetailsResponse = new EnvDetailsResponse();
    EnvDetailsResponse.EnvDetails envDetail = new EnvDetailsResponse.EnvDetails();
    envDetail.setName("some-name");
    envDetailsResponse.setErrMsg("");
    envDetailsResponse.setEnvDetails(List.of(envDetail));
    HttpResponse<EnvDetailsResponse> mockResponse = new HttpResponse<>(200, envDetailsResponse);

    // mocks
    when(CommonUtilities.isEmpty(anyList())).thenReturn(true);
    when(CommonUtilities.isEmpty(anyString())).thenReturn(true);
    when(Connector.sendRequest(any(), any(), any(TypeReference.class), any(), any(), any()))
        .thenReturn(mockResponse);

    // Act
    List<EnvDetailsResponse.EnvDetails> result = AppEnvProperty.getEnvDetailsList("some-app", true);

    // Assert
    assertNotNull(result, "Result should not be null");
    assertEquals(1, result.size(), "Result size should be 1");
    assertEquals(envDetail, result.getFirst(), "Returned element should match the mock object");
  }

  @Test
  void testGetEnvDetailsList_ErrorInResponse() {
    // Arrange
    EnvDetailsResponse envDetailsResponse = new EnvDetailsResponse();
    envDetailsResponse.setErrMsg("some-error-message");
    envDetailsResponse.setEnvDetails(List.of());
    HttpResponse<EnvDetailsResponse> mockResponse = new HttpResponse<>(200, envDetailsResponse);

    // mocks
    when(CommonUtilities.isEmpty(anyList())).thenReturn(true);
    when(CommonUtilities.isEmpty(anyString())).thenReturn(false);
    when(Connector.sendRequest(any(), any(), any(TypeReference.class), any(), any(), any()))
        .thenReturn(mockResponse);

    // Act
    List<EnvDetailsResponse.EnvDetails> result =
        AppEnvProperty.getEnvDetailsList("some-app", false);

    // Assert
    assertEquals(0, result.size());
  }
}

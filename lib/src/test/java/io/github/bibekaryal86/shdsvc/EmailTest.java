package io.github.bibekaryal86.shdsvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bibekaryal86.shdsvc.dtos.*;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class EmailTest {

  private EmailRequest createSampleEmailRequest() {
    EmailRequest.EmailContact from =
        new EmailRequest.EmailContact("sender@example.com", "Sender Example");
    EmailRequest.EmailContact to =
        new EmailRequest.EmailContact("user@example.com", "User Example");
    EmailRequest.EmailContent content =
        new EmailRequest.EmailContent("Test Subject", "Hello HTML", "Hello Text");
    EmailRequest.EmailAttachment attachment =
        new EmailRequest.EmailAttachment("file.txt", "content", "text/plain");

    return new EmailRequest(from, List.of(to), null, content, List.of(attachment));
  }

  @Test
  public void testSendEmailNoEx_successful() {
    EmailRequest request = createSampleEmailRequest();
    EmailResponseOut mockOut = new EmailResponseOut("req-123");
    HttpResponse<EmailResponseOut> mockHttpResponse = new HttpResponse<>(200, mockOut, Map.of());

    try (MockedStatic<CommonUtilities> utilities = mockStatic(CommonUtilities.class);
        MockedStatic<Connector> connector = mockStatic(Connector.class)) {

      utilities.when(() -> CommonUtilities.getSystemEnvProperty(any())).thenReturn("value");
      utilities
          .when(() -> CommonUtilities.getBasicAuth("value", "value"))
          .thenReturn("Basic dXNlcjpwd2Q=");

      connector
          .when(() -> Connector.sendRequest(any(), any(), any(), any(), any(), any()))
          .thenReturn(mockHttpResponse);

      Email email = new Email();
      EmailResponse response = email.sendEmailNoEx(request);

      assertEquals(200, response.status());
    }
  }

  @Test
  public void testSendEmailNoEx_fallbackOnException() {
    EmailRequest request = createSampleEmailRequest();

    try (MockedStatic<CommonUtilities> utilities = mockStatic(CommonUtilities.class);
        MockedStatic<Connector> connector = mockStatic(Connector.class)) {

      utilities.when(() -> CommonUtilities.getSystemEnvProperty(any())).thenReturn("value");
      utilities
          .when(() -> CommonUtilities.getBasicAuth("value", "value"))
          .thenReturn("Basic dXNlcjpwd2Q=");

      connector
          .when(() -> Connector.sendRequest(any(), any(), any(), any(), any(), any()))
          .thenThrow(new RuntimeException("Simulated failure"));

      Email email = new Email();
      EmailResponse response = email.sendEmailNoEx(request);

      assertEquals(500, response.status());
      assertTrue(response.rawResponse().contains("Simulated failure"));
    }
  }
}

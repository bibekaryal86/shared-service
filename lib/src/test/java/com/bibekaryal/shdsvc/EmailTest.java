package com.bibekaryal.shdsvc;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.bibekaryal.shdsvc.dtos.EmailRequest;

public class EmailTest {
  private Email email;

  @Mock private MailjetClient mailjetClient;

  @BeforeEach
  void setUp() throws Exception {
    try (var ignored = MockitoAnnotations.openMocks(this)) {
      email = new Email(mailjetClient);
    }
  }

  @Test
  void testSendEmail_Success() throws Exception {
    // Arrange
    EmailRequest emailRequest = createSampleEmailRequest();
    MailjetResponse mockResponse = mock(MailjetResponse.class);
    when(mockResponse.getStatus()).thenReturn(200);
    when(mockResponse.getRawResponseContent()).thenReturn("Success");
    when(mailjetClient.post(any(MailjetRequest.class))).thenReturn(mockResponse);

    // Act
    email.sendEmail(emailRequest);

    // Assert
    ArgumentCaptor<MailjetRequest> requestCaptor = ArgumentCaptor.forClass(MailjetRequest.class);
    verify(mailjetClient, times(1)).post(requestCaptor.capture());
    MailjetRequest capturedRequest = requestCaptor.getValue();

    JSONObject requestBody = capturedRequest.getBodyJSON();
    JSONArray messages = requestBody.getJSONArray("Messages");
    JSONObject message = messages.getJSONObject(0);

    assertEquals(
        emailRequest.emailFrom().emailAddress(), message.getJSONObject("From").getString("Email"));
    assertEquals(emailRequest.emailContent().html(), message.getString("HTMLPart"));
  }

  @Test
  void testSendEmail_Exception() throws Exception {
    // Arrange
    EmailRequest emailRequest = createSampleEmailRequest();
    doThrow(new RuntimeException("Mailjet error"))
        .when(mailjetClient)
        .post(any(MailjetRequest.class));

    // Act & Assert
    assertDoesNotThrow(() -> email.sendEmail(emailRequest));
    verify(mailjetClient, times(1)).post(any(MailjetRequest.class));
  }

  private EmailRequest createSampleEmailRequest() {
    EmailRequest.EmailContact from = new EmailRequest.EmailContact("from@example.com", "Sender");
    List<EmailRequest.EmailContact> toList =
        List.of(new EmailRequest.EmailContact("to@example.com", "Receiver"));
    EmailRequest.EmailContent content =
        new EmailRequest.EmailContent("Hello", null, "<p>Hello</p>");
    List<EmailRequest.EmailAttachment> attachments =
        List.of(new EmailRequest.EmailAttachment("SGVsbG8gd29ybGQ=", "file.txt", "text/plain"));

    return new EmailRequest(from, toList, List.of(), content, attachments);
  }
}

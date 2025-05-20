package io.github.bibekaryal86.shdsvc;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import io.github.bibekaryal86.shdsvc.dtos.EmailRequest;
import io.github.bibekaryal86.shdsvc.dtos.EmailResponse;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Email {
  private static final Logger logger = LoggerFactory.getLogger(Email.class);

  public static final String ENV_MJ_PUB_KEY = "MJ_PUBLIC";
  public static final String ENV_MJ_PVT_KEY = "MJ_PRIVATE";
  private static final String ENV_MG_API_KEY = "MG_KEY";
  private static final String ENV_MG_DOMAIN = "MG_DOMAIN";

  private static final MailjetClient mailjetClient =
      new MailjetClient(
          ClientOptions.builder()
              .apiKey(CommonUtilities.getSystemEnvProperty(ENV_MJ_PUB_KEY))
              .apiSecretKey(CommonUtilities.getSystemEnvProperty(ENV_MJ_PVT_KEY))
              .build());

  private static final MailgunMessagesApi mailgunMessagesApi =
      MailgunClient.config(ENV_MG_API_KEY).createApi(MailgunMessagesApi.class);

  public EmailResponse sendEmail(final EmailRequest emailRequest) {
    final UUID requestId = UUID.randomUUID();
    logger.debug("[{}] Send Email Request: [{}]", requestId, emailRequest);

    try {
      final JSONObject message = buildMailjetMessage(emailRequest, requestId);
      final MailjetRequest request =
          new MailjetRequest(Emailv31.resource)
              .property(Emailv31.MESSAGES, new JSONArray().put(message));
      final MailjetResponse response = mailjetClient.post(request);

      logger.debug(
          "[{}] Response: [{}]-[{}]",
          requestId,
          response.getStatus(),
          response.getRawResponseContent());

      return new EmailResponse(
          requestId.toString(),
          response.getStatus(),
          response.getCount(),
          response.getTotal(),
          response.getRawResponseContent());
    } catch (Exception ex) {
      logger.error("[{}] Send Email Error", requestId, ex);
      return new EmailResponse(requestId.toString(), 500, 0, 0, ex.getMessage());
    }
  }

  public EmailResponse sendEmailMailgun(final EmailRequest emailRequest) {
    final UUID requestId = UUID.randomUUID();
    logger.debug("[{}] Send Email Mailgun Request: [{}]", requestId, emailRequest);
    List<File> attachments = new ArrayList<>();
    try {
      final Message.MessageBuilder messageBuilder = buildMailgunMessage(emailRequest);
      attachments = createAttachmentFilesNoEx(emailRequest.emailAttachments());
      messageBuilder.attachment(attachments);

      MessageResponse messageResponse =
          mailgunMessagesApi.sendMessage(ENV_MG_DOMAIN, messageBuilder.build());
      return new EmailResponse(messageResponse.getId(), 200, 0, 0, messageResponse.getMessage());

    } catch (Exception ex) {
      logger.error("[{}] Send Email Mailgun Error", requestId, ex);
      return new EmailResponse(requestId.toString(), 500, 0, 0, ex.getMessage());
    } finally {
      deleteTemporaryFiles(attachments);
    }
  }

  private JSONObject buildMailjetMessage(EmailRequest emailRequest, UUID requestId) {
    final JSONObject message =
        new JSONObject()
            .put(Emailv31.Message.CUSTOMID, requestId)
            .put(Emailv31.Message.FROM, emailContactJSONObject(emailRequest.emailFrom()))
            .put(Emailv31.Message.TO, emailContactsJSONArray(emailRequest.emailToList()));

    if (!CommonUtilities.isEmpty(emailRequest.emailCcList())) {
      message.put(Emailv31.Message.CC, emailContactsJSONArray(emailRequest.emailCcList()));
    }

    if (emailRequest.emailContent() != null) {
      EmailRequest.EmailContent emailContent = emailRequest.emailContent();

      if (!CommonUtilities.isEmpty(emailContent.subject())) {
        message.put(Emailv31.Message.SUBJECT, emailContent.subject());
      }

      if (!CommonUtilities.isEmpty(emailContent.text())) {
        message.put(Emailv31.Message.TEXTPART, emailRequest.emailContent().text());
      }

      if (!CommonUtilities.isEmpty(emailContent.html())) {
        message.put(Emailv31.Message.HTMLPART, emailRequest.emailContent().html());
      }
    }

    if (!CommonUtilities.isEmpty(emailRequest.emailAttachments())) {
      message.put(
          Emailv31.Message.ATTACHMENTS, emailAttachmentsJSONArray(emailRequest.emailAttachments()));
    }

    return message;
  }

  private JSONArray emailContactsJSONArray(final List<EmailRequest.EmailContact> emailContacts) {
    final JSONArray jsonArray = new JSONArray();
    for (EmailRequest.EmailContact emailContact : emailContacts) {
      jsonArray.put(emailContactJSONObject(emailContact));
    }
    return jsonArray;
  }

  private JSONObject emailContactJSONObject(final EmailRequest.EmailContact emailContact) {
    return new JSONObject()
        .put("Email", emailContact.emailAddress())
        .put("Name", emailContact.fullName());
  }

  private JSONArray emailAttachmentsJSONArray(
      final List<EmailRequest.EmailAttachment> emailAttachments) {
    final JSONArray jsonArray = new JSONArray();
    for (EmailRequest.EmailAttachment emailAttachment : emailAttachments) {
      jsonArray.put(
          new JSONObject()
              .put(
                  "ContentType",
                  CommonUtilities.isEmpty(emailAttachment.contentType())
                      ? "text/plain"
                      : emailAttachment.contentType())
              .put("Filename", emailAttachment.fileName())
              .put("Base64Content", emailAttachment.fileContent()));
    }
    return jsonArray;
  }

  private Message.MessageBuilder buildMailgunMessage(EmailRequest emailRequest) {
    Message.MessageBuilder messageBuilder =
        Message.builder()
            .from(emailRequest.emailFrom().emailAddress())
            .to(
                emailRequest.emailToList().stream()
                    .map(EmailRequest.EmailContact::emailAddress)
                    .toList());

    if (!CommonUtilities.isEmpty(emailRequest.emailCcList())) {
      messageBuilder.cc(
          emailRequest.emailCcList().stream()
              .map(EmailRequest.EmailContact::emailAddress)
              .toList());
    }

    if (emailRequest.emailContent() != null) {
      EmailRequest.EmailContent emailContent = emailRequest.emailContent();

      if (!CommonUtilities.isEmpty(emailContent.subject())) {
        messageBuilder.subject(emailContent.subject());
      }

      if (!CommonUtilities.isEmpty(emailContent.text())) {
        messageBuilder.text(emailContent.text());
      }

      if (!CommonUtilities.isEmpty(emailContent.html())) {
        messageBuilder.html(emailContent.html());
      }
    }
    return messageBuilder;
  }

  private static List<File> createAttachmentFilesNoEx(
      List<EmailRequest.EmailAttachment> emailAttachments) {
    try {
      return createTemporaryFiles(emailAttachments);
    } catch (Exception ex) {
      logger.error("Create Attachments Exception", ex);
      return Collections.emptyList();
    }
  }

  private static List<File> createTemporaryFiles(
      List<EmailRequest.EmailAttachment> emailAttachments) throws IOException {
    List<File> temporaryFiles = new ArrayList<>();
    for (EmailRequest.EmailAttachment emailAttachment : emailAttachments) {
      File tempFile = File.createTempFile("attachment_", "_" + emailAttachment.fileName());
      try (FileWriter writer = new FileWriter(tempFile)) {
        writer.write(emailAttachment.fileContent());
      }
      temporaryFiles.add(tempFile);
    }
    return temporaryFiles;
  }

  private static void deleteTemporaryFiles(List<File> files) {
    for (File file : files) {
      file.delete();
    }
  }
}

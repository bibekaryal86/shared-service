package io.github.bibekaryal86.shdsvc;

import static io.github.bibekaryal86.shdsvc.helpers.ConstantUtilities.ENV_EMAIL_API_PWD;
import static io.github.bibekaryal86.shdsvc.helpers.ConstantUtilities.ENV_EMAIL_API_URL;
import static io.github.bibekaryal86.shdsvc.helpers.ConstantUtilities.ENV_EMAIL_API_USER;
import static io.github.bibekaryal86.shdsvc.helpers.ConstantUtilities.ENV_MG_API_KEY;
import static io.github.bibekaryal86.shdsvc.helpers.ConstantUtilities.ENV_MG_DOMAIN;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import io.github.bibekaryal86.shdsvc.dtos.EmailRequest;
import io.github.bibekaryal86.shdsvc.dtos.EmailRequestOut;
import io.github.bibekaryal86.shdsvc.dtos.EmailResponse;
import io.github.bibekaryal86.shdsvc.dtos.EmailResponseOut;
import io.github.bibekaryal86.shdsvc.dtos.Enums;
import io.github.bibekaryal86.shdsvc.dtos.HttpResponse;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Email {
  private static final Logger log = LoggerFactory.getLogger(Email.class);

  private static MailgunMessagesApi mailgunMessagesApi = null;

  public EmailResponse sendEmail(final EmailRequest emailRequest) {
    final UUID requestId = UUID.randomUUID();
    log.debug("[{}] Send Email Request: [{}]", requestId, emailRequest);

    try {
      final String emailUrl = CommonUtilities.getSystemEnvProperty(ENV_EMAIL_API_URL);
      final String emailUser = CommonUtilities.getSystemEnvProperty(ENV_EMAIL_API_USER);
      final String emailPassword = CommonUtilities.getSystemEnvProperty(ENV_EMAIL_API_PWD);
      final String emailAuthorization = CommonUtilities.getBasicAuth(emailUser, emailPassword);

      final EmailRequestOut emailRequestOut = buildEmailMessage(emailRequest);
      final HttpResponse<EmailResponseOut> httpResponse =
          Connector.sendRequest(
              emailUrl,
              Enums.HttpMethod.POST,
              new TypeReference<EmailResponseOut>() {},
              emailAuthorization,
              Collections.emptyMap(),
              emailRequestOut);

      final EmailResponseOut emailResponseOut = httpResponse.responseBody();
      log.debug(
          "[{}] Send Email Response: Id=[{}] Code=[{}]",
          requestId,
          emailResponseOut.requestId(),
          httpResponse.statusCode());
      return new EmailResponse(emailResponseOut.toString(), httpResponse.statusCode(), 1, 1, "");
    } catch (Exception ex) {
      log.error(
          "[{}] Send Email Exception=[{}] ExMessage=[{}]",
          requestId,
          ex.getClass(),
          ex.getMessage());
      throw new RuntimeException(ex);
    }
  }

  public EmailResponse sendEmailMailgun(final EmailRequest emailRequest) {
    final UUID requestId = UUID.randomUUID();

    if (mailgunMessagesApi == null) {
      mailgunMessagesApi =
          MailgunClient.config(CommonUtilities.getSystemEnvProperty(ENV_MG_API_KEY))
              .createApi(MailgunMessagesApi.class);
    }

    log.debug("[{}] Send Email Mailgun Request: [{}]", requestId, emailRequest);
    List<File> attachments = new ArrayList<>();
    try {
      final Message.MessageBuilder messageBuilder = buildMailgunMessage(emailRequest);
      attachments = createAttachmentFilesNoEx(requestId, emailRequest.emailAttachments());
      messageBuilder.attachment(attachments);

      MessageResponse messageResponse =
          mailgunMessagesApi.sendMessage(
              CommonUtilities.getSystemEnvProperty(ENV_MG_DOMAIN), messageBuilder.build());
      return new EmailResponse(messageResponse.getId(), 200, 0, 0, messageResponse.getMessage());
    } catch (Exception ex) {
      log.error(
          "[{}] Send Email Mailgun Exception=[{}] ExMessage=[{}]",
          requestId,
          ex.getClass(),
          ex.getMessage());
      throw new RuntimeException(ex);
    } finally {
      deleteTemporaryFiles(attachments);
    }
  }

  private EmailRequestOut buildEmailMessage(final EmailRequest emailRequest) {
    final String subject = emailRequest.emailContent().subject();
    final String htmlBody = emailRequest.emailContent().html();
    final String textBody = emailRequest.emailContent().text();

    final List<String> emailToList =
        emailRequest.emailToList().stream().map(EmailRequest.EmailContact::emailAddress).toList();
    final List<String> emailCcList =
        emailRequest.emailCcList() == null
            ? Collections.emptyList()
            : emailRequest.emailCcList().stream()
                .map(EmailRequest.EmailContact::emailAddress)
                .toList();
    final List<String> emailBccList =
        Collections.emptyList(); // TODO not available in shared-service
    final EmailRequestOut.EmailRequestRecipients emailRequestRecipients =
        new EmailRequestOut.EmailRequestRecipients(emailToList, emailCcList, emailBccList);

    final List<EmailRequestOut.EmailRequestAttachment> emailRequestAttachments = new ArrayList<>();
    if (!CommonUtilities.isEmpty(emailRequest.emailAttachments())) {
      for (EmailRequest.EmailAttachment emailAttachment : emailRequest.emailAttachments()) {
        final String base64Content =
            Base64.getEncoder()
                .encodeToString(emailAttachment.fileContent().getBytes(StandardCharsets.UTF_8));
        final String fileName = emailAttachment.fileName();
        final String fileContentType = emailAttachment.contentType();
        final EmailRequestOut.EmailRequestAttachment attachment =
            new EmailRequestOut.EmailRequestAttachment(fileName, base64Content, fileContentType);
        emailRequestAttachments.add(attachment);
      }
    }

    return new EmailRequestOut(
        subject, htmlBody, textBody, emailRequestRecipients, emailRequestAttachments);
  }

  private Message.MessageBuilder buildMailgunMessage(final EmailRequest emailRequest) {
    final Message.MessageBuilder messageBuilder =
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
      final UUID requestId, final List<EmailRequest.EmailAttachment> emailAttachments) {
    try {
      return createTemporaryFiles(emailAttachments);
    } catch (Exception ex) {
      log.error(
          "[{}] Create Attachments Exception=[{}] ExMessage=[{}]",
          requestId,
          ex.getClass(),
          ex.getMessage());
      return Collections.emptyList();
    }
  }

  private static List<File> createTemporaryFiles(
      final List<EmailRequest.EmailAttachment> emailAttachments) throws IOException {
    final List<File> temporaryFiles = new ArrayList<>();
    for (final EmailRequest.EmailAttachment emailAttachment : emailAttachments) {
      final File tempFile = File.createTempFile("attachment_", "_" + emailAttachment.fileName());
      try (final FileWriter writer = new FileWriter(tempFile)) {
        writer.write(emailAttachment.fileContent());
      }
      temporaryFiles.add(tempFile);
    }
    return temporaryFiles;
  }

  private static void deleteTemporaryFiles(final List<File> files) {
    for (File file : files) {
      file.delete();
    }
  }
}

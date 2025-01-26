package shared.service;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;

import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.service.dtos.EmailRequest;
import shared.service.helpers.CommonUtilities;

public class Email {
  private static final Logger logger = LoggerFactory.getLogger(Email.class);

  public static final String ENV_MAILJET_PUBLIC_KEY = "MJ_PUBLIC";
  public static final String ENV_MAILJET_PRIVATE_KEY = "MJ_PRIVATE";

  private static final MailjetClient mailjetClient = new MailjetClient(
          ClientOptions.builder()
                  .apiKey(CommonUtilities.getSystemEnvProperty(ENV_MAILJET_PUBLIC_KEY))
                  .apiSecretKey(CommonUtilities.getSystemEnvProperty(ENV_MAILJET_PRIVATE_KEY))
                  .build());

  public static void sendEmail(final EmailRequest emailRequest) {
    final UUID requestId = UUID.randomUUID();
    logger.debug("[{}] Request: [{}]", requestId, emailRequest);

    try {
      final JSONObject message =
          new JSONObject()
              .put(Emailv31.Message.CUSTOMID, requestId)
              .put(Emailv31.Message.FROM, emailContactJSONObject(emailRequest.emailFrom()))
              .put(Emailv31.Message.TO, emailContactsJSONArray(emailRequest.emailToList()));

      if (!CommonUtilities.isEmpty(emailRequest.emailCcList())) {
        message.put(Emailv31.Message.CC, emailContactsJSONArray(emailRequest.emailCcList()));
      }

      if (!CommonUtilities.isEmpty(emailRequest.emailContent().text())) {
        message.put(Emailv31.Message.TEXTPART, emailRequest.emailContent().text());
      }

      if (!CommonUtilities.isEmpty(emailRequest.emailContent().html())) {
        message.put(Emailv31.Message.HTMLPART, emailRequest.emailContent().html());
      }

      if (!CommonUtilities.isEmpty(emailRequest.emailAttachments())) {
        message.put(Emailv31.Message.ATTACHMENTS, emailAttachmentsJSONArray(emailRequest.emailAttachments()));
      }

      final MailjetRequest request = new MailjetRequest(Emailv31.resource).property(Emailv31.MESSAGES, new JSONArray().put(message));

      final MailjetResponse response = mailjetClient.post(request);

      logger.debug(
          "[{}] Response: [{}]-[{}]",
          requestId,
          response.getStatus(),
          response.getRawResponseContent());
    } catch (Exception ex) {
      logger.error("[{}] Send Email...", requestId, ex);
    }
  }

  private static JSONArray emailContactsJSONArray(final List<EmailRequest.EmailContact> emailContacts) {
    final JSONArray jsonArray = new JSONArray();
    for (EmailRequest.EmailContact emailContact : emailContacts) {
      jsonArray.put(emailContactJSONObject(emailContact));
    }
    return jsonArray;
  }

  private static JSONObject emailContactJSONObject(final EmailRequest.EmailContact emailContact) {
    return new JSONObject()
            .put("Email", emailContact.emailAddress())
            .put("Name", emailContact.fullName());
  }

  private static JSONArray emailAttachmentsJSONArray(final List<EmailRequest.EmailAttachment> emailAttachments) {
    final JSONArray jsonArray = new JSONArray();
    for (EmailRequest.EmailAttachment emailAttachment : emailAttachments) {
      jsonArray.put(new JSONObject()
              .put("ContentType", CommonUtilities.isEmpty(emailAttachment.contentType()) ? "text/plain" : emailAttachment.contentType())
              .put("Filename", emailAttachment.fileName())
              .put("Base64Content", emailAttachment.fileContent()));
    }
    return jsonArray;
  }
}

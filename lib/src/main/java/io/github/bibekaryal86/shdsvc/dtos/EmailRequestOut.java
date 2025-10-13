package io.github.bibekaryal86.shdsvc.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;

public class EmailRequestOut implements Serializable {
  private final String subject;
  private final String htmlBody;
  private final String textBody;
  private final String emailFromName;
  private final EmailRequestRecipients emailRequestRecipients;
  private final List<EmailRequestAttachment> emailRequestAttachments;

  @JsonCreator
  public EmailRequestOut(
      @JsonProperty("subject") final String subject,
      @JsonProperty("htmlBody") final String htmlBody,
      @JsonProperty("textBody") final String textBody,
      @JsonProperty("emailFromName") final String emailFromName,
      @JsonProperty("recipients") final EmailRequestRecipients emailRequestRecipients,
      @JsonProperty("attachments") final List<EmailRequestAttachment> emailRequestAttachments) {
    this.subject = subject;
    this.htmlBody = htmlBody;
    this.textBody = textBody;
    this.emailFromName = emailFromName;
    this.emailRequestRecipients = emailRequestRecipients;
    this.emailRequestAttachments = emailRequestAttachments;
  }

  public String getSubject() {
    return subject;
  }

  public String getHtmlBody() {
    return htmlBody;
  }

  public String getTextBody() {
    return textBody;
  }

  public String getEmailFromName() {
    return emailFromName;
  }

  public EmailRequestRecipients getEmailRequestRecipients() {
    return emailRequestRecipients;
  }

  public List<EmailRequestAttachment> getEmailRequestAttachments() {
    return emailRequestAttachments;
  }

  public static class EmailRequestAttachment implements Serializable {
    private final String filename;
    private final String content; // Base64 encoded content
    private final String mimeType;

    @JsonCreator
    public EmailRequestAttachment(
        @JsonProperty("fileName") final String filename,
        @JsonProperty("content") final String content,
        @JsonProperty("mimeType") final String mimeType) {
      this.filename = filename;
      this.content = content;
      this.mimeType = mimeType;
    }

    public String getFilename() {
      return filename;
    }

    public String getContent() {
      return content;
    }

    public String getMimeType() {
      return mimeType;
    }
  }

  public static class EmailRequestRecipients implements Serializable {
    private final List<String> emailTo;
    private final List<String> emailCc;
    private final List<String> emailBcc;

    @JsonCreator
    public EmailRequestRecipients(
        @JsonProperty("to") List<String> emailTo,
        @JsonProperty("cc") List<String> emailCc,
        @JsonProperty("bcc") List<String> emailBcc) {
      this.emailTo = emailTo;
      this.emailCc = emailCc;
      this.emailBcc = emailBcc;
    }

    public List<String> getEmailTo() {
      return emailTo;
    }

    public List<String> getEmailCc() {
      return emailCc;
    }

    public List<String> getEmailBcc() {
      return emailBcc;
    }
  }
}

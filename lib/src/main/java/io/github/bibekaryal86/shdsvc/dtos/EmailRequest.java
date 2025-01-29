package io.github.bibekaryal86.shdsvc.dtos;

import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record EmailRequest(
    EmailContact emailFrom,
    List<EmailContact> emailToList,
    List<EmailContact> emailCcList,
    EmailContent emailContent,
    List<EmailAttachment> emailAttachments)
    implements Serializable {
  public EmailRequest {
    if (Objects.isNull(emailFrom)
        || CommonUtilities.isEmpty(emailToList)
        || Objects.isNull(emailContent)) {
      throw new IllegalArgumentException("Required Attribute Missing...");
    }
  }

  @Override
  public String toString() {
    return "EmailRequest{"
        + "emailFrom=["
        + (Objects.isNull(emailFrom) ? "null" : emailFrom.fullName())
        + "]"
        + ", emailToList=["
        + (CommonUtilities.isEmpty(emailToList)
            ? "empty"
            : emailToList.stream().map(EmailContact::fullName).collect(Collectors.joining("|")))
        + "]"
        + ", emailCcList=["
        + (CommonUtilities.isEmpty(emailCcList)
            ? "empty"
            : emailCcList.stream().map(EmailContact::fullName).collect(Collectors.joining("|")))
        + "]"
        + ", content=["
        + (Objects.isNull(emailContent) ? "null" : emailContent.subject())
        + "]"
        + ", attachment=["
        + (CommonUtilities.isEmpty(emailAttachments)
            ? "empty"
            : emailAttachments.stream()
                .map(EmailAttachment::fileName)
                .collect(Collectors.joining("|")))
        + "]"
        + "}";
  }

  public record EmailContact(String emailAddress, String fullName) implements Serializable {
    public EmailContact {
      if (CommonUtilities.isEmpty(emailAddress) || CommonUtilities.isEmpty(fullName)) {
        throw new IllegalArgumentException("Required Attribute Missing...");
      }
    }
  }

  public record EmailContent(String subject, String text, String html) implements Serializable {
    public EmailContent {
      if (CommonUtilities.isEmpty(subject)
          || (CommonUtilities.isEmpty(text) && CommonUtilities.isEmpty(html))) {
        throw new IllegalArgumentException("Required Attribute Missing...");
      }
    }
  }

  public record EmailAttachment(String fileContent, String fileName, String contentType)
      implements Serializable {
    public EmailAttachment {
      if (CommonUtilities.isEmpty(fileContent) || CommonUtilities.isEmpty(fileName)) {
        throw new IllegalArgumentException("Required Attribute Missing...");
      }
    }
  }
}

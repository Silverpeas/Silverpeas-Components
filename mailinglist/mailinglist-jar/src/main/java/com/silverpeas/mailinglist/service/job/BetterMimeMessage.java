/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.mailinglist.service.job;

import java.io.IOException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author ehugonnet
 */
public class BetterMimeMessage extends MimeMessage {

  public static final String SPAM_FLAG_HEADER = "X-Spam-Flag";
  public static final String SPAM_LEVEL_HEADER = "X-Spam-Score";
  public static final String AUTO_SUBMITTED_HEADER = "Auto-Submitted";
  private final MimeMessage mimeMessage;

  public BetterMimeMessage(MimeMessage message) throws MessagingException {
    super(message);
    this.mimeMessage = message;
  }

  /**
   * Looking for a X-Spam-Flag header in the message.
   * @return true if the flag is set to YES, false otherwise.
   * @throws MessagingException
   */
  public boolean isSpam() throws MessagingException {
    String[] spamFlags = this.mimeMessage.getHeader(SPAM_FLAG_HEADER);
    if (spamFlags != null) {
      for (String flag : spamFlags) {
        if ("YES".equalsIgnoreCase(flag)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returning the content of the X-Spam-Score header.
   * @return the content of the X-Spam-Score header.
   * @throws MessagingException
   */
  public float getSpamLevel() throws MessagingException {
    String[] spamScores = this.mimeMessage.getHeader(SPAM_LEVEL_HEADER);
    if (spamScores != null && spamScores.length > 0) {
      return Float.parseFloat(spamScores[0]);
    }
    return 0f;
  }

  /**
   * Indicates if this email is automatic ou bounced .
   * @return true  if this email is automatic ou bounced - false otherwise.
   * @throws IOException
   * @throws MessagingException
   */
  public boolean isBounced() throws IOException, MessagingException {
    if (isAutomaticMessage()) {
      return true;
    }
    if (mimeMessage.getContent() instanceof Multipart) {
      Multipart parts = (Multipart) this.mimeMessage.getContent();
      for (int i = 0; i < parts.getCount(); i++) {
        String contentType = parts.getBodyPart(i).getContentType();
        if (isNotification(contentType)) {
          return true;
        }
      }
    }
    return isNotification(this.mimeMessage.getContentType());
  }

  protected boolean isAutomaticMessage() throws MessagingException {
    String[] autoSubmittedHeaders = this.mimeMessage.getHeader(AUTO_SUBMITTED_HEADER);
    if (autoSubmittedHeaders != null) {
      for (String autoSubmittedHeader : autoSubmittedHeaders) {
        if ("auto-generated".equalsIgnoreCase(autoSubmittedHeader)
            || "auto-replied".equalsIgnoreCase(autoSubmittedHeader)
            || autoSubmittedHeader.startsWith("auto-notified")) {
          return true;
        }
      }
    }
    return false;
  }

  protected boolean isNotification(String contentType) {
    return isDeliveryStatus(contentType) || isMessageReport(contentType);
  }

  /**
   * Indicates if the corresponding MimeType is a content-type for delivery status notifications
   * (DSNs).  A DSN can be used to notify the sender of a message of any of several conditions:
   * failed delivery, delayed delivery, successful delivery, or the gatewaying of a message into an
   * environment that may not support DSNs.  The "message/delivery-status" content-type defined
   * herein is intended for use within the framework of the "multipart/report" content type defined.
   * Cf. RFC 1894
   * @param contentType the content-type for the mail part.
   * @return true if it is a content-type for delivery status.
   * @see http://www.ietf.org/rfc/rfc1894.txt
   */
  protected boolean isDeliveryStatus(String contentType) {
    return "message/delivery-status".equalsIgnoreCase(contentType);
  }

  /**
   * A multipart/report message content, as defined in RFC 3462. A multipart/report content is a
   * container for mail reports of any kind, and is most often used to return a delivery status
   * report or a disposition notification report.
   * @param contentType the content-type for the mail part.
   * @return true if it is a content-type for delivery status.
   * @see http://www.ietf.org/rfc/rfc3462.txt
   */
  protected boolean isMessageReport(String contentType) {
    return contentType.startsWith("multipart/report");
  }
}

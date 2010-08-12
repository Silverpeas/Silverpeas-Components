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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.ParseException;

import com.silverpeas.mailinglist.service.event.MessageEvent;
import com.silverpeas.mailinglist.service.event.MessageListener;
import com.silverpeas.mailinglist.service.model.beans.Attachment;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.mailinglist.service.util.HtmlCleaner;
import com.silverpeas.util.MimeTypes;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;

public class MailProcessor {

  public static final int SUMMARY_SIZE = 200;

  public static final String MAIL_HEADER_IN_REPLY_TO = "In-Reply-To";

  public static final String MAIL_HEADER_REFERENCES = "References";

  private HtmlCleaner cleaner;

  public void setCleaner(HtmlCleaner cleaner) {
    this.cleaner = cleaner;
    this.cleaner.setSummarySize(SUMMARY_SIZE);
  }

  /**
   * Processes a part for a multi-part email.
   * @param part the part to be processed.
   * @param message the message corresponding to the email.
   * @throws MessagingException
   * @throws IOException
   */
  public void processMailPart(Part part, Message message)
      throws MessagingException, IOException {
    if (!isTextPart(part)) {
      Object content = part.getContent();
      if (content instanceof Multipart) {
        processMultipart((Multipart) content, message);
      } else {
        String fileName = getFileName(part);
        if (fileName != null) {
          Attachment attachment = new Attachment();
          attachment.setSize(part.getSize());
          attachment.setFileName(fileName);
          attachment.setContentType(extractContentType(part.getContentType()));
          String attachmentPath = saveAttachment(part,
              message.getComponentId(), message.getMessageId());
          attachment.setPath(attachmentPath);
          message.getAttachments().add(attachment);
        }
      }
    } else {
      processBody((String) part.getContent(), extractContentType(part
          .getContentType()), message);
    }
  }

  /**
   * Processes the body (text) part of an email.
   * @param content the text content of the email.
   * @param contentType the content type for this text.
   * @param message the message corresponding to this part
   * @throws IOException
   * @throws MessagingException
   */
  public void processBody(String content, String contentType, Message message)
      throws IOException, MessagingException {
    if (message.getContentType() != null
        && message.getContentType().indexOf(MimeTypes.HTML_MIME_TYPE) >= 0) {
      // this is the text-part of an HTMLmultipart message
      return;
    }
    message.setContentType(contentType);
    if (contentType == null) {
      message.setContentType(MimeTypes.PLAIN_TEXT_MIME_TYPE);
    }
    if (message.getContentType().indexOf(MimeTypes.PLAIN_TEXT_MIME_TYPE) >= 0) {
      message.setBody(content);
      if (message.getBody().length() > SUMMARY_SIZE) {
        message.setSummary(message.getBody().substring(0, SUMMARY_SIZE));
      } else {
        message.setSummary(message.getBody());
      }
    } else if (message.getContentType().indexOf(MimeTypes.HTML_MIME_TYPE) >= 0) {
      message.setBody(content);
      Reader reader = null;
      try {
        reader = new StringReader(content);
        cleaner.parse(reader);
        message.setSummary(cleaner.getSummary());
      } finally {
        if (reader != null) {
          reader.close();
        }
      }
    } else { // Managing as text/plain
      message.setContentType(MimeTypes.PLAIN_TEXT_MIME_TYPE);
      message.setBody(content);
      if (message.getBody().length() > SUMMARY_SIZE) {
        message.setSummary(message.getBody().substring(0, SUMMARY_SIZE));
      } else {
        message.setSummary(message.getBody());
      }
    }
  }

  /**
   * Replaces special chars.
   * @param toParse the String whose chars are to be replaced.
   * @return the String without its special chars. Empty String if toParse is null.
   */
  public String replaceSpecialChars(String toParse) {
    if (toParse == null) {
      return "";
    }
    String newLogicalName = toParse.replace(' ', '_');
    newLogicalName = newLogicalName.replace('\'', '_');
    newLogicalName = newLogicalName.replace('-', '_');
    newLogicalName = newLogicalName.replace('#', '_');
    newLogicalName = newLogicalName.replace('%', '_');
    newLogicalName = newLogicalName.replace('>', '_');
    newLogicalName = newLogicalName.replace('<', '_');
    newLogicalName = newLogicalName.replace('\\', '_');
    newLogicalName = newLogicalName.replace('/', '_');
    newLogicalName = newLogicalName.replace('?', '_');
    newLogicalName = newLogicalName.replace(':', '_');
    newLogicalName = newLogicalName.replace('|', '_');
    return newLogicalName;
  }

  /**
   * Saves an attachment as a file, and stores the path in the message.
   * @param part the part corresponding to the attachment.
   * @param componentId the id of the mailing list component.
   * @param messageId the id of the message (email id).
   * @return the absolute path to the file.
   * @throws IOException
   * @throws MessagingException
   */
  public String saveAttachment(Part part, String componentId, String messageId)
      throws IOException, MessagingException {
    String filePath = null;
    File parentDir = new File(FileRepositoryManager
        .getAbsolutePath(componentId)
        + replaceSpecialChars(messageId));
    if (!parentDir.exists()) {
      parentDir.mkdirs();
    }
    FileOutputStream fileOut = null;
    BufferedOutputStream out = null;
    BufferedInputStream in = null;
    InputStream partIn = null;
    try {
      partIn = part.getInputStream();
      in = new BufferedInputStream(partIn);
      File targetFile = new File(parentDir, getFileName(part));
      filePath = targetFile.getAbsolutePath();
      fileOut = new FileOutputStream(targetFile);
      out = new BufferedOutputStream(fileOut);
      byte[] buffer = new byte[8];
      int c = 0;
      while ((c = in.read(buffer)) != -1) {
        out.write(buffer, 0, c);
      }
    } finally {
      if (in != null) {
        in.close();
      }
      if (partIn != null) {
        partIn.close();
      }
      if (out != null) {
        out.close();
      }
      if (fileOut != null) {
        fileOut.close();
      }
    }
    return filePath;
  }

  /**
   * Process an email, extracting attachments and constructing a Message.
   * @param mail the email to be processed.
   * @param mailingList the mailing list it is going to be affected to.
   * @param event the event which will be send at the end of all processing.
   * @throws MessagingException
   * @throws IOException
   */
  public void prepareMessage(MimeMessage mail, MessageListener mailingList,
      MessageEvent event) throws MessagingException, IOException {
    String sender = ((InternetAddress[]) mail.getFrom())[0].getAddress();
    if (!mailingList.checkSender(sender)) {
      return;
    }
    Message message = new Message();
    message.setComponentId(mailingList.getComponentId());
    message.setSender(sender);
    message.setSentDate(mail.getSentDate());
    message.setMessageId(mail.getMessageID());
    String[] referenceId = mail.getHeader(MAIL_HEADER_IN_REPLY_TO);
    if (referenceId == null || referenceId.length == 0) {
      referenceId = mail.getHeader(MAIL_HEADER_REFERENCES);
    }
    if (referenceId == null || referenceId.length == 0) {
      message.setReferenceId(null);
    } else {
      message.setReferenceId(referenceId[0]);
    }
    message.setTitle(mail.getSubject());
    SilverTrace.info("mailingList", "MailProcessor.prepareMessage()",
        "mailinglist.notification.error",
        "Processing message " + mail.getSubject());
    Object content = mail.getContent();
    if (content instanceof Multipart) {
      processMultipart((Multipart) content, message);
    } else if (content instanceof String) {
      processBody((String) content, mail.getContentType(), message);
    }
    event.addMessage(message);
  }

  protected static String extractContentType(String contentType) {
    try {
      ContentType type = new ContentType(contentType);
      return type.getBaseType();
    } catch (ParseException e) {
      SilverTrace.error("mailingList", "MailProcessor.extractContentType()",
          "mailinglist.notification.error", e);
    }
    return contentType;
  }

  protected String getFileName(Part part) throws MessagingException {
    String fileName = part.getFileName();
    if (fileName == null) {
      try {
        ContentType type = new ContentType(part.getContentType());
        fileName = type.getParameter("name");
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
    return fileName;
  }

  /**
   * Analyze the part to check if it is an attachment, a base64 encoded file or some text.
   * @param part the part to be analyzed.
   * @return true if it is some text - false otherwise.
   * @throws MessagingException
   */
  protected boolean isTextPart(Part part) throws MessagingException {
    String disposition = part.getDisposition();
    if (!Part.ATTACHMENT.equals(disposition)
        && !Part.INLINE.equals(disposition)) {
      try {
        ContentType type = new ContentType(part.getContentType());
        return "text".equalsIgnoreCase(type.getPrimaryType());
      } catch (ParseException e) {
        e.printStackTrace();
      }
    } else if (Part.INLINE.equals(disposition)) {
      try {
        ContentType type = new ContentType(part.getContentType());
        return "text".equalsIgnoreCase(type.getPrimaryType())
            && getFileName(part) == null;
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  public void processMultipart(Multipart multipart, Message message)
      throws MessagingException, IOException {
    int partsNumber = multipart.getCount();
    for (int i = 0; i < partsNumber; i++) {
      Part part = multipart.getBodyPart(i);
      processMailPart(part, message);
    }
  }
}

/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.external.mailinglist.servlets;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.Attachment;
import com.silverpeas.mailinglist.service.model.beans.Message;

public class AttachmentServlet extends HttpServlet implements MailingListRoutage {

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    service(req, resp);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    service(req, resp);
  }

  public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    RestRequest rest = new RestRequest(request);
    String messageId = (String) rest.getElements().get(DESTINATION_MESSAGE);
    String attachmentId = (String) rest.getElements().get(DESTINATION_ATTACHMENT);
    if (messageId == null || attachmentId == null) {
      return;
    }
    Message message = ServicesFactory.getMessageService().getMessage(messageId);
    Iterator<Attachment> iter = message.getAttachments().iterator();
    Attachment file = null;
    boolean found = false;
    while (iter.hasNext() && !found) {
      Attachment attachment = iter.next();
      file = attachment;
      found = attachmentId.equals(attachment.getId());
    }
    if (found) {
      response.setContentType(file.getContentType());
      response.setHeader("Content-Disposition", "inline; filename=" + file.getFileName());
      int length = (new Long(file.getSize())).intValue();
      response.setContentLength(length);
      OutputStream out = response.getOutputStream();
      InputStream in = null;
      try {
        in = new FileInputStream(file.getPath());
        byte[] buffer = new byte[8];
        int c = 0;
        while ((c = in.read(buffer)) != -1) {
          out.write(buffer, 0, c);
        }
      } catch (IOException ioex) {
        throw ioex;
      } finally {
        out.close();
        if (in != null) {
          in.close();
        }
      }
    }
  }
}

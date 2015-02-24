/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.external.mailinglist.servlets;

import com.silverpeas.mailinglist.service.MailingListServicesProvider;
import com.silverpeas.mailinglist.service.model.beans.Attachment;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public class MessageProcessor implements MailingListRoutage {

  public static String processMessage(RestRequest rest, HttpServletRequest request) {
    String id = rest.getElements().get(DESTINATION_MESSAGE);
    switch (rest.getAction()) {
      case RestRequest.DELETE:
        if ((Boolean) request.getAttribute(IS_USER_ADMIN_ATT) ||
            (Boolean) request.getAttribute(IS_USER_MODERATOR_ATT)) {
          String[] ids = request.getParameterValues(SELECTED_MESSAGE_PARAM);
          if (ids != null && ids.length > 0) {
            for (final String id1 : ids) {
              deleteMessage(id1);
            }
          } else {
            if (id != null && !DELETE_ACTION.equalsIgnoreCase(id) &&
                !UPDATE_ACTION.equalsIgnoreCase(id)) {
              deleteMessage(id);
            }
          }
        }
        if (MODERATION_VALUE.equalsIgnoreCase(rest.getElements().get(DESTINATION_ELEMENT))) {
          return buildRedirectUrl(request,
              rest.getComponentId() + '/' + DESTINATION_MODERATION + '/' + rest.getComponentId());
        }
        return buildRedirectUrl(request,
            rest.getComponentId() + '/' + DESTINATION_LIST + '/' + rest.getComponentId());
      case RestRequest.UPDATE:
        boolean isAuthorized = ((Boolean) request.getAttribute(IS_USER_ADMIN_ATT) ||
            (Boolean) request.getAttribute(IS_USER_MODERATOR_ATT));
        if (!isAuthorized) {
          return buildRedirectUrl(request,
              rest.getComponentId() + '/' + DESTINATION_LIST + '/' + rest.getComponentId());
        }
        String[] ids = request.getParameterValues(SELECTED_MESSAGE_PARAM);
        if (ids != null && ids.length > 0) {
          for (final String id1 : ids) {
            moderateMessage(id1);
          }
          if (ids.length != 1) {
            return buildRedirectUrl(request,
                rest.getComponentId() + '/' + DESTINATION_MODERATION + '/' + rest.getComponentId());
          }
          id = ids[0];
        } else {
          if (id != null && !DELETE_ACTION.equalsIgnoreCase(id) &&
              !UPDATE_ACTION.equalsIgnoreCase(id)) {
            moderateMessage(id);
          } else {
            return buildRedirectUrl(request,
                rest.getComponentId() + '/' + DESTINATION_LIST + '/' + rest.getComponentId());
          }
        }
        rest.getElements().put(DESTINATION_ELEMENT, DESTINATION_MODERATION);
      case RestRequest.FIND:
        Message message = findMessage(id);
        request.setAttribute(MESSAGE_ATT, message);
        if (message != null && message.getAttachments() != null &&
            !message.getAttachments().isEmpty()) {
          List<DisplayableAttachment> attachments =
              new ArrayList<>(message.getAttachments().size());
          for (Attachment currentAttachment : message.getAttachments()) {
            DisplayableAttachment attachment =
                new DisplayableAttachment(message.getId(), currentAttachment);
            attachments.add(attachment);
          }
          request.setAttribute(MESSAGE_ATTACHMENTS_ATT, attachments);
        }
        request.setAttribute(PREVIOUS_PATH_ATT, rest.getElements().get(DESTINATION_ELEMENT));
        return JSP_BASE + DESTINATION_DISPLAY_MESSAGE;
      default:
        return buildRedirectUrl(request,
            rest.getComponentId() + '/' + DESTINATION_LIST + '/' + rest.getComponentId());
    }
  }

  protected static String buildRedirectUrl(HttpServletRequest request, String destination) {
    return request.getScheme() + "://" + request.getServerName() + ':' + request.getServerPort() +
        request.getContextPath() + request.getServletPath() + '/' + destination;
  }

  protected static Message findMessage(String id) {
    return MailingListServicesProvider.getMessageService().getMessage(id);
  }

  protected static void deleteMessage(String id) {
    MailingListServicesProvider.getMessageService().deleteMessage(id);
  }

  protected static void moderateMessage(String id) {
    MailingListServicesProvider.getMessageService().moderateMessage(id);
    Message message = MailingListServicesProvider.getMessageService().getMessage(id);
    MailingList list = MailingListServicesProvider.getMailingListService()
        .findMailingList(message.getComponentId());
    try {
      MailingListServicesProvider.getNotificationHelper().notify(message, list);
    } catch (Exception e) {
      SilverTrace
          .error("mailingList", "MailSender.sendMail", "mailinglist.external.notification.send", e);
    }
  }
}

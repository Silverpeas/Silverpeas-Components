/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.mailinglist.servlets;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.silverpeas.components.mailinglist.service.model.MailingListService;
import org.silverpeas.components.mailinglist.service.model.MessageService;
import org.silverpeas.components.mailinglist.service.model.beans.Attachment;
import org.silverpeas.components.mailinglist.service.model.beans.MailingList;
import org.silverpeas.components.mailinglist.service.model.beans.Message;
import org.silverpeas.components.mailinglist.service.notification.NotificationHelper;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.kernel.logging.SilverLogger;

import java.util.ArrayList;
import java.util.List;

@Bean
public class MessageProcessor implements MailingListRoutage {

  @Inject
  private MailingListService mailingListService;

  @Inject
  private MessageService messageService;

  @Inject
  private NotificationHelper notificationHelper;

  public String processMessage(RestRequest rest, HttpServletRequest request) {
    String id = rest.getElements().get(DESTINATION_MESSAGE);
    switch (rest.getAction()) {
      case RestRequest.DELETE:
        return performDeletion(rest, request, id);
      case RestRequest.UPDATE:
        return performUpdate(rest, request, id);
      case RestRequest.FIND:
        setMessage(id, request);
        request.setAttribute(PREVIOUS_PATH_ATT, rest.getElements().get(DESTINATION_ELEMENT));
        return JSP_BASE + DESTINATION_DISPLAY_MESSAGE;
      default:
        return buildRedirectUrl(request,
            rest.getComponentId() + '/' + DESTINATION_LIST + '/' + rest.getComponentId());
    }
  }

  private @NonNull String performUpdate(RestRequest rest, HttpServletRequest request, String id) {
    boolean isNotAboutThisMessage = id != null && !DELETE_ACTION.equalsIgnoreCase(id) &&
        !UPDATE_ACTION.equalsIgnoreCase(id);
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
      if (isNotAboutThisMessage) {
        moderateMessage(id);
      } else {
        return buildRedirectUrl(request,
            rest.getComponentId() + '/' + DESTINATION_LIST + '/' + rest.getComponentId());
      }
    }
    rest.getElements().put(DESTINATION_ELEMENT, DESTINATION_MODERATION);
    setMessage(id, request);
    request.setAttribute(PREVIOUS_PATH_ATT, rest.getElements().get(DESTINATION_ELEMENT));
    return JSP_BASE + DESTINATION_DISPLAY_MESSAGE;
  }

  private @NonNull String performDeletion(RestRequest rest, HttpServletRequest request, String id) {
    boolean isNotAboutThisMessage = id != null && !DELETE_ACTION.equalsIgnoreCase(id) &&
        !UPDATE_ACTION.equalsIgnoreCase(id);
    if ((Boolean) request.getAttribute(IS_USER_ADMIN_ATT) ||
        (Boolean) request.getAttribute(IS_USER_MODERATOR_ATT)) {
      String[] ids = request.getParameterValues(SELECTED_MESSAGE_PARAM);
      if (ids != null && ids.length > 0) {
        for (final String id1 : ids) {
          deleteMessage(id1);
        }
      } else {
        if (isNotAboutThisMessage) {
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
  }

  protected static String buildRedirectUrl(HttpServletRequest request, String destination) {
    return request.getScheme() + "://" + request.getServerName() + ':' + request.getServerPort() +
        request.getContextPath() + request.getServletPath() + '/' + destination;
  }

  protected Message findMessage(String id) {
    return messageService.getMessage(id);
  }

  protected void deleteMessage(String id) {
    messageService.deleteMessage(id);
  }

  protected void moderateMessage(String id) {
    messageService.moderateMessage(id);
    Message message = messageService.getMessage(id);
    MailingList list = mailingListService.findMailingList(message.getComponentId());
    try {
      notificationHelper.notify(message, list);
    } catch (Exception e) {
      SilverLogger.getLogger(MessageProcessor.class).error(e);
    }
  }

  private void setMessage(String id, HttpServletRequest request) {
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
  }
}

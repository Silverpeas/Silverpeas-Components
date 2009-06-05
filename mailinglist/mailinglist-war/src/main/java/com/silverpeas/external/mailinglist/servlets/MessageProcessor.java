package com.silverpeas.external.mailinglist.servlets;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.Attachment;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class MessageProcessor implements MailingListRoutage {

  public static String processMessage(RestRequest rest,
      HttpServletRequest request) {
    String id = (String) rest.getElements().get(DESTINATION_MESSAGE);
    switch (rest.getAction()) {
      case RestRequest.DELETE:
        if (((Boolean) request.getAttribute(IS_USER_ADMIN_ATT)).booleanValue()
            || ((Boolean) request.getAttribute(IS_USER_MODERATOR_ATT))
                .booleanValue()) {
          String[] ids = request.getParameterValues(SELECTED_MESSAGE_PARAM);
          if (ids != null && ids.length > 0) {
            for (int i = 0; i < ids.length; i++) {
              deleteMessage(ids[i]);
            }
          } else {
            if (id != null && !DELETE_ACTION.equalsIgnoreCase(id)
                && !UPDATE_ACTION.equalsIgnoreCase(id)) {
              deleteMessage(id);
            }
          }
        }
        if (MODERATION_VALUE.equalsIgnoreCase((String) rest.getElements().get(
            DESTINATION_ELEMENT))) {
          return buildRedirectUrl(request, rest.getComponentId() + '/'
              + DESTINATION_MODERATION + '/' + rest.getComponentId());
        }
        return buildRedirectUrl(request, rest.getComponentId() + '/'
            + DESTINATION_LIST + '/' + rest.getComponentId());
      case RestRequest.UPDATE:
        boolean isAuthorized = (((Boolean) request
            .getAttribute(IS_USER_ADMIN_ATT)).booleanValue() || ((Boolean) request
            .getAttribute(IS_USER_MODERATOR_ATT)).booleanValue());
        if (!isAuthorized) {
          return buildRedirectUrl(request, rest.getComponentId() + '/'
              + DESTINATION_LIST + '/' + rest.getComponentId());
        }
        String[] ids = request.getParameterValues(SELECTED_MESSAGE_PARAM);
        if (ids != null && ids.length > 0) {
          for (int i = 0; i < ids.length; i++) {
            moderateMessage(ids[i]);
          }
          if (ids.length != 1) {
            return buildRedirectUrl(request, rest.getComponentId() + '/'
                + DESTINATION_MODERATION + '/' + rest.getComponentId());
          }
          id = ids[0];
        } else {
          if (id != null && !DELETE_ACTION.equalsIgnoreCase(id)
              && !UPDATE_ACTION.equalsIgnoreCase(id)) {
            moderateMessage(id);
          } else {
            return buildRedirectUrl(request, rest.getComponentId() + '/'
                + DESTINATION_LIST + '/' + rest.getComponentId());
          }
        }
        rest.getElements().put(DESTINATION_ELEMENT, DESTINATION_MODERATION);
      case RestRequest.FIND:
        Message message = findMessage(id);
        request.setAttribute(MESSAGE_ATT, message);
        if (message.getAttachments() != null
            && !message.getAttachments().isEmpty()) {
          List<DisplayableAttachment> attachments = new ArrayList<DisplayableAttachment>(
              message.getAttachments().size());
          for (Attachment currentAttachment : message.getAttachments()) {
            DisplayableAttachment attachment = new DisplayableAttachment(
                message.getId(), currentAttachment);
            attachments.add(attachment);
          }
          request.setAttribute(MESSAGE_ATTACHMENTS_ATT, attachments);
        }
        request.setAttribute(PREVIOUS_PATH_ATT, rest.getElements().get(
            DESTINATION_ELEMENT));
        return JSP_BASE + DESTINATION_DISPLAY_MESSAGE;
      default:
        return buildRedirectUrl(request, rest.getComponentId() + '/'
            + DESTINATION_LIST + '/' + rest.getComponentId());
    }
  }

  protected static String buildRedirectUrl(HttpServletRequest request,
      String destination) {
    return request.getScheme() + "://" + request.getServerName() + ':'
        + request.getServerPort() + request.getContextPath()
        + request.getServletPath() + '/' + destination;
  }

  protected static Message findMessage(String id) {
    return ServicesFactory.getMessageService().getMessage(id);
  }

  protected static void deleteMessage(String id) {
    ServicesFactory.getMessageService().deleteMessage(id);
  }

  protected static void moderateMessage(String id) {
    ServicesFactory.getMessageService().moderateMessage(id);
    Message message = ServicesFactory.getMessageService().getMessage(id);
    MailingList list = ServicesFactory.getMailingListService().findMailingList(
        message.getComponentId());
    try {
      ServicesFactory.getNotificationHelper().notify(message, list);
    } catch (Exception e) {
      e.printStackTrace();
      SilverTrace.error("mailingList", "MailSender.sendMail",
          "mailinglist.external.notification.send", e);
    }
  }
}

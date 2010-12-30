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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.forums.sessionController.helpers;

import com.silverpeas.notation.model.NotationDetail;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.web.RequestHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.forums.models.Message;
import com.stratelia.webactiv.forums.sessionController.ForumsSessionController;
import com.stratelia.webactiv.forums.url.ActionUrl;
import com.stratelia.webactiv.util.ResourceLocator;
import java.io.IOException;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

/**
 *
 * @author ehugonnet
 */
public class ForumHelper {

  public static final String IMAGE_UPDATE = "../../util/icons/update.gif";
  public static final String IMAGE_UNLOCK = "../../util/icons/lock.gif";
  public static final String IMAGE_LOCK = "../../util/icons/unlock.gif";
  public static final String IMAGE_DELETE = "../../util/icons/delete.gif";
  public static final String IMAGE_MOVE = "../../util/icons/moveMessage.gif";
  public static final String IMAGE_ADD_FORUM = "../../util/icons/forums_to_add.gif";
  public static final String IMAGE_ADD_CATEGORY = "../../util/icons/folderAddBig.gif";
  public static final String IMAGE_WORD = "icons/word.gif";
  public static final String IMAGE_NOTATION_OFF = "../../util/icons/starEmpty.gif";
  public static final String IMAGE_NOTATION_ON = "../../util/icons/starFilled.gif";
  public static final String IMAGE_NOTATION_EMPTY = "../../util/icons/shim.gif";
  public static final String STATUS_VALIDATE = "V";
  public static final String STATUS_FOR_VALIDATION = "A";
  public static final String STATUS_REFUSED = "R";

  public static int getIntParameter(HttpServletRequest request, String name) {    
    return getIntParameter(request, name, -1);
  }

  public static int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
    return RequestHelper.getIntParameter(request, name, defaultValue);
  }

  public static String convertDate(Date date, ResourcesWrapper resources) {
    return resources.getOutputDateAndHour(date);
  }

  public static void addBodyOnload(JspWriter out, ForumsSessionController fsc) {
    addBodyOnload(out, fsc, "");
  }

  public static void addBodyOnload(JspWriter out, ForumsSessionController fsc, String call) {
    try {
      String methodName = call;
      if (call == null) {
        methodName = "";
      }
      out.print("onload=\"");
      out.print(methodName);
      if (fsc.isResizeFrame()) {
        if (methodName.length() > 0 && !methodName.endsWith(";")) {
          out.print(";");
        }
        out.print("resizeFrame();");
      }
      out.print("\"");
    } catch (IOException ioe) {
      SilverTrace.info("forums", "JSPmessagesListManager.addBodyOnload()",
          "root.EX_NO_MESSAGE", null, ioe);
    }
  }

  public static void addJsResizeFrameCall(JspWriter out, ForumsSessionController fsc) {
    try {
      if (fsc.isResizeFrame()) {
        out.print("resizeFrame();");
      }
    } catch (IOException ioe) {
      SilverTrace.info("forums", "JSPmessagesListManager.addJsResizeFrameCall()",
          "root.EX_NO_MESSAGE", null, ioe);
    }
  }

  public static void displayMessageLine(Message message, JspWriter out, ResourceLocator resource,
      String userId, boolean admin, boolean moderator, boolean reader, boolean view, int depth,
      boolean hasChildren, boolean deployed, boolean forumActive, boolean simpleMode, String call,
      ForumsSessionController fsc, ResourcesWrapper resources) {
    try {
      int messageId = message.getId();
      String messageTitle = message.getTitle();
      String author = message.getAuthor();
      String messageAuthor = fsc.getAuthorName(message.getAuthor());
      int messageParent = message.getParentId();
      if (messageAuthor == null) {
        messageAuthor = resource.getString("inconnu");
      }
      int forumId = message.getForumId();
      boolean isSubscriber = fsc.isSubscriber(messageId, userId);
      int cellsCount = 0;
      String cellWidth = (simpleMode ? " width=\"15\"" : "");
      int lineHeight = ((fsc.isExternal() && reader) ? 16 : 24);
      // isAutorized : si l'utilisateur est autorisé à modifier le message
      boolean isAutorized = admin || moderator || userId.equals(author);




      if (STATUS_VALIDATE.equals(message.getStatus()) || (!STATUS_VALIDATE.equals(
          message.getStatus()) && isAutorized)) {

        out.println("  <tr id=\"msgLine" + messageId + "\" height=\"" + lineHeight + "\">");

        // abonnement
        out.print("    <td" + cellWidth + ">");
        if (isSubscriber) {
          out.print("<a href=\"");
          out.print(ActionUrl.getUrl(
              (view ? "viewForum" : "viewMessage"), call, 13, messageId, forumId));
          out.print("\"><img src=\"icons/abonn_message.gif\" border=\"0\" alt=\""
              + resource.getString("unsubscribeMessage") + "\" title=\""
              + resource.getString("unsubscribeMessage") + "\"></a>");
        } else {
          out.print("&nbsp;");
        }
        out.println("</td>");
        cellsCount++;

        out.print("    <td" + cellWidth + ">");

        // rechercher si l'utilisateur a des messages non lus sur ce sujet
        if (messageParent == 0 && (!fsc.isExternal() || !reader)) {
          boolean isNewMessage = fsc.isNewMessage(userId, forumId, messageId);
          out.print(
              "<img src=\"icons/" + (isNewMessage ? "buletRed" : "buletColoredGreen") + ".gif\">");
        }
        out.println("</td>");

        cellsCount++;
        // Titre du message
        out.print("    <td class=\"txtnote\">");
        out.print("<table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">");
        out.print("<tr>");
        out.print("<td width=\"" + depth * 10 + "\">");
        if (depth > 0) {
          out.print("<img src=\"icons/1px.gif\" width=\"" + depth * 10 + "\" height=\"1\">");
        }
        out.print("</td><td align=\"left\"><a href=\"");
        if (fsc.isDisplayAllMessages() && !view) {
          out.print("javascript:scrollMessage('" + messageId + "')");
        } else {
          out.print(ActionUrl.getUrl(
              "viewMessage", call, 1, messageId, forumId, !simpleMode, false));
        }
        out.print("\">");
        out.print("<span class=\"message_" + message.getStatus() + "\"><b>");
        out.print(EncodeHelper.javaStringToHtmlString(messageTitle));
        // Auteur du message
        out.print("</b>");
        out.print(simpleMode ? "&nbsp;" : "<br>");
        out.print("(" + messageAuthor);
        // Date de Creation
        out.print("&nbsp;-&nbsp;" + convertDate(message.getDate(), resources));
        out.print(")");
        if (message.getStatus().equals(STATUS_FOR_VALIDATION)) {
          out.println(" - " + resource.getString("toValidate"));
        } else if (message.getStatus().equals(STATUS_REFUSED)) {
          out.println(" - " + resource.getString("refused"));
        }
        out.println("</span>");
        cellsCount++;

        out.print("</a>");
        out.print("</td>");
        out.print("</tr>");
        out.print("</table>");
        out.println("</td>");
      }




      if (!simpleMode) {
        // Dernier post
        out.print("    <td align=\"center\"><span class=\"txtnote\">");
        int lastMessageId = -1;
        Date dateLastMessage = null;
        String lastMessageDate = "";
        String lastMessageUser = "";
        Object[] lastMessage = fsc.getLastMessage(forumId, messageId);
        if (lastMessage != null) {
          lastMessageId = Integer.parseInt((String) lastMessage[0]);
          lastMessageDate = convertDate((Date) lastMessage[1], resources);
          lastMessageUser = (String) lastMessage[2];
        }
        if (lastMessageDate != null) {
          out.print("<a href=\"" + ActionUrl.getUrl(
              "viewMessage", call, 1, lastMessageId, forumId, true, false) + "\">");
          out.print(EncodeHelper.javaStringToHtmlString(lastMessageDate));
          out.print("<br/>");
          out.print(EncodeHelper.javaStringToHtmlString(lastMessageUser));
          out.print("</a>");
        }
        out.println("</span></td>");
        cellsCount++;

        // Nombres de réponses
        out.print("    <td align=\"center\"><span class=\"txtnote\">");
        out.print(fsc.getNbResponses(forumId, messageId));
        out.println("</span></td>");
        cellsCount++;

        // Nombres de vues
        out.print("    <td align=\"center\"><span class=\"txtnote\">");
        out.print(fsc.getMessageStat(messageId));
        out.println("</span></td>");
        cellsCount++;

        // Notation
        NotationDetail notation = fsc.getMessageNotation(messageId);
        int globalNote = notation.getRoundGlobalNote();
        int userNote = notation.getUserNote();
        String cellLabel = notation.getNotesCount() + " " + resources.getString("forums.note");
        if (userNote > 0) {
          cellLabel += " - " + resources.getString("forums.yourNote") + " : " + userNote;
        }
        out.print("<td align=\"center\"  title=\"" + cellLabel + "\"><span class=\"txtnote\">");
        for (int i = 1; i <= 5; i++) {
          out.print("<img class=\"notation_" + (i <= globalNote ? "on" : "off")
              + "\" src=\"" + IMAGE_NOTATION_EMPTY + "\"/>");
        }
        out.println("</span></td>");
      }

      // Opérations
      if (isAutorized) {
        int opCellWidth = 40;
        if (depth == 0) {
          opCellWidth += 20;
        }
        if (!simpleMode) {
          opCellWidth += 20;
        }
        out.print("    <td align=\"center\" width=\"" + opCellWidth + "\">");
        if (messageParent == 0 && (admin || moderator)) {
          out.print("<a href=\"");
          out.print(ActionUrl.getUrl("editMessage", call, 3, messageId, forumId));
          out.print("\"><img src=" + IMAGE_MOVE + " align=\"middle\" border=\"0\" alt=\""
              + resource.getString("moveMessage") + "\" title=\""
              + resource.getString("moveMessage") + "\"></a>");
          out.print("&nbsp;");
        }

        if (!view) {
          out.print("<a href=\"javascript:editMessage(" + messageId + ")\">");
          out.print("<img src=" + IMAGE_UPDATE + " align=\"middle\" border=\"0\" alt=\""
              + resource.getString("editMessage") + "\" title=\""
              + resource.getString("editMessage") + "\"></a>");
          out.print("&nbsp;");
        }
        out.print(
            "<a href=\"javascript:deleteMessage(" + messageId + ", " + messageParent + ", false)\">");
        out.print("<img src=" + IMAGE_DELETE + " align=\"middle\" border=\"0\" alt=\""
            + resource.getString("deleteMessage") + "\" title=\""
            + resource.getString("deleteMessage") + "\"></a>");


        if (!simpleMode) {
          out.print("&nbsp;");
          out.print("<a href=\"");
          out.print(ActionUrl.getUrl("editMessageKeywords", call, -1, messageId, forumId));
          out.print("\"><img src=" + IMAGE_WORD + " align=\"middle\" border=\"0\" alt=\""
              + resource.getString("editMessageKeywords") + "\" title=\""
              + resource.getString("editMessageKeywords") + "\"></a>");
        }

        out.println("</td>");
        cellsCount++;
      }

      out.println("  </tr>");
    } catch (IOException ioe) {
      SilverTrace.info("forums", "JSPmessagesListManager.displayMessageLine()", "root.EX_NO_MESSAGE",
          null, ioe);
    }
  }

  public static void displayMessagesList(JspWriter out, ResourceLocator resource, String userId,
      boolean admin, boolean moderator, boolean reader, boolean view, int currentForumId,
      boolean simpleMode, String call, ForumsSessionController fsc, ResourcesWrapper resources) {
    try {
      Message[] messages = fsc.getMessagesList(currentForumId);
      if (messages.length > 0) {
        scanMessage(messages, out, resource, userId, currentForumId, admin, moderator, reader,
            view, 0, 0, 0, simpleMode, call, fsc, resources);
      } else {
        int colspan = 6;
        if (admin || moderator) {
          colspan++;
        }
        out.println("<tr><td colspan=\"" + colspan + "\" align=center><span class=\"txtnote\">"
            + resource.getString("noMessages") + "</span></td></tr>");
      }
    } catch (IOException ioe) {
      SilverTrace.info("forums", "JSPmessagesListManager.displayMessagesList()",
          "root.EX_NO_MESSAGE", null, ioe);
    }
  }

  public static void displaySingleMessageList(JspWriter out, ResourceLocator resource, String userId,
      boolean admin, boolean moderator, boolean reader, boolean view, int currentForumId,
      int messageId, boolean simpleMode, String call, ForumsSessionController fsc,
      ResourcesWrapper resources) {
    try {
      int parent = fsc.getMessageParentId(messageId);
      while (parent > 0) {
        messageId = parent;
        parent = fsc.getMessageParentId(messageId);
      }

      Message[] messages = fsc.getMessagesList(currentForumId);
      int messagesCount = messages.length;
      if (messagesCount > 0) {
        out.println(
            "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" class=\"contourintfdcolor\">");
        displayOneMessage(messages, out, resource, userId, currentForumId, admin, moderator,
            reader, view, messageId, 0, simpleMode, call, fsc, resources);
        out.println("</table>");

        if (messagesCount > 1) {
          out.println("<div class=\"contourintfdcolor\" id=\"msgDiv\">");
          out.println(
              "<table id=\"msgTable\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\">");
          scanMessage(messages, out, resource, userId, currentForumId, admin, moderator,
              reader, view, messageId, 1, -1, simpleMode, call, fsc, resources);
          out.println("</table>");
          out.println("</div>");
        }
      } else {
        out.println(
            "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"contourintfdcolor\">");
        out.println("<tr><td align=\"center\"><span class=\"txtnav\">"
            + resource.getString("noMessages") + "</span></td></tr>");
        out.println("</table>");
      }
    } catch (IOException ioe) {
      SilverTrace.info("forums", "JSPmessagesListManager.displaySingleMessageList()",
          "root.EX_NO_MESSAGE", null, ioe);
    } catch (Exception e) {
      try {
        out.println("ERROR");
      } catch (IOException ioe2) {
        SilverTrace.info("forums", "JSPmessagesListManager.displaySingleMessageList()",
            "root.EX_NO_MESSAGE", null, ioe2);
      }
    }
  }

  public static void scanMessage(Message[] messages, JspWriter out, ResourceLocator resource, String userId,
      int currentPage, boolean admin, boolean moderator, boolean reader, boolean view,
      int currentMessageId, int depth, int maxDepth, boolean simpleMode, String call,
      ForumsSessionController fsc, ResourcesWrapper resources) {
    for (int i = 0; i < messages.length; i++) {
      Message message = messages[i];
      int parentId = message.getParentId();
      if (parentId == currentMessageId) {
        int messageId = message.getId();
        boolean hasChildren = hasMessagesChildren(messages, messageId);
        //boolean isDeployed = fsc.messageIsDeployed(messageId);
        boolean isDeployed = true;
        displayMessageLine(message, out, resource, userId, admin, moderator, reader, view,
            depth, hasChildren, isDeployed, fsc.isForumActive(currentPage), simpleMode, call, fsc,
            resources);
        if (hasChildren && isDeployed && (maxDepth == -1 || depth < maxDepth)) {
          scanMessage(messages, out, resource, userId, currentPage, admin, moderator, reader,
              view, messageId, (depth + 1), maxDepth, simpleMode, call, fsc, resources);
        }
      }
    }
  }

  public static void deployAll(Message[] messages, ForumsSessionController fsc) {
    for (int i = 0; i < messages.length; i++) {
      fsc.deployMessage(messages[i].getId());
    }
  }

  public static void displayOneMessage(Message[] messages, JspWriter out, ResourceLocator resource,
      String userId, int currentPage, boolean admin, boolean moderator, boolean reader, boolean view,
      int currentMessageId, int depth, boolean simpleMode, String call, ForumsSessionController fsc,
      ResourcesWrapper resources) {
    int i = 0;
    boolean loop = true;
    while ((i < messages.length) && loop) {
      Message message = messages[i];
      int messageId = message.getId();
      if (messageId == currentMessageId) {
        boolean hasChildren = hasMessagesChildren(messages, messageId);
        boolean isDeployed = fsc.messageIsDeployed(messageId);
        displayMessageLine(message, out, resource, userId, admin, moderator, reader, view,
            depth, hasChildren, isDeployed, fsc.isForumActive(currentPage), simpleMode, call, fsc,
            resources);
        loop = false;
      }
      i++;
    }
  }

  public static boolean hasMessagesChildren(Message[] messages, int messageId) {
    int i = 0;
    while (i < messages.length) {
      if (messages[i].getParentId() == messageId) {
        return true;
      }
      i++;
    }
    return false;
  }

  public static void displayMessagesAdminButtons(boolean moderator, int forumActive, JspWriter out,
      int currentFolderId, String call, ResourceLocator resource) {
    try {
      if (forumActive == 1) {
        out.print("<a href=\""
            + ActionUrl.getUrl("editMessage", call, 1, currentFolderId, currentFolderId)
            + "\"><img src=\"icons/fo_newmessage.gif\" width=\"25\" height=\"26\" "
            + "border=\"0\" alt=\"" + resource.getString("newMessage") + "\" title=\""
            + resource.getString("newMessage") + "\"></a>");
      }
    } catch (IOException ioe) {
      SilverTrace.info("forums", "JSPmessagesListManager.displayMessagesAdminButtons()",
          "root.EX_NO_MESSAGE", null, ioe);
    }
  }

  public static int[] displayMessageNotation(JspWriter out, ResourcesWrapper resources, int messageId,
      ForumsSessionController fsc, boolean reader) {
    try {
      NotationDetail notation = fsc.getMessageNotation(messageId);
      int globalNote = notation.getRoundGlobalNote();
      int userNote = notation.getUserNote();
      out.print("<span class=\"txtnote\">" + resources.getString("forums.messageNote") + " : ");
      for (int i = 1; i <= 5; i++) {
        out.print("<img");
        if (!reader) {
          out.print(" id=\"notationImg" + i + "\"");
        }
        out.print(" style=\"margin-bottom: 0px\" class=\"notation_" + (i <= globalNote ? "on" : "off")
            + "\" src=\"" + IMAGE_NOTATION_EMPTY + "\"/>");
      }
      out.print(" (" + notation.getNotesCount() + " " + resources.getString("forums.note"));
      if (userNote > 0) {
        out.print(" - " + resources.getString("forums.yourNote") + " : " + userNote);
      }
      out.println(")</span>");
      return new int[]{globalNote, userNote};
    } catch (IOException ioe) {
      SilverTrace.info("forums", "JSPforumsListManager.displayForumNotation()",
          "root.EX_NO_MESSAGE", null, ioe);
      return new int[0];
    }
  }
}

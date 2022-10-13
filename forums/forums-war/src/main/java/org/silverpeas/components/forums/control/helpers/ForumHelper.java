/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.forums.control.helpers;

import org.silverpeas.components.forums.control.ForumsSessionController;
import org.silverpeas.components.forums.model.Message;
import org.silverpeas.components.forums.url.ActionUrl;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.webapi.rating.RaterRatingEntity;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.MultiSilverpeasBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.Date;

/**
 * @author ehugonnet
 */
public class ForumHelper {

  public static final String IMAGE_UPDATE = "../../util/icons/update.gif";
  public static final String IMAGE_UNLOCK = "../../util/icons/lock.gif";
  public static final String IMAGE_LOCK = "../../util/icons/unlock.gif";
  public static final String IMAGE_DELETE = "../../util/icons/delete.gif";
  public static final String IMAGE_MOVE = "../../util/icons/moveMessage.gif";
  public static final String IMAGE_ADD_FORUM = "../../util/icons/create-action/add-forum.png";
  public static final String IMAGE_ADD_CATEGORY = "../../util/icons/create-action/add-folder.png";
  public static final String IMAGE_WORD = "icons/word.gif";
  public static final String IMAGE_NOTATION_OFF = "../../util/icons/starEmpty.gif";
  public static final String IMAGE_NOTATION_ON = "../../util/icons/starFilled.gif";
  public static final String IMAGE_NOTATION_EMPTY = "../../util/icons/shim.gif";
  public static final String STATUS_VALIDATE = "V";
  public static final String STATUS_FOR_VALIDATION = "A";
  public static final String STATUS_REFUSED = "R";
  private static final String LINK_TO = "<a href=\"";
  private static final String IMAGE_DEFAULT_ATTR = " align=\"middle\" border=\"0\" alt=\"";
  private static final String EDIT_MESSAGE = "editMessage";
  private static final String TITLE = "\" title=\"";
  private static final String EDIT_MESSAGE_KEYWORDS = "editMessageKeywords";
  private static final String TD_DEFAULT_ATTR = "    <td align=\"center\"><span class=\"txtnote\">";
  private static final String VIEW_MESSAGE = "viewMessage";
  private static final String SPAN_TD_END = "</span></td>";
  private static final String TABLE_END = "</table>";
  private static final String LINK_END = "\"></a>";
  private static final String NBSP = "&nbsp;";
  private static final String TD_END = "</td>";

  private ForumHelper() {

  }

  public static int getIntParameter(HttpServletRequest request, String name) {
    return getIntParameter(request, name, -1);
  }

  public static int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
    String param = request.getParameter(name);
    if (StringUtil.isDefined(param)) {
      return Integer.parseInt(param.trim());
    }
    return defaultValue;
  }

  public static String convertDate(Date date, MultiSilverpeasBundle resources) {
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
      SilverLogger.getLogger(ForumHelper.class).warn(ioe);
    }
  }

  public static void addJsResizeFrameCall(JspWriter out, ForumsSessionController fsc) {
    try {
      if (fsc.isResizeFrame()) {
        out.print("resizeFrame();");
      }
    } catch (IOException ioe) {
      SilverLogger.getLogger(ForumHelper.class).warn(ioe);
    }
  }

  private static void displayMessageLine(PrintOutParameters params, Message message,
      RoleMask roleMask, int depth, final boolean isSubscriberByInheritance) {
    ForumsSessionController fsc = params.sessionController;
    try {
      int messageId = message.getId();
      String messageTitle = message.getTitle();
      String author = message.getAuthor();
      String messageAuthor = fsc.getAuthorName(message.getAuthor());
      int messageParent = message.getParentId();
      if (messageAuthor == null) {
        messageAuthor = params.getTranslations().getString("inconnu");
      }
      int forumId = message.getForumId();
      boolean isSubscriber = fsc.isMessageSubscriber(messageId);
      String cellWidth = (params.isSimpleMode() ? " width=\"15\"" : "");
      int lineHeight = ((fsc.isExternal() && roleMask.isReader()) ? 16 : 24);
      // isAutorized : si l'utilisateur est autorisé à modifier le message
      boolean isAutorized = roleMask.isAdmin() || roleMask.isModerator() || roleMask.isAbout(author);
      params.setMessageId(messageId)
          .setForumId(forumId);

      if (message.isValid() || (!message.isValid() && isAutorized)) {

        params.getWriter().println("  <tr id=\"msgLine" + messageId + "\" height=\"" + lineHeight + "\">");

        printOutSubscriptionMessage(params, isSubscriberByInheritance, isSubscriber, cellWidth);

        printOutMessageReadStatus(params, roleMask.getUserId(), roleMask.isReader(), messageParent, cellWidth);

        printOutMessageProperties(params, message, depth, messageTitle, messageAuthor);

        if (!params.isSimpleMode()) {
          printOutAdditionalInfo(params, message);
        }
      }
      // Opérations
      printOutActions(params, roleMask, depth, messageParent, isAutorized);

      params.getWriter().println("  </tr>");
    } catch (IOException ioe) {
      SilverLogger.getLogger(ForumHelper.class).warn(ioe);
    }
  }

  private static void printOutActions(final PrintOutParameters params, final RoleMask roleMask,
      final int depth, final int messageParent, final boolean isAutorized)
      throws IOException {
    JspWriter out = params.getWriter();
    if (isAutorized) {
      int opCellWidth = 40;
      if (depth == 0) {
        opCellWidth += 20;
      }
      if (!params.isForumView()) {
        opCellWidth += 20;
      }
      out.print("    <td align=\"center\" width=\"" + opCellWidth + "\">");
      if (messageParent == 0 && (roleMask.isAdmin() || roleMask.isModerator())) {
        out.print(LINK_TO);
        out.print(ActionUrl.getUrl(EDIT_MESSAGE, params.getCall(), 3, params.getMessageId(),
            params.getForumId()));
        out.print("\"><img src=" + IMAGE_MOVE + IMAGE_DEFAULT_ATTR +
            params.getTranslations().getString("moveMessage") + TITLE +
            params.getTranslations().getString("moveMessage") + LINK_END);
        out.print(NBSP);
      }

      if (!params.isForumView()) {
        out.print("<a href=\"javascript:editMessage(" + params.getMessageId() + ")\">");
        out.print("<img src=" + IMAGE_UPDATE + IMAGE_DEFAULT_ATTR +
            params.getTranslations().getString(EDIT_MESSAGE) + TITLE +
            params.getTranslations().getString(EDIT_MESSAGE) + LINK_END);
        out.print(NBSP);
      }
      out.print(
          "<a href=\"javascript:deleteMessage(" + params.getMessageId() + ", " + messageParent +
              ", false)\">");
      out.print("<img src=" + IMAGE_DELETE + IMAGE_DEFAULT_ATTR +
          params.getTranslations().getString("deleteMessage") + TITLE +
          params.getTranslations().getString("deleteMessage") + LINK_END);

      if (!params.isSimpleMode()) {
        out.print(NBSP);
        out.print(LINK_TO);
        out.print(
            ActionUrl.getUrl(EDIT_MESSAGE_KEYWORDS, params.getCall(), -1, params.getMessageId(),
                params.getForumId()));
        out.print("\"><img src=" + IMAGE_WORD + IMAGE_DEFAULT_ATTR +
            params.getTranslations().getString(EDIT_MESSAGE_KEYWORDS) + TITLE +
            params.getTranslations().getString(EDIT_MESSAGE_KEYWORDS) + LINK_END);
      }

      out.println(TD_END);
    }
  }

  private static void printOutAdditionalInfo(final PrintOutParameters params, final Message message)
      throws IOException {
    // Dernier post
    JspWriter out = params.getWriter();
    out.print(TD_DEFAULT_ATTR);
    int lastMessageId = -1;
    String lastMessageDate = "";
    String lastMessageUser = "";
    Object[] lastMessage =
        params.getSessionController().getLastMessage(params.getForumId(), params.getMessageId());
    if (ArrayUtil.isNotEmpty(lastMessage)) {
      lastMessageId = Integer.parseInt((String) lastMessage[0]);
      lastMessageDate = convertDate((Date) lastMessage[1], params.getResources());
      lastMessageUser = (String) lastMessage[2];
    }
    if (lastMessageDate != null) {
      out.print(LINK_TO +
          ActionUrl.getUrl(VIEW_MESSAGE, params.getCall(), 1, lastMessageId, params.getForumId(),
              true, false) + "\">");
      out.print(WebEncodeHelper.javaStringToHtmlString(lastMessageDate));
      out.print("<br/>");
      out.print(WebEncodeHelper.javaStringToHtmlString(lastMessageUser));
      out.print("</a>");
    }
    out.println(SPAN_TD_END);

    // Nombres de réponses
    out.print(TD_DEFAULT_ATTR);
    out.print(
        params.getSessionController().getNbResponses(params.getForumId(), params.getMessageId()));
    out.println(SPAN_TD_END);

    // Nombres de vues
    out.print(TD_DEFAULT_ATTR);
    out.print(params.getSessionController().getMessageStat(params.getMessageId()));
    out.println(SPAN_TD_END);

    // Notation
    SilverpeasRole highestUserRole = params.getSessionController().getHighestSilverpeasUserRole();
    boolean canUserRating =
        highestUserRole != null && highestUserRole.isGreaterThanOrEquals(SilverpeasRole.USER);
    RaterRatingEntity raterRatingEntity = RaterRatingEntity.fromRateable(message);
    out.print("<td  align=\"center\">");
    out.write(raterRatingEntity.toJSonScript(
        "raterRatingEntity_" + raterRatingEntity.getContributionId()));
    out.write("<div silverpeas-rating readonly=\"true\" raterRating=\"raterRatingEntity_" +
        raterRatingEntity.getContributionId() + "\" shownbraterratings=\"false\" canuserrating=\"" +
        canUserRating + "\"></div>");
    out.println(TD_END);
  }

  private static void printOutMessageProperties(final PrintOutParameters params,
      final Message message, final int depth, final String messageTitle, final String messageAuthor)
      throws IOException {
    // Titre du message
    JspWriter out = params.getWriter();
    out.print("    <td class=\"txtnote\">");
    out.print("<table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">");
    out.print("<tr>");
    out.print("<td width=\"" + depth * 10 + "\">");
    if (depth > 0) {
      out.print("<img src=\"icons/1px.gif\" width=\"" + depth * 10 + "\" height=\"1\">");
    }
    out.print("</td><td align=\"left\"><a href=\"");
    if (params.getSessionController().isDisplayAllMessages() && !params.isForumView()) {
      out.print("javascript:scrollMessage('" + params.getMessageId() + "')");
    } else {
      out.print(ActionUrl.getUrl(VIEW_MESSAGE, params.getCall(), 1, params.getMessageId(),
          params.getForumId(), !params.isSimpleMode(), false));
    }
    out.print("\">");
    out.print("<span class=\"message_" + message.getStatus() + "\"><b>");
    out.print(WebEncodeHelper.javaStringToHtmlString(messageTitle));
    out.print("</b>");
    // Auteur du message
    out.print(params.isSimpleMode() ? NBSP : "<br>");
    out.print("(" + messageAuthor);
    // Date de Creation
    out.print("&nbsp;-&nbsp;" + convertDate(message.getDate(), params.getResources()));
    out.print(")");
    if (message.isToBeValidated()) {
      out.println(" - " + params.getTranslations().getString("toValidate"));
    } else if (message.isRefused()) {
      out.println(" - " + params.getTranslations().getString("refused"));
    }
    out.println("</span>");

    out.print("</a>");
    out.print(TD_END);
    out.print("</tr>");
    out.print(TABLE_END);
    out.println(TD_END);
  }

  private static void printOutMessageReadStatus(final PrintOutParameters params,
      final String userId, final boolean reader, final int messageParent, final String cellWidth)
      throws IOException {
    JspWriter out = params.getWriter();
    out.print("    <td" + cellWidth + ">");

    // rechercher si l'utilisateur a des messages non lus sur ce sujet
    if (messageParent == 0 && (!params.getSessionController().isExternal() || !reader)) {
      boolean isNewMessage = params.getSessionController()
          .isNewMessage(userId, params.getForumId(), params.getMessageId());
      out.print("<img src=\"icons/" + (isNewMessage ? "newMessage" : "noNewMessage") + ".gif\">");
    }
    out.println(TD_END);
  }

  private static void printOutSubscriptionMessage(final PrintOutParameters params,
      final boolean isSubscriberByInheritance, final boolean isSubscriber, final String cellWidth)
      throws IOException {
    JspWriter out = params.getWriter();
    out.print("    <td" + cellWidth + ">");
    if (isSubscriber || isSubscriberByInheritance) {
      out.print("<div class=\"messageFooter\">");
      out.print("<input name=\"checkbox\" type=\"checkbox\" checked ");
      if (!isSubscriber) {
        out.print("disabled ");
      } else {
        out.print("title=\"" + params.getTranslations().getString("unsubscribeMessage") + "\" ");
      }
      out.print("onclick=\"javascript:window.location.href='");
      out.print(
          ActionUrl.getUrl((params.isForumView() ? "viewForum" : VIEW_MESSAGE), params.getCall(),
              13, params.messageId, params.forumId));
      out.print("'\"/></div>");
    } else {
      out.print(NBSP);
    }
    out.println(TD_END);
  }

  public static void displayMessagesList(PrintOutParameters params, RoleMask roleMask,
      boolean isSubscriberByInheritance) {
    try {
      Message[] messages = params.getSessionController().getMessagesList(params.getForumId());
      if (messages.length > 0) {
        params.setMessageId(0);
        scanMessage(messages, params, roleMask, 0, 0, isSubscriberByInheritance);
      } else {
        int colspan = 6;
        if (roleMask.isAdmin() || roleMask.isModerator()) {
          colspan++;
        }
        params.getWriter().println("<tr><td colspan=\"" + colspan + "\" align=center><span class=\"txtnote\">" +
            params.getTranslations().getString("noMessages") + "</span></td></tr>");
      }
    } catch (IOException ioe) {
      SilverLogger.getLogger(ForumHelper.class).warn(ioe);
    }
  }

  public static void displaySingleMessageList(PrintOutParameters params, RoleMask roleMask,
      boolean isSubscriberByInheritance) {
    ForumsSessionController fsc = params.getSessionController();
    JspWriter out = params.getWriter();
    try {
      int messageId = params.getMessageId();
      int parent = fsc.getMessageParentId(messageId);
      while (parent > 0) {
        messageId = parent;
        parent = fsc.getMessageParentId(messageId);
      }
      params.setMessageId(messageId);

      Message[] messages = fsc.getMessagesList(params.getForumId());
      int messagesCount = messages.length;

      if (messagesCount > 0) {
        out.println("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" " +
            "class=\"principal-message\">");

        displayOneMessage(messages, params, roleMask, 0, isSubscriberByInheritance);
        out.println(TABLE_END);

        if (messagesCount > 1) {
          out.println("<div class=\"answer-message\" id=\"msgDiv\">");
          out.println("<table id=\"msgTable\" width=\"100%\" border=\"0\" cellspacing=\"0\" " +
              "cellpadding" + "=\"2\">");
          scanMessage(messages, params, roleMask, 1, -1, isSubscriberByInheritance);
          out.println(TABLE_END);
          out.println("</div>");
        }
      } else {
        out.println("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" " +
            "class=\"contourintfdcolor\">");
        out.println(
            "<tr><td align=\"center\"><span class=\"txtnav\">" +
                params.getTranslations().getString("noMessages") + "</span></td></tr>");
        out.println(TABLE_END);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(ForumHelper.class).warn(e);
      try {
        out.println("ERROR");
      } catch (IOException ioe2) {
        SilverLogger.getLogger(ForumHelper.class).warn(ioe2);
      }
    }
  }

  private static void scanMessage(Message[] messages, PrintOutParameters params, RoleMask roleMask,
      int depth, int maxDepth, boolean isSubscriberByInheritance) {
    int currentMessageId = params.getMessageId();
    for (Message message : messages) {
      int parentId = message.getParentId();
      if (parentId == currentMessageId) {
        int messageId = message.getId();
        params.setMessageId(messageId);

        // Verifying subscription by inheritance
        boolean isMessageSubscriberByInheritance = isSubscriberByInheritance;
        if (!isMessageSubscriberByInheritance) {
          isMessageSubscriberByInheritance =
              params.getSessionController().isMessageSubscriberByInheritance(messageId);
        }

        displayMessageLine(params, message, roleMask, depth, isMessageSubscriberByInheritance);
        boolean hasChildren = hasMessagesChildren(messages, messageId);
        if (hasChildren && (maxDepth == -1 || depth < maxDepth)) {
          scanMessage(messages, params, roleMask,depth + 1, maxDepth,
              isMessageSubscriberByInheritance);
        }
      }
    }
  }

  private static void displayOneMessage(Message[] messages, PrintOutParameters params,
      RoleMask roleMask, int depth, boolean isSubscriberByInheritance) {
    int i = 0;
    boolean loop = true;
    int currentMessageId = params.getMessageId();
    while ((i < messages.length) && loop) {
      Message message = messages[i];
      int messageId = message.getId();
      if (messageId == currentMessageId) {
        displayMessageLine(params, message, roleMask, depth, isSubscriberByInheritance);
        loop = false;
      }
      i++;
    }
  }

  private static boolean hasMessagesChildren(Message[] messages, int messageId) {
    for (Message message : messages) {
      if (message.getParentId() == messageId) {
        return true;
      }
    }
    return false;
  }

  public static class RoleMask {

    private String userId;
    private boolean[] rights = new boolean[] {false, false, false};

    public boolean isAbout(final String userId) {
      return this.userId.equals(userId);
    }

    public String getUserId() {
      return userId;
    }

    public boolean isAdmin() {
      return rights[0];
    }

    public boolean isModerator() {
      return rights[1];
    }

    public boolean isReader() {
      return rights[2];
    }

    public RoleMask setAdmin(boolean isAdmin) {
      this.rights[0] = isAdmin;
      return this;
    }

    public RoleMask setModerator(boolean isModerator) {
      this.rights[1] = isModerator;
      return this;
    }

    public RoleMask setReader(boolean isReader) {
      this.rights[2] = isReader;
      return this;
    }

    public RoleMask setUserId(String id) {
      this.userId = id;
      return this;
    }
  }

  public static class PrintOutParameters {
    private JspWriter writer;
    private ForumsSessionController sessionController;
    private MultiSilverpeasBundle resources;
    private LocalizationBundle translations;
    private String call;
    private int messageId;
    private int forumId;
    private boolean simpleMode;
    private boolean view;

    public JspWriter getWriter() {
      return writer;
    }

    public PrintOutParameters setWriter(final JspWriter writer) {
      this.writer = writer;
      return this;
    }

    public ForumsSessionController getSessionController() {
      return sessionController;
    }

    public PrintOutParameters setSessionController(
        final ForumsSessionController sessionController) {
      this.sessionController = sessionController;
      return this;
    }

    public MultiSilverpeasBundle getResources() {
      return resources;
    }

    public PrintOutParameters setResources(final MultiSilverpeasBundle resources) {
      this.resources = resources;
      return this;
    }

    public LocalizationBundle getTranslations() {
      return translations;
    }

    public PrintOutParameters setTranslations(final LocalizationBundle translations) {
      this.translations = translations;
      return this;
    }

    public int getMessageId() {
      return messageId;
    }

    public PrintOutParameters setMessageId(final int messageId) {
      this.messageId = messageId;
      return this;
    }

    public int getForumId() {
      return forumId;
    }

    public PrintOutParameters setForumId(final int forumId) {
      this.forumId = forumId;
      return this;
    }

    public boolean isSimpleMode() {
      return simpleMode;
    }

    public PrintOutParameters setSimpleMode(final boolean simpleMode) {
      this.simpleMode = simpleMode;
      return this;
    }

    public boolean isForumView() {
      return view;
    }

    public PrintOutParameters setForumView(final boolean view) {
      this.view = view;
      return this;
    }

    public String getCall() {
      return call;
    }

    public PrintOutParameters setCall(final String call) {
      this.call = call;
      return this;
    }
  }
}

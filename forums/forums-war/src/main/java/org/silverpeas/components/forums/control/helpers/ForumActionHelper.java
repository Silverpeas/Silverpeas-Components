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
import org.silverpeas.components.forums.model.Forum;
import org.silverpeas.components.forums.model.Message;
import org.silverpeas.components.forums.service.ForumsException;
import org.silverpeas.core.notification.message.MessageNotifier;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * @author ehugonnet
 */
public class ForumActionHelper {

  public static final int DEPLOY_FORUM = 1;
  public static final int UNDEPLOY_FORUM = 2;
  public static final int CREATE_FORUM = 3;
  public static final int DELETE_FORUM = 4;
  public static final int LOCK_FORUM = 5;
  public static final int UNLOCK_FORUM = 6;
  public static final int UPDATE_FORUM = 7;
  public static final int CREATE_MESSAGE = 8;
  public static final int DELETE_MESSAGE = 9;
  public static final int DEPLOY_MESSAGE = 10;
  public static final int UNDEPLOY_MESSAGE = 11;
  public static final int MOVE_MESSAGE = 12;
  public static final int UNSUBSCRIBE_THREAD = 13;
  public static final int SUBSCRIBE_THREAD = 14;
  public static final int UPDATE_MESSAGE = 15;
  public static final int EVALUATE_FORUM = 16;
  public static final int UNSUBSCRIBE_FORUM = 17;
  public static final int SUBSCRIBE_FORUM = 18;
  public static final int UNSUBSCRIBE_FORUMS = 19;
  public static final int SUBSCRIBE_FORUMS = 20;
  private static final String FORUM_KEYWORDS_PARAMETER = "forumKeywords";
  private static final String FORUM_ID_PARAMETER = "forumId";

  private ForumActionHelper() {
  }

  /**
   * Handles the invocation of the create or update forum action
   * @param request
   * @param fsc
   * @throws ForumsException
   */
  public static void createForumAction(HttpServletRequest request, ForumsSessionController fsc) {
    Invoker invoker = new Invoker(fsc.getUserId(), fsc.isAdmin(), true);
    actionManagement(CREATE_FORUM, -1, (HttpRequest) request, invoker, fsc.getMultilang(), null,
        fsc);
  }

  /**
   * Handles the invocation of the create or update forum action
   * @param request
   * @param fsc
   * @throws ForumsException
   */
  public static void updateForumAction(HttpServletRequest request, ForumsSessionController fsc) {
    Invoker invoker = new Invoker(fsc.getUserId(), fsc.isAdmin(), true);
    actionManagement(UPDATE_FORUM, -1, (HttpRequest) request, invoker, fsc.getMultilang(), null,
        fsc);
  }

  /**
   * Method invoked from JSP (yes, this is bad ... this has to change)
   * @param request
   * @param isAdmin
   * @param isModerator
   * @param userId
   * @param resource
   * @param out
   * @param fsc
   */
  public static void actionManagement(HttpServletRequest request, boolean isAdmin,
      boolean isModerator, String userId, LocalizationBundle resource, JspWriter out,
      ForumsSessionController fsc) {
    int action = ForumHelper.getIntParameter(request, "action");
    int params = ForumHelper.getIntParameter(request, "params");
    Invoker invoker = new Invoker(userId, isAdmin, isModerator);
    actionManagement(action, params, (HttpRequest)request, invoker, resource, out, fsc);
  }

  /**
   * Centralization of different action treatments.
   * @param action the invoked action
   * @param params the parameters of the action
   * @param request the HTTP request behind the action
   * @param invoker the user invoking the action
   * @param translations localized messages
   * @param out the output to use for printing out the action's result.
   * @param fsc the Forum session controller.
   */
  private static void actionManagement(int action, int params, HttpRequest request, Invoker invoker,
      LocalizationBundle translations, JspWriter out,
      ForumsSessionController fsc) {
    if (action != -1) {
      executeAction(action, params, request, invoker, translations, out, fsc);
    }
    if (!fsc.isComponentSubscriptionInfoDisplayed() && fsc.isComponentSubscriber()) {
      MessageNotifier.addInfo(fsc.getString("forums.forum.subscribe.info"));
      fsc.setComponentSubscriptionInfoDisplayed(true);
    }

    int forumId = ForumHelper.getIntParameter(request, FORUM_ID_PARAMETER, 0);
    request.setAttribute("isForumSubscriberByInheritance",
        ((forumId == 0) ? fsc.isComponentSubscriber() :
            fsc.isForumSubscriberByInheritance(forumId)));
  }

  private static void executeAction(final int action, final int params, final HttpRequest request,
      final Invoker invoker, final LocalizationBundle translations, final JspWriter out,
      final ForumsSessionController fsc) {
    try {
      switch (action) {
        case DEPLOY_FORUM:
          fsc.deployForum(params);
          break;
        case UNDEPLOY_FORUM:
          fsc.undeployForum(params);
          break;
        case CREATE_FORUM:
          createForum(request, invoker.getUserId(), fsc);
          break;
        case DELETE_FORUM:
          deleteForum(params, request, fsc);
          break;
        case LOCK_FORUM:
          lockForum(params, invoker.isAdmin(), invoker.isModerator, fsc);
          break;
        case UNLOCK_FORUM:
          boolean success = unlockForum(params, invoker.isAdmin(), invoker.isModerator, fsc);
          if (success) {
            printOutScript(translations, out);
          }
          break;
        case UPDATE_FORUM:
          updateForum(request, fsc);
          break;
        case CREATE_MESSAGE:
          createMessageInForum(request, invoker.getUserId(), fsc);
          break;
        case DELETE_MESSAGE:
          fsc.deleteMessage(params);
          break;
        case DEPLOY_MESSAGE:
          fsc.deployMessage(params);
          break;
        case UNDEPLOY_MESSAGE:
          fsc.undeployMessage(params);
          break;
        case MOVE_MESSAGE:
          moveMessage(request, fsc);
          break;
        case UNSUBSCRIBE_FORUMS:
          fsc.unsubscribeComponent();
          MessageNotifier.addSuccess(translations.getString("forums.unsubscribe.success"));
          break;
        case SUBSCRIBE_FORUMS:
          fsc.subscribeComponent();
          MessageNotifier.addSuccess(translations.getString("forums.subscribe.success"));
          break;
        case UNSUBSCRIBE_FORUM:
          unsubscribeForum(params, translations, fsc);
          break;
        case SUBSCRIBE_FORUM:
          subscribeForum(params, translations, fsc);
          break;
        case UNSUBSCRIBE_THREAD:
          unsubscribeThread(params, translations, fsc);
          break;
        case SUBSCRIBE_THREAD:
          subscribeThread(params, translations, fsc);
          break;
        case UPDATE_MESSAGE:
          updateMessage(request, fsc);
          break;
        default:
          break;
      }
    } catch (IOException | NumberFormatException e) {
      SilverLogger.getLogger(ForumActionHelper.class).warn(e);
    }
  }

  private static void updateMessage(final HttpRequest request, final ForumsSessionController fsc) {
    int messageId = ForumHelper.getIntParameter(request, "messageId");
    String keywords = request.getParameter(FORUM_KEYWORDS_PARAMETER).trim();
    fsc.updateMessageKeywords(messageId, keywords);
  }

  private static void subscribeThread(final int params, final LocalizationBundle resource,
      final ForumsSessionController fsc) {
    Message message = fsc.subscribeMessage(params);
    String bundleKey = message.isSubject() ? "forums.subject.subscribe.success" :
        "forums.message.subscribe.success";
    MessageNotifier.addSuccess(resource.getStringWithParams(bundleKey, message.getTitle()));
  }

  private static void unsubscribeThread(final int params, final LocalizationBundle resource,
      final ForumsSessionController fsc) {
    Message message = fsc.unsubscribeMessage(params);
    String bundleKey = message.isSubject() ? "forums.subject.unsubscribe.success" :
        "forums.message.unsubscribe.success";
    MessageNotifier.addSuccess(resource.getStringWithParams(bundleKey, message.getTitle()));
  }

  private static void subscribeForum(final int params, final LocalizationBundle resource,
      final ForumsSessionController fsc) {
    Forum forum = fsc.subscribeForum(params);
    MessageNotifier.addSuccess(
        resource.getStringWithParams("forums.forum.subscribe.success", forum.getName()));
  }

  private static void unsubscribeForum(final int params, final LocalizationBundle resource,
      final ForumsSessionController fsc) {
    Forum forum = fsc.unsubscribeForum(params);
    MessageNotifier.addSuccess(
        resource.getStringWithParams("forums.forum.unsubscribe.success", forum.getName()));
  }

  private static void printOutScript(final LocalizationBundle resource, final JspWriter out)
      throws IOException {
    out.println("<script language=\"Javascript\">");
    out.println("alert(\"" + resource.getString("adminTopicLock") + "\");");
    out.println("</script>");
  }

  private static void moveMessage(final HttpRequest request, final ForumsSessionController fsc) {
    int messageId = ForumHelper.getIntParameter(request, "messageId");
    int folderId = ForumHelper.getIntParameter(request, "messageNewFolder");
    fsc.moveMessage(messageId, folderId);
  }

  private static void createMessageInForum(final HttpRequest request, final String userId,
      final ForumsSessionController fsc) {
    int forumId = ForumHelper.getIntParameter(request, FORUM_ID_PARAMETER);
    int parentId = ForumHelper.getIntParameter(request, "parentId", 0);
    String messageTitle = request.getParameter("messageTitle").trim();
    String messageText = request.getParameter("messageText").trim();
    String forumKeywords = request.getParameter(FORUM_KEYWORDS_PARAMETER);
    String subscribe = request.getParameter("subscribeMessage");
    if (StringUtil.isDefined(messageTitle) && StringUtil.isDefined(messageText)) {
      int messageId =
          fsc.createMessage(messageTitle, userId, forumId, parentId, messageText, forumKeywords,
              request.getUploadedFiles());
      if (subscribe != null && messageId != 0) {
        fsc.subscribeMessage(messageId);
      }
      if (parentId > 0) {
        fsc.deployMessage(parentId);
      }
    }
  }

  private static void updateForum(final HttpRequest request, final ForumsSessionController fsc) {
    String forumName = request.getParameter("forumName").trim();
    String forumDescription = request.getParameter("forumDescription").trim();
    int forumId = ForumHelper.getIntParameter(request, FORUM_ID_PARAMETER);
    String keywords = request.getParameter(FORUM_KEYWORDS_PARAMETER).trim();
    int forumParent = ForumHelper.getIntParameter(request, "forumFolder");
    String[] forumModerators = request.getParameterValues("moderators");
    fsc.removeAllModerators(forumId);
    if (forumModerators != null) {
      for (String moderator : forumModerators) {
        fsc.addModerator(forumId, moderator.trim());
      }
    }
    String categoryId = request.getParameter("CategoryId");
    fsc.updateForum(forumId, forumName, forumDescription, forumParent, categoryId, keywords);
  }

  private static boolean unlockForum(final int params, final boolean isAdmin,
      final boolean isModerator, final ForumsSessionController fsc) {
    boolean success = true;
    final int adminLevel = 1;
    final int moderatorLevel = 2;
    if (isAdmin) {
      success = fsc.unlockForum(params, adminLevel) == 0;
    } else if (isModerator) {
      success = fsc.unlockForum(params, moderatorLevel) == 0;
    }
    return success;
  }

  private static void lockForum(final int params, final boolean isAdmin, final boolean isModerator,
      final ForumsSessionController fsc) {
    final int adminLevel = 1;
    final int moderatorLevel = 2;
    if (isAdmin) {
      fsc.lockForum(params, adminLevel);
    } else if (isModerator) {
      fsc.lockForum(params, moderatorLevel);
    }
  }

  private static void deleteForum(final int params, final HttpRequest request,
      final ForumsSessionController fsc) {
    fsc.deleteForum(params);
    int forumId = ForumHelper.getIntParameter(request, FORUM_ID_PARAMETER);
    request.setAttribute("nbChildrens", fsc.getForumSonsNb(forumId));
  }

  private static void createForum(final HttpRequest request, final String userId,
      final ForumsSessionController fsc) {
    String forumName = request.getParameter("forumName").trim();
    String forumDescription = request.getParameter("forumDescription").trim();
    String[] forumModerators = request.getParameterValues("moderators");
    int forumParent = ForumHelper.getIntParameter(request, "forumFolder");
    String categoryId = request.getParameter("CategoryId").trim();
    String keywords = request.getParameter(FORUM_KEYWORDS_PARAMETER).trim();
    String positions = request.getParameter("Positions");
    fsc.setForumPositions(positions);
    int forumId =
        fsc.createForum(forumName, forumDescription, userId, forumParent, categoryId, keywords);
    if (forumModerators != null) {
      for (String moderator : forumModerators) {
        fsc.addModerator(forumId, moderator.trim());
      }
    }
    request.setAttribute("nbChildrens", fsc.getForumSonsNb(forumParent));
  }

  private static class Invoker {
    private final boolean isAdmin;
    private final boolean isModerator;
    private final String userId;

    public Invoker(final String userId, final boolean isAdmin, final boolean isModerator) {
      this.isAdmin = isAdmin;
      this.isModerator = isModerator;
      this.userId = userId;
    }

    public boolean isAdmin() {
      return isAdmin;
    }

    public boolean isModerator() {
      return isModerator;
    }

    public String getUserId() {
      return userId;
    }
  }
}
